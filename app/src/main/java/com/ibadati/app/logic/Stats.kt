package com.ibadati.app.logic

import com.ibadati.app.data.Book
import com.ibadati.app.data.CustomSin
import com.ibadati.app.data.DayLog
import com.ibadati.app.data.Khatma
import com.ibadati.app.data.PrayerLog
import com.ibadati.app.data.Prayers
import com.ibadati.app.data.Quran
import com.ibadati.app.data.ReadingLog
import com.ibadati.app.data.Settings
import com.ibadati.app.data.Status
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/** All stored records, grouped for quick lookup. */
data class Records(
    val days: Map<Long, DayLog> = emptyMap(),
    val prayers: Map<Long, Map<Int, PrayerLog>> = emptyMap(),
    val khatmas: List<Khatma> = emptyList(),
    val memorized: Set<Int> = emptySet(),
    /** sin key → days it was ticked. */
    val sinDays: Map<String, Set<Long>> = emptyMap(),
    val customSins: List<CustomSin> = emptyList(),
    val books: List<Book> = emptyList(),
    /** book id → (day → pages). */
    val reading: Map<Long, Map<Long, Int>> = emptyMap(),
) {
    fun day(d: Long): DayLog = days[d] ?: DayLog(d)
    fun prayer(d: Long, i: Int): PrayerLog = prayers[d]?.get(i) ?: Prayers.default(d, i)

    fun pagesRead(bookId: Long): Int = reading[bookId]?.values?.sum() ?: 0
    fun readingOn(d: Long): Int = reading.values.sumOf { it[d] ?: 0 }

    /** First day that has anything logged (or null when nothing is logged yet). Sins are not counted. */
    val firstDay: Long? by lazy {
        val a = days.values.filter { hasData(it) }.minOfOrNull { it.day }
        val b = prayers.filterValues { m -> m.values.any { hasData(it) } }.keys.minOrNull()
        val c = reading.values.flatMap { m -> m.filterValues { it > 0 }.keys }.minOrNull()
        listOfNotNull(a, b, c).minOrNull()
    }

    fun hasAnyData(d: Long): Boolean =
        (days[d]?.let { hasData(it) } ?: false) ||
            (prayers[d]?.values?.any { hasData(it) } ?: false) ||
            readingOn(d) > 0

    companion object {
        fun hasData(d: DayLog) = d.witr > 0 || d.qiyam > 0 || d.athkar != 0 || d.duaMinutes > 0 ||
            d.istighfar > 0 || d.salawat > 0 || d.kahf || d.quranPages > 0 || d.fasted

        fun hasData(p: PrayerLog) = p.fardh || p.status != Status.NONE || p.sunnahBefore || p.sunnahAfter
    }
}

data class KhatmaInfo(
    val current: Khatma?,
    /** 1-based number of the current khatma. */
    val number: Int,
    /** Furthest page reached in the current khatma. */
    val maxPage: Int,
    val completed: Boolean,
    val history: List<Khatma>,
) {
    val progress: Double get() = maxPage.toDouble() / Quran.PAGES
    val remaining: Int get() = Quran.PAGES - maxPage
}

/** State of the 40-day mosque challenge. */
data class Challenge(
    /** Completed days in the current challenge (0..39). */
    val current: Int,
    /** Day the current challenge started (null when current = 0). */
    val startDay: Long?,
    /** True when today already counts (all five prayers in the mosque). */
    val todayDone: Boolean,
    /** Longest run of consecutive full days ever. */
    val best: Int,
    /** Days on which each full 40-day challenge was completed. */
    val completions: List<Long>,
) {
    companion object {
        const val LENGTH = 40
    }
}

object Stats {

    fun khatma(r: Records): KhatmaInfo {
        val current = r.khatmas.lastOrNull()
        val maxPage = if (current == null) 0
        else r.days.values.filter { it.khatmaId == current.id && it.quranPages > 0 }.maxOfOrNull { it.quranTo } ?: 0
        return KhatmaInfo(
            current = current,
            number = r.khatmas.size.coerceAtLeast(1),
            maxPage = maxPage,
            completed = current?.endDay != null,
            history = r.khatmas.filter { it.endDay != null },
        )
    }

