package com.ibadati.app.data

/** Fixed definition of one of the five daily prayers. */
data class PrayerDef(
    val index: Int,
    val en: String,
    val ar: String,
    val fardh: Int,
    /** Default Sunnah raka'at before (0 = none). */
    val before: Int,
    /** Default Sunnah raka'at after (0 = none). */
    val after: Int,
    /** True when the Sunnah can be 2 or 4 (Dhuhr). */
    val flexible: Boolean = false,
)

object Prayers {
    val all = listOf(
        PrayerDef(0, "Fajr", "الفجر", fardh = 2, before = 2, after = 0),
        PrayerDef(1, "Dhuhr", "الظهر", fardh = 4, before = 4, after = 2, flexible = true),
        PrayerDef(2, "Asr", "العصر", fardh = 4, before = 4, after = 0),
        PrayerDef(3, "Maghrib", "المغرب", fardh = 3, before = 0, after = 2),
        PrayerDef(4, "Isha", "العشاء", fardh = 4, before = 0, after = 2),
    )

    /** Number of Sunnah "slots" per day: Fajr 1, Dhuhr 2, Asr 1, Maghrib 1, Isha 1. */
    val SUNNAH_SLOTS = all.sumOf { (if (it.before > 0) 1 else 0) + (if (it.after > 0) 1 else 0) }

    val WITR_OPTIONS = listOf(1, 3, 5, 7, 9, 11, 13)
    val QIYAM_OPTIONS = (1..10).map { it * 2 }

    fun default(day: Long, index: Int): PrayerLog {
        val d = all[index]
        return PrayerLog(day, index, beforeRakaat = d.before, afterRakaat = d.after)
    }
}

data class AthkarDef(val index: Int, val en: String, val ar: String, val hintEn: String, val hintAr: String)

object Athkar {
    val items = listOf(
        AthkarDef(0, "Morning Athkar", "أذكار الصباح", "After Fajr until sunrise", "بعد الفجر حتى الشروق"),
        AthkarDef(1, "Fajr Athkar", "أذكار الفجر", "After the prayer", "بعد الصلاة"),
        AthkarDef(2, "Dhuhr Athkar", "أذكار الظهر", "After the prayer", "بعد الصلاة"),
        AthkarDef(3, "Asr Athkar", "أذكار العصر", "After the prayer", "بعد الصلاة"),
        AthkarDef(4, "Maghrib Athkar", "أذكار المغرب", "After the prayer", "بعد الصلاة"),
        AthkarDef(5, "Isha Athkar", "أذكار العشاء", "After the prayer", "بعد الصلاة"),
        AthkarDef(6, "Night Athkar", "أذكار المساء والنوم", "Evening & before sleep", "المساء وقبل النوم"),
    )
}

object Quran {
    const val PAGES = 604
}
