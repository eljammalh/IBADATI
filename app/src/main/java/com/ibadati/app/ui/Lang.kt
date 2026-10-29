package com.ibadati.app.ui

import androidx.compose.runtime.staticCompositionLocalOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import kotlin.math.roundToInt

/**
 * The current language and numeral style. Every piece of text in the app comes
 * through [t] (see Strings.kt), so English and Arabic always stay side by side.
 */
class Lang(val ar: Boolean, val arDigits: Boolean) {

    fun t(en: String, ar: String): String = if (this.ar) ar else en

    /** Converts Western digits to Arabic-Indic digits when that setting is on. */
    fun digits(s: String): String {
        if (!arDigits) return s
        val sb = StringBuilder(s.length)
        for (c in s) {
            sb.append(
                when (c) {
                    in '0'..'9' -> '٠' + (c - '0')
                    '%' -> '٪'
                    '.' -> '٫'
                    else -> c
                },
            )
        }
        return sb.toString()
    }

    fun n(v: Int): String = digits(v.toString())
    fun n(v: Long): String = digits(v.toString())

    /** One decimal place, dropped when whole: 2.0 -> "2", 2.46 -> "2.5". */
    fun dec(v: Double): String {
        val r = (v * 10).roundToInt() / 10.0
        val s = if (r == r.toLong().toDouble()) r.toLong().toString() else r.toString()
        return digits(s)
    }

    fun pct(fraction: Double): String = digits("${(fraction.coerceIn(0.0, 1.0) * 100).roundToInt()}%")

    fun time(h: Int, m: Int): String = digits(String.format(java.util.Locale.US, "%02d:%02d", h, m))

    // ---- Dates ----

    private val weekdaysEn = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val weekdaysAr = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")
    private val monthsEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )
    private val monthsAr = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر",
    )
    private val hijriEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Ula", "Jumada al-Akhirah",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah",
    )
    private val hijriAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
    )

    fun weekday(d: DayOfWeek): String = if (ar) weekdaysAr[d.value - 1] else weekdaysEn[d.value - 1]

    /** Short weekday letter for calendars. */
    fun weekdayShort(d: DayOfWeek): String =
        if (ar) weekdaysAr[d.value - 1].removePrefix("ال").take(2) else weekdaysEn[d.value - 1].take(2)

    fun month(m: Int): String = if (ar) monthsAr[m - 1] else monthsEn[m - 1]
    fun monthShort(m: Int): String = if (ar) monthsAr[m - 1] else monthsEn[m - 1].take(3)

    fun date(day: Long): String {
        val d = LocalDate.ofEpochDay(day)
        return digits("${d.dayOfMonth} ${month(d.monthValue)} ${d.year}")
    }

    fun shortDate(day: Long): String {
        val d = LocalDate.ofEpochDay(day)
        return digits("${d.dayOfMonth} ${monthShort(d.monthValue)}")
    }

    fun hijri(day: Long): String? = try {
        val h = HijrahDate.from(LocalDate.ofEpochDay(day))
        val dd = h.get(ChronoField.DAY_OF_MONTH)
        val mm = h.get(ChronoField.MONTH_OF_YEAR)
        val yy = h.get(ChronoField.YEAR)
        val name = if (ar) hijriAr[mm - 1] else hijriEn[mm - 1]
        digits(if (ar) "$dd $name $yy هـ" else "$dd $name $yy AH")
    } catch (e: Exception) {
        null
    }
}

val LocalLang = staticCompositionLocalOf { Lang(ar = false, arDigits = false) }

/** Accepts both Western and Arabic-Indic digits typed by the user. */
fun parseNumber(s: String): Int? {
    val norm = buildString {
        for (c in s) {
            when (c) {
                in '0'..'9' -> append(c)
                in '٠'..'٩' -> append('0' + (c - '٠'))
                in '۰'..'۹' -> append('0' + (c - '۰'))
            }
        }
    }
    if (norm.isEmpty()) return null
    return norm.take(9).toIntOrNull()
}