    /** Where reading on [day] most likely starts: the page after the last one read before it. */
    fun suggestedStart(r: Records, day: Long): Int {
        val k = r.khatmas.lastOrNull() ?: return 1
        if (k.endDay != null && k.endDay < day) return 1
        val last = r.days.values
            .filter { it.day < day && it.khatmaId == k.id && it.quranPages > 0 }
            .maxByOrNull { it.day } ?: return 1
        return (last.quranTo + 1).coerceAtMost(Quran.PAGES)
    }

    /** Average pages per day from the first reading day until today. */
    fun quranDailyAverage(r: Records, today: Long): Double {
        val reads = r.days.values.filter { it.quranPages > 0 }
        if (reads.isEmpty()) return 0.0
        val first = reads.minOf { it.day }
        val span = (today - first + 1).coerceAtLeast(1)
        return reads.sumOf { it.quranPages }.toDouble() / span
    }

    fun allOnTime(r: Records, d: Long): Boolean = (0..4).all { Status.prayed(r.prayer(d, it).status) }

    fun allInMosque(r: Records, d: Long): Boolean = (0..4).all { r.prayer(d, it).status == Status.MOSQUE }

    fun anyMissed(r: Records, d: Long): Boolean = (0..4).any { r.prayer(d, it).status == Status.MISSED }

    /** Current and best run of days with all five prayers on time. */
    fun streaks(r: Records, today: Long): Pair<Int, Int> {
        val first = r.firstDay ?: return 0 to 0
        var best = 0
        var run = 0
        var d = first
        while (d <= today) {
            if (allOnTime(r, d)) {
                run++
                if (run > best) best = run
            } else {
                run = 0
            }
            d++
        }
        // Today still in progress: don't break the streak just because today isn't finished yet.
        var current = 0
        var c = if (allOnTime(r, today)) today else today - 1
        while (c >= first && allOnTime(r, c)) {
            current++
            c--
        }
        return current to best
    }

    /**
     * The 40-day challenge. A day counts when all five prayers are "in the mosque".
     * A past day that doesn't count resets the challenge; today only resets it once a prayer is marked missed.
     * Reaching 40 completes the challenge and a new one starts the next day.
     */
    fun challenge(r: Records, today: Long): Challenge {
        val mosqueDays = r.prayers.filterValues { m -> m.size == 5 && m.values.all { it.status == Status.MOSQUE } }.keys
        val first = mosqueDays.minOrNull() ?: return Challenge(0, null, false, 0, emptyList())
        var run = 0
        var start: Long? = null
        var consecutive = 0
        var best = 0
        val completions = mutableListOf<Long>()
        var d = first
        while (d <= today) {
            val full = d in mosqueDays
            if (full) {
                consecutive++
                if (consecutive > best) best = consecutive
                if (run == 0) start = d
                run++
                if (run == Challenge.LENGTH) {
                    completions += d
                    run = 0
                    start = null
                }
            } else if (d < today || anyMissed(r, d)) {
                consecutive = 0
                run = 0
                start = null
            }
            d++
        }
        return Challenge(run, start, today in mosqueDays, best, completions)
    }

