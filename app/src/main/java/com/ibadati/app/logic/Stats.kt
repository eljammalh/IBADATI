package com.ibadati.app.logic

import com.ibadati.app.data.DayLog
import com.ibadati.app.data.Khatma
import com.ibadati.app.data.PrayerLog
import com.ibadati.app.data.Prayers
import com.ibadati.app.data.Quran
import com.ibadati.app.data.Settings
import com.ibadati.app.data.Status
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** All stored records, grouped for quick lookup. */
data class Records(
    val days: Map<Long, DayLog> = emptyMap(),
    val prayers: Map<Long, Map<Int, PrayerLog>> = emptyMap(),
    val khatmas: List<Khatma> = emptyList(),
) {
    fun day(d: Long): DayLog = days[d] ?: DayLog(d)
    fun prayer(d: Long, i: Int): PrayerLog = prayers[d]?.get(i) ?: Prayers.default(d, i)

    /** First day that has anything logged (or null when nothing is logged yet). */
    val firstDay: Long? by lazy {
        val a = days.values.filter { hasData(it) }.minOfOrNull { it.day }
        val b = prayers.filterValues { m -> m.values.any { hasData(it) } }.keys.minOrNull()
        listOfNotNull(a, b).minOrNull()
    }

    fun hasAnyData(d: Long): Boolean =
        (days[d]?.let { hasData(it) } ?: false) || (prayers[d]?.values?.any { hasData(it) } ?: false)

    companion object {
        fun hasData(d: DayLog) = d.witr > 0 || d.qiyam > 0 || d.athkar != 0 || d.duaMinutes > 0 ||
            d.istighfar > 0 || d.salawat > 0 || d.kahf || d.quranPages > 0

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

    fun allOnTime(r: Records, d: Long): Boolean = (0..4).all {
        val s = r.prayer(d, it).status
        s == Status.JAMAA || s == Status.ON_TIME
    }

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

    /** 0..1 summary of a day for the heatmap; -1 when there's nothing to show. */
    fun dayScore(r: Records, d: Long, s: Settings): Float {
        if (!r.hasAnyData(d)) return -1f
        var prayers = 0.0
        var sunnahDone = 0
        for (i in 0..4) {
            val p = r.prayer(d, i)
            prayers += when (p.status) {
                Status.JAMAA -> 1.0
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

enum class Range { WEEK, MONTH, YEAR, ALL }

/** One bar in a chart: a single day, or a whole month when [monthly]. */
data class Point(val key: Long, val value: Int, val monthly: Boolean)

data class Report(
    val from: Long,
    val to: Long,
    val days: Int,
    val hasData: Boolean,
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
    val avgScore: Double = 0.0,
) {
    val fardhTotal: Int get() = days * 5
    fun frac(v: Int, total: Int): Double = if (total <= 0) 0.0 else v.toDouble() / total
}

object Reports {

    fun build(r: Records, range: Range, today: Long, settings: Settings): Report {
        val first = r.firstDay ?: return Report(today, today, 0, hasData = false)
        val start = when (range) {
            Range.WEEK -> today - 6
            Range.MONTH -> today - 29
            Range.YEAR -> today - 364
            Range.ALL -> first
        }
        val from = maxOf(start, first).coerceAtMost(today)
        val to = today
        val n = (to - from + 1).toInt()
        val dayList = (from..to).toList()

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
        var scoreSum = 0.0
        var scoreDays = 0

        for (d in dayList) {
            for (i in 0..4) {
                val p = r.prayer(d, i)
                when (p.status) {
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
            val sc = Stats.dayScore(r, d, settings)
            scoreSum += sc.coerceAtLeast(0f)
            scoreDays++
        }

        val monthly = range == Range.YEAR || range == Range.ALL
        fun series(value: (DayLog) -> Int): List<Point> {
            if (!monthly) return dayList.map { Point(it, value(r.day(it)), false) }
            val ym0 = YearMonth.from(LocalDate.ofEpochDay(from))
            val ym1 = YearMonth.from(LocalDate.ofEpochDay(to))
            val sums = LinkedHashMap<YearMonth, Int>()
            var ym = ym0
            while (!ym.isAfter(ym1)) { sums[ym] = 0; ym = ym.plusMonths(1) }
            for (d in dayList) {
                val k = YearMonth.from(LocalDate.ofEpochDay(d))
                sums[k] = (sums[k] ?: 0) + value(r.day(d))
            }
            return sums.entries.toList().takeLast(24).map { Point(it.key.atDay(1).toEpochDay(), it.value, true) }
        }

        val sunnahPossible = Prayers.all.map { n * ((if (it.before > 0) 1 else 0) + (if (it.after > 0) 1 else 0)) }

        return Report(
            from = from,
            to = to,
            days = n,
            hasData = true,
            jamaa = jamaa,
            onTime = onTime,
            missed = missed,
            notLogged = n * 5 - jamaa - onTime - missed,
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
            quran = series { it.quranPages },
            quranTotal = dayList.sumOf { r.day(it).quranPages },
            kahfDone = kahf,
            fridays = fridays,
            istighfar = series { it.istighfar },
            salawat = series { it.salawat },
            istighfarTotal = dayList.sumOf { r.day(it).istighfar },
            salawatTotal = dayList.sumOf { r.day(it).salawat },
            avgScore = if (scoreDays > 0) scoreSum / scoreDays else 0.0,
        )
    }
}
