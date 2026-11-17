package com.ibadati.app.logic

import com.ibadati.app.data.Freq
import com.ibadati.app.data.Habit
import com.ibadati.app.data.HabitLinks
import com.ibadati.app.data.HabitRound
import com.ibadati.app.data.HabitType
import com.ibadati.app.data.Prayers
import com.ibadati.app.data.Status
import java.time.DayOfWeek
import java.time.LocalDate

enum class DayMark {
    /** Target reached. */
    DONE,
    /** Only the minimum version. */
    MIN,
    /** A scheduled day that was missed (uses a strike). */
    MISS,
    /** Today, not done yet. */
    PENDING,
    /** Not scheduled (or a non-required day of an X-times-a-week habit). */
    OFF,
    PAUSED,
    /** Before the habit started, or in the future. */
    NONE,
}

/** Everything the screens need to know about one habit. */
data class HabitState(
    val habit: Habit,
    val round: HabitRound,
    val roundNumber: Int,
    /** Completed days in the current round (since its start or its last reset). */
    val progress: Int,
    val goal: Int,
    val strikesUsed: Int,
    val goalReached: Boolean,
    val goalReachedDay: Long?,
    val best: Int,
    val totalDays: Int,
    val totalAmount: Double,
    /** 0..1 consistency over roughly the last 30 scheduled days, weighted toward recent days. */
    val strength: Double,
    val marks: Map<Long, DayMark>,
    /** Day the streak last reset after too many misses, if any. */
    val lastReset: Long?,
    val levels: List<HabitRound>,
) {
    val streak: Int get() = progress
    val level: Int get() = round.level
    fun mark(day: Long): DayMark = marks[day] ?: DayMark.NONE
}

object HabitEngine {

    // ---- Values ----

    /** Linked habits read the app's existing data; others read their own log. */
    fun value(h: Habit, day: Long, logs: Map<Long, Double>, r: Records): Double {
        val link = h.link ?: return logs[day] ?: 0.0
        val d = r.day(day)
        fun b(v: Boolean) = if (v) 1.0 else 0.0
        return when {
            link == "quran_pages" -> d.quranPages.toDouble()
            link == "istighfar" -> d.istighfar.toDouble()
            link == "salawat" -> d.salawat.toDouble()
            link == "dua" -> d.duaMinutes.toDouble()
            link == "book_pages" -> r.readingOn(day).toDouble()
            link == "qiyam" -> d.qiyam.toDouble()
            link == "witr" -> d.witr.toDouble()
            link == "fasting" -> b(d.fasted)
            link == "kahf" -> b(d.kahf)
            link == "sunnah" -> Prayers.all.sumOf { p ->
                val log = r.prayer(day, p.index)
                (if (p.before > 0 && log.sunnahBefore) 1 else 0) + (if (p.after > 0 && log.sunnahAfter) 1 else 0)
            }.toDouble()
            link == "all_mosque" -> b(Stats.allInMosque(r, day))
            link.startsWith("athkar:") -> b(d.athkarDone(link.substringAfter(":").toIntOrNull() ?: 0))
            link.startsWith("mosque:") -> b(r.prayer(day, idx(link)).status == Status.MOSQUE)
            link.startsWith("jamaa:") -> r.prayer(day, idx(link)).status.let { b(it == Status.MOSQUE || it == Status.JAMAA) }
            link.startsWith("ontime:") -> b(Status.prayed(r.prayer(day, idx(link)).status))
            else -> logs[day] ?: 0.0
        }
    }

    private fun idx(link: String) = (link.substringAfter(":").toIntOrNull() ?: 0).coerceIn(0, 4)

    fun isLinkedCheck(h: Habit) = h.link?.let { HabitLinks.byKey[it]?.check } == true

    /** DONE / MIN / null (not done) for a value against a round's target. */
    fun level(h: Habit, v: Double, target: Double, minimum: Double): DayMark? {
        if (h.type == HabitType.CHECK || isLinkedCheck(h)) {
            return when {
                v >= 0.99 -> DayMark.DONE
                v > 0.0 -> DayMark.MIN
                else -> null
            }
        }
        return when {
            v >= target - 1e-9 && v > 0 -> DayMark.DONE
            v >= minimum - 1e-9 && v > 0 -> DayMark.MIN
            else -> null
        }
    }

    // ---- Schedule ----

    fun pausedOn(h: Habit, day: Long): Boolean {
        if (h.pauses.isBlank()) return false
        return h.pauses.split(";").any { seg ->
            val parts = seg.split("-")
            val a = parts.getOrNull(0)?.toLongOrNull() ?: return@any false
            val b = parts.getOrNull(1)?.toLongOrNull()
            day >= a && (b == null || day <= b)
        }
    }

    fun isPaused(h: Habit): Boolean = h.pauses.split(";").any { it.endsWith("-") && it.length > 1 }

    /** Whether the habit is due on this weekday (X-times-a-week habits are due any day). */
    fun scheduledWeekday(h: Habit, day: Long): Boolean = when (h.freq) {
        Freq.DAYS -> {
            val dow = LocalDate.ofEpochDay(day).dayOfWeek.value - 1
            (h.days shr dow) and 1 == 1
        }
        else -> true
    }

    private fun isWeekEnd(day: Long) = LocalDate.ofEpochDay(day).dayOfWeek == DayOfWeek.FRIDAY