    /** 0..1 summary of a day for the heatmap; -1 when there's nothing to show. */
    fun dayScore(r: Records, d: Long, s: Settings): Float {
        if (!r.hasAnyData(d)) return -1f
        var prayers = 0.0
        var sunnahDone = 0
        for (i in 0..4) {
            val p = r.prayer(d, i)
            prayers += when (p.status) {
                Status.MOSQUE -> 1.0
                Status.JAMAA -> 0.95
                Status.ON_TIME -> 0.85
                Status.MISSED -> 0.0
                else -> if (p.fardh) 0.6 else 0.0
            }
            val def = Prayers.all[i]
            if (def.before > 0 && p.sunnahBefore) sunnahDone++
            if (def.after > 0 && p.sunnahAfter) sunnahDone++
        }
        val day = r.day(d)
        val quran = if (s.quranGoal > 0) (day.quranPages.toDouble() / s.quranGoal).coerceAtMost(1.0) else 0.0
        val ist = if (s.istighfarGoal > 0) (day.istighfar.toDouble() / s.istighfarGoal).coerceAtMost(1.0) else 0.0
        val sal = if (s.salawatGoal > 0) (day.salawat.toDouble() / s.salawatGoal).coerceAtMost(1.0) else 0.0
        val score = 0.5 * (prayers / 5) +
            0.1 * (sunnahDone.toDouble() / Prayers.SUNNAH_SLOTS) +
            0.2 * (day.athkarCount / 7.0) +
            0.1 * quran +
            0.1 * ((ist + sal) / 2)
        return score.toFloat().coerceIn(0f, 1f)
    }
}

// ---------------------------------------------------------------- Fasting

enum class SunnahFast { MONDAY, THURSDAY, WHITE }

object Fasting {
    fun hijriDay(day: Long): Int? = try {
        HijrahDate.from(LocalDate.ofEpochDay(day)).get(ChronoField.DAY_OF_MONTH)
    } catch (e: Exception) {
        null
    }

    /** Which Sunnah fasts fall on this day (a White Day can also be a Monday or Thursday). */
    fun kinds(day: Long): Set<SunnahFast> {
        val out = mutableSetOf<SunnahFast>()
        when (LocalDate.ofEpochDay(day).dayOfWeek) {
            DayOfWeek.MONDAY -> out += SunnahFast.MONDAY
            DayOfWeek.THURSDAY -> out += SunnahFast.THURSDAY
            else -> {}
        }
        if ((hijriDay(day) ?: 0) in 13..15) out += SunnahFast.WHITE
        return out
    }

    fun isSunnah(day: Long) = kinds(day).isNotEmpty()

    /** The next Sunnah fasting day from [from] (inclusive). */
    fun next(from: Long): Long {
        var d = from
        while (!isSunnah(d)) d++
        return d
    }
}

// ---------------------------------------------------------------- Memorization

data class Memorization(
    val surahs: Int,
    val pages: Double,
    /** Memorized pages in each juz (index 0 = juz 1). */
    val perJuz: List<Double>,
) {
    val fraction: Double get() = pages / 604.0
    /** Juz memorized completely. */
    val fullJuz: Int get() = perJuz.indices.count { perJuz[it] >= juzLength(it + 1) - 0.05 }

    companion object {
        /** Madani Mushaf: juz 1 = pages 1–21, juz n = pages 20n−18 … 20n+1, juz 30 ends at 604. */
        fun juzStart(n: Int): Double = if (n == 1) 0.0 else (20 * (n - 1) + 1).toDouble()
        fun juzEnd(n: Int): Double = if (n == 30) 604.0 else juzStart(n + 1)
        fun juzLength(n: Int): Double = juzEnd(n) - juzStart(n)
    }
}

object Memorize {
    fun compute(ids: Set<Int>): Memorization {
        val perJuz = DoubleArray(30)
        var pages = 0.0
        for (id in ids) {
            val s = com.ibadati.app.data.Mushaf.byId(id)
            pages += s.length
            for (j in 1..30) {
                val overlap = minOf(s.end, Memorization.juzEnd(j)) - maxOf(s.start, Memorization.juzStart(j))
                if (overlap > 0) perJuz[j - 1] += overlap
            }
        }
        return Memorization(ids.size, pages, perJuz.toList())
    }
}

// ---------------------------------------------------------------- Reports

enum class Range { TODAY, WEEK, MONTH, YEAR, ALL }

/** One bar in a chart: a single day, or a whole month when [monthly]. */
data class Point(val key: Long, val value: Int, val monthly: Boolean)

data class Report(
    val from: Long,
    val to: Long,
    val days: Int,
    val hasData: Boolean,
    val mosque: Int = 0,
    val jamaa: Int = 0,
    val onTime: Int = 0,
    val missed: Int = 0,
    val notLogged: Int = 0,
    val sunnahDone: Int = 0,
    val sunnahTotal: Int = 0,
    val sunnahRakaat: Int = 0,
    /** Per prayer: (done, possible). */
    val sunnahByPrayer: List<Pair<Int, Int>> = emptyList(),
    val witrNights: Int = 0,
    val witrAvg: Double = 0.0,
    val qiyamNights: Int = 0,
    val qiyamAvg: Double = 0.0,
    val athkarDone: List<Int> = emptyList(),
    val duaTotal: Int = 0,
    val quran: List<Point> = emptyList(),
    val quranTotal: Int = 0,
    val kahfDone: Int = 0,
    val fridays: Int = 0,
    val istighfar: List<Point> = emptyList(),
    val salawat: List<Point> = emptyList(),
    val istighfarTotal: Int = 0,
    val salawatTotal: Int = 0,
    val fastDays: Int = 0,
    val sunnahFastDone: Int = 0,
    val sunnahFastAvailable: Int = 0,
    val reading: List<Point> = emptyList(),
    val readingTotal: Int = 0,
    val booksCompleted: Int = 0,
    val avgScore: Double = 0.0,
) {
    val fardhTotal: Int get() = days * 5
    fun frac(v: Int, total: Int): Double = if (total <= 0) 0.0 else v.toDouble() / total
}

object Reports {