    // ---- The simulation ----

    /**
     * Walks every day from the first round to today, applying the streak, strike and round rules.
     * Weeks (for X-times-a-week habits) run Saturday–Friday.
     */
    fun compute(h: Habit, rounds: List<HabitRound>, logs: Map<Long, Double>, r: Records, today: Long): HabitState {
        val sorted = rounds.sortedWith(compareBy({ it.startDay }, { it.id })).ifEmpty {
            listOf(HabitRound(0, h.id, 1, h.target, h.minimum, h.goalDays, h.created))
        }
        val marks = HashMap<Long, DayMark>()
        var roundIdx = 0
        var round = sorted[0]
        var progress = 0
        var strikes = 0
        var reached = false
        var reachedDay: Long? = null
        var best = 0
        var totalDays = 0
        var totalAmount = 0.0
        var lastReset: Long? = null
        var weekDone = 0
        var weekEligible = 0

        var d = sorted[0].startDay
        while (d <= today) {
            // A new round starts on its start day.
            while (roundIdx + 1 < sorted.size && sorted[roundIdx + 1].startDay <= d) {
                roundIdx++
                round = sorted[roundIdx]
                progress = 0; strikes = 0; reached = false; reachedDay = null
                weekDone = 0; weekEligible = 0
            }
            val v = value(h, d, logs, r)
            if (h.type == HabitType.AMOUNT && !isLinkedCheck(h)) totalAmount += v
            val got = level(h, v, round.target, round.minimum)
            val goal = round.goalDays.coerceAtLeast(1)

            when {
                pausedOn(h, d) -> {
                    marks[d] = DayMark.PAUSED
                    if (got != null) totalDays++
                }
                !scheduledWeekday(h, d) -> {
                    marks[d] = if (got != null) got else DayMark.OFF
                    if (got != null) totalDays++
                }
                got != null -> {
                    marks[d] = got
                    totalDays++
                    weekDone++
                    weekEligible++
                    if (!reached) {
                        progress++
                        if (progress >= goal) {
                            reached = true
                            reachedDay = d
                        }
                    }
                    if (progress > best) best = progress
                }
                d == today -> marks[d] = DayMark.PENDING
                h.freq == Freq.PER_WEEK -> {
                    marks[d] = DayMark.OFF
                    weekEligible++
                }
                else -> {
                    marks[d] = DayMark.MISS
                    if (!reached) {
                        strikes++
                        if (strikes > h.strikes) {
                            progress = 0; strikes = 0; lastReset = d
                        }
                    }
                }
            }

            // X times a week: a finished week short of its target counts as one miss.
            if (h.freq == Freq.PER_WEEK && isWeekEnd(d)) {
                if (d < today && !reached && weekEligible > 0 && weekDone < minOf(h.perWeek, weekEligible)) {
                    strikes++
                    if (strikes > h.strikes) {
                        progress = 0; strikes = 0; lastReset = d
                    }
                    if (marks[d] == DayMark.OFF) marks[d] = DayMark.MISS
                }
                weekDone = 0; weekEligible = 0
            }
            d++
        }

        // Strength: recent scheduled days, newest weighted most.
        var num = 0.0
        var den = 0.0
        var w = 1.0
        var counted = 0
        var x = today
        val first = sorted[0].startDay
        while (x >= first && counted < 30) {
            val m = marks[x]
            val score = when (m) {
                DayMark.DONE -> 1.0
                DayMark.MIN -> 0.8
                DayMark.MISS -> 0.0
                DayMark.OFF -> if (h.freq == Freq.PER_WEEK) 0.3 else null
                else -> null // pending today, paused, none: not counted
            }
            if (score != null) {
                num += score * w
                den += w
                w *= 0.92
                counted++
            }
            x--
        }
        val strength = if (den > 0) num / den else 0.0

        return HabitState(
            habit = h,
            round = round,
            roundNumber = roundIdx + 1,
            progress = progress,
            goal = round.goalDays.coerceAtLeast(1),
            strikesUsed = strikes,
            goalReached = reached,
            goalReachedDay = reachedDay,
            best = best,
            totalDays = totalDays,
            totalAmount = totalAmount,
            strength = strength,
            marks = marks,
            lastReset = lastReset,
            levels = sorted,
        )
    }

    /** Due today and not finished (ignores paused/archived). */
    fun dueToday(s: HabitState, today: Long): Boolean =
        !s.habit.archived && !pausedOn(s.habit, today) && scheduledWeekday(s.habit, today) &&
            s.mark(today) == DayMark.PENDING

    /** A gentle level-up suggestion: about +25–50%, rounded to a friendly step. */
    fun suggestLevelUp(target: Double, unit: String): Double {
        val raw = target * 1.4
        val step = when {
            target < 2 -> 0.25
            target < 10 -> 1.0
            target < 50 -> 5.0
            else -> 10.0
        }
        val rounded = kotlin.math.ceil(raw / step) * step
        return if (rounded <= target) target + step else rounded
    }

    fun suggestEasier(target: Double): Double {
        val step = when {
            target <= 2 -> 0.25
            target <= 10 -> 1.0
            target <= 50 -> 5.0
            else -> 10.0
        }
        return maxOf(step, kotlin.math.floor(target * 0.7 / step) * step)
    }
}