    fun build(r: Records, range: Range, today: Long, settings: Settings): Report {
        val first = r.firstDay ?: return Report(today, today, 0, hasData = false)
        val start = when (range) {
            Range.TODAY -> today
            Range.WEEK -> today - 6
            Range.MONTH -> today - 29
            Range.YEAR -> today - 364
            Range.ALL -> first
        }
        val from = if (range == Range.TODAY) today else maxOf(start, first).coerceAtMost(today)
        val to = today
        val n = (to - from + 1).toInt()
        val dayList = (from..to).toList()

        var mosque = 0
        var jamaa = 0
        var onTime = 0
        var missed = 0
        var sunnahDone = 0
        var sunnahRakaat = 0
        val byPrayer = IntArray(5)
        var witrNights = 0
        var witrRak = 0
        var qiyamNights = 0
        var qiyamRak = 0
        val athkar = IntArray(7)
        var dua = 0
        var kahf = 0
        var fridays = 0
        var fastDays = 0
        var sunnahFastDone = 0
        var sunnahFastAvailable = 0
        var scoreSum = 0.0
        var scoreDays = 0

        for (d in dayList) {
            for (i in 0..4) {
                val p = r.prayer(d, i)
                when (p.status) {
                    Status.MOSQUE -> mosque++
                    Status.JAMAA -> jamaa++
                    Status.ON_TIME -> onTime++
                    Status.MISSED -> missed++
                }
                val def = Prayers.all[i]
                if (def.before > 0 && p.sunnahBefore) {
                    sunnahDone++; byPrayer[i]++; sunnahRakaat += p.beforeRakaat.takeIf { it > 0 } ?: def.before
                }
                if (def.after > 0 && p.sunnahAfter) {
                    sunnahDone++; byPrayer[i]++; sunnahRakaat += p.afterRakaat.takeIf { it > 0 } ?: def.after
                }
            }
            val day = r.day(d)
            if (day.witr > 0) { witrNights++; witrRak += day.witr }
            if (day.qiyam > 0) { qiyamNights++; qiyamRak += day.qiyam }
            for (i in 0..6) if (day.athkarDone(i)) athkar[i]++
            dua += day.duaMinutes
            if (LocalDate.ofEpochDay(d).dayOfWeek == DayOfWeek.FRIDAY) {
                fridays++
                if (day.kahf) kahf++
            }
            if (day.fasted) fastDays++
            if (Fasting.isSunnah(d)) {
                sunnahFastAvailable++
                if (day.fasted) sunnahFastDone++
            }
            val sc = Stats.dayScore(r, d, settings)
            scoreSum += sc.coerceAtLeast(0f)
            scoreDays++
        }

        val monthly = range == Range.YEAR || range == Range.ALL
        fun series(value: (Long) -> Int): List<Point> {
            if (!monthly) return dayList.map { Point(it, value(it), false) }
            val ym0 = YearMonth.from(LocalDate.ofEpochDay(from))
            val ym1 = YearMonth.from(LocalDate.ofEpochDay(to))
            val sums = LinkedHashMap<YearMonth, Int>()
            var ym = ym0
            while (!ym.isAfter(ym1)) { sums[ym] = 0; ym = ym.plusMonths(1) }
            for (d in dayList) {
                val k = YearMonth.from(LocalDate.ofEpochDay(d))
                sums[k] = (sums[k] ?: 0) + value(d)
            }
            return sums.entries.toList().takeLast(24).map { Point(it.key.atDay(1).toEpochDay(), it.value, true) }
        }

        val sunnahPossible = Prayers.all.map { n * ((if (it.before > 0) 1 else 0) + (if (it.after > 0) 1 else 0)) }

        return Report(
            from = from,
            to = to,
            days = n,
            hasData = true,
            mosque = mosque,
            jamaa = jamaa,
            onTime = onTime,
            missed = missed,
            notLogged = n * 5 - mosque - jamaa - onTime - missed,
            sunnahDone = sunnahDone,
            sunnahTotal = n * Prayers.SUNNAH_SLOTS,
            sunnahRakaat = sunnahRakaat,
            sunnahByPrayer = (0..4).map { byPrayer[it] to sunnahPossible[it] },
            witrNights = witrNights,
            witrAvg = if (witrNights > 0) witrRak.toDouble() / witrNights else 0.0,
            qiyamNights = qiyamNights,
            qiyamAvg = if (qiyamNights > 0) qiyamRak.toDouble() / qiyamNights else 0.0,
            athkarDone = athkar.toList(),
            duaTotal = dua,
            quran = series { r.day(it).quranPages },
            quranTotal = dayList.sumOf { r.day(it).quranPages },
            kahfDone = kahf,
            fridays = fridays,
            istighfar = series { r.day(it).istighfar },
            salawat = series { r.day(it).salawat },
            istighfarTotal = dayList.sumOf { r.day(it).istighfar },
            salawatTotal = dayList.sumOf { r.day(it).salawat },
            fastDays = fastDays,
            sunnahFastDone = sunnahFastDone,
            sunnahFastAvailable = sunnahFastAvailable,
            reading = series { r.readingOn(it) },
            readingTotal = dayList.sumOf { r.readingOn(it) },
            booksCompleted = r.books.count { it.completedDay != null && it.completedDay in from..to },
            avgScore = if (scoreDays > 0) scoreSum / scoreDays else 0.0,
        )
    }
}

// ---------------------------------------------------------------- Reading

data class BookProgress(val book: Book, val read: Int) {
    val fraction: Double get() = (read.toDouble() / book.totalPages).coerceIn(0.0, 1.0)
    val remaining: Int get() = (book.totalPages - read).coerceAtLeast(0)
}

object Reading {
    /** Pages per day since the book was added, and the estimated finish day (null if no pace yet). */
    fun pace(r: Records, b: Book, today: Long): Pair<Double, Long?> {
        val logs = r.reading[b.id] ?: emptyMap()
        val read = logs.values.sum()
        if (read <= 0) return 0.0 to null
        val first = minOf(b.addedDay, logs.filterValues { it > 0 }.keys.minOrNull() ?: b.addedDay)
        val span = (today - first + 1).coerceAtLeast(1)
        val pace = read.toDouble() / span
        val left = (b.totalPages - read).coerceAtLeast(0)
        val finish = if (left == 0) today else today + kotlin.math.ceil(left / pace).toLong()
        return pace to finish
    }

    fun logsOf(logs: List<ReadingLog>): Map<Long, Map<Long, Int>> =
        logs.groupBy { it.bookId }.mapValues { (_, l) -> l.associate { it.day to it.pages } }
}
