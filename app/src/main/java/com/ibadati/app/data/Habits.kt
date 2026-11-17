package com.ibadati.app.data

object HabitType {
    const val CHECK = 0
    const val AMOUNT = 1
}

object Freq {
    const val DAILY = 0
    const val DAYS = 1
    const val PER_WEEK = 2
    const val ALL_DAYS = 0x7F
}

/** A place in the day to attach a habit to (habit stacking), in daily order. */
data class AnchorDef(val key: String, val en: String, val ar: String)

object Anchors {
    const val CUSTOM = "custom"
    val all = listOf(
        AnchorDef("wake", "waking up", "الاستيقاظ"),
        AnchorDef("fajr", "praying Fajr", "صلاة الفجر"),
        AnchorDef("breakfast", "breakfast", "الفطور"),
        AnchorDef("dhuhr", "praying Dhuhr", "صلاة الظهر"),
        AnchorDef("lunch", "lunch", "الغداء"),
        AnchorDef("asr", "praying Asr", "صلاة العصر"),
        AnchorDef("home", "arriving home", "الوصول إلى البيت"),
        AnchorDef("maghrib", "praying Maghrib", "صلاة المغرب"),
        AnchorDef("dinner", "dinner", "العشاء (الوجبة)"),
        AnchorDef("isha", "praying Isha", "صلاة العشاء"),
        AnchorDef("sleep", "getting ready for sleep", "الاستعداد للنوم"),
    )
    val byKey = all.associateBy { it.key }

    /** Prayer index (0 Fajr … 4 Isha) for prayer anchors, else null. */
    fun prayerIndex(key: String): Int? = when (key) {
        "fajr" -> 0
        "dhuhr" -> 1
        "asr" -> 2
        "maghrib" -> 3
        "isha" -> 4
        else -> null
    }

    /** Sort position in the day; custom anchors come last. */
    fun order(key: String): Int = all.indexOfFirst { it.key == key }.let { if (it < 0) all.size else it }
}

data class UnitDef(val key: String, val en: String, val ar: String)

object Units {
    val all = listOf(
        UnitDef("pages", "pages", "صفحات"),
        UnitDef("ayahs", "ayahs", "آيات"),
        UnitDef("minutes", "minutes", "دقائق"),
        UnitDef("times", "times", "مرات"),
        UnitDef("rakaat", "raka'at", "ركعات"),
    )
    val byKey = all.associateBy { it.key }
}

/**
 * App data a habit can complete from automatically.
 * [check] = done/not done; otherwise an amount in [unit].
 */
data class LinkDef(val key: String, val en: String, val ar: String, val check: Boolean, val unit: String, val target: Double)

object HabitLinks {
    val all = buildList {
        add(LinkDef("quran_pages", "Quran pages read", "صفحات القرآن المقروءة", false, "pages", 2.0))
        add(LinkDef("istighfar", "Istighfar count", "عدد الاستغفار", false, "times", 100.0))
        add(LinkDef("salawat", "Salawat count", "عدد الصلاة على النبي ﷺ", false, "times", 100.0))
        add(LinkDef("dua", "Dua minutes", "دقائق الدعاء", false, "minutes", 5.0))
        add(LinkDef("book_pages", "Book pages read", "صفحات الكتب المقروءة", false, "pages", 10.0))
        add(LinkDef("qiyam", "Qiyam raka'at", "ركعات قيام الليل", false, "rakaat", 2.0))
        add(LinkDef("witr", "Witr raka'at", "ركعات الوتر", false, "rakaat", 1.0))
        add(LinkDef("sunnah", "Sunnah prayers prayed", "السنن الرواتب المؤداة", false, "times", 6.0))
        add(LinkDef("fasting", "Fasting", "الصيام", true, "", 1.0))
        add(LinkDef("kahf", "Surah Al-Kahf", "سورة الكهف", true, "", 1.0))
        Athkar.items.forEach { a -> add(LinkDef("athkar:${a.index}", a.en, a.ar, true, "", 1.0)) }
        Prayers.all.forEach { p ->
            add(LinkDef("mosque:${p.index}", "${p.en} in the mosque", "${p.ar} في المسجد", true, "", 1.0))
            add(LinkDef("jamaa:${p.index}", "${p.en} in jama'a or the mosque", "${p.ar} جماعة أو في المسجد", true, "", 1.0))
            add(LinkDef("ontime:${p.index}", "${p.en} on time", "${p.ar} في وقتها", true, "", 1.0))
        }
        add(LinkDef("all_mosque", "All 5 prayers in the mosque", "الصلوات الخمس في المسجد", true, "", 1.0))
    }
    val byKey = all.associateBy { it.key }
}

/** A starting point for a new habit. Values are the suggested small starter level. */
data class HabitTemplate(
    val key: String,
    val en: String,
    val ar: String,
    val icon: String,
    val color: Int,
    val type: Int,
    val unit: String,
    val target: Double,
    val minimum: Double,
    val anchor: String,
    val link: String? = null,
)

object HabitTemplates {
    val all = listOf(
        HabitTemplate("memorize", "Memorize Quran", "حفظ القرآن", "🧠", 0, HabitType.AMOUNT, "pages", 0.5, 0.25, "fajr"),
        HabitTemplate("read_quran", "Read Quran", "قراءة القرآن", "📖", 1, HabitType.AMOUNT, "pages", 2.0, 1.0, "fajr", "quran_pages"),
        HabitTemplate("duha", "Duha prayer", "صلاة الضحى", "☀️", 2, HabitType.CHECK, "", 1.0, 1.0, "breakfast"),
        HabitTemplate("qiyam", "Qiyam", "قيام الليل", "🌙", 3, HabitType.AMOUNT, "rakaat", 2.0, 2.0, "isha", "qiyam"),
        HabitTemplate("istighfar", "Istighfar", "الاستغفار", "🤲", 4, HabitType.AMOUNT, "times", 100.0, 33.0, "asr", "istighfar"),
        HabitTemplate("salawat", "Salawat", "الصلاة على النبي ﷺ", "💚", 5, HabitType.AMOUNT, "times", 100.0, 10.0, "maghrib", "salawat"),
        HabitTemplate("dua", "Dua", "الدعاء", "🌿", 6, HabitType.AMOUNT, "minutes", 5.0, 1.0, "asr", "dua"),
        HabitTemplate("sadaqah", "Sadaqah", "الصدقة", "💰", 7, HabitType.CHECK, "", 1.0, 1.0, "home"),
        HabitTemplate("book", "Read a book", "قراءة كتاب", "📚", 1, HabitType.AMOUNT, "pages", 10.0, 2.0, "sleep", "book_pages"),
        HabitTemplate("exercise", "Exercise", "الرياضة", "🏃", 2, HabitType.AMOUNT, "minutes", 10.0, 2.0, "home"),
        HabitTemplate("family", "Call parents / family", "الاتصال بالوالدين والأهل", "📞", 5, HabitType.CHECK, "", 1.0, 1.0, "maghrib"),
    )
    val byKey = all.associateBy { it.key }
}

object HabitIcons {
    val all = listOf(
        "⭐", "📖", "🧠", "🤲", "🕌", "🌙", "☀️", "💚", "🌿", "📚", "✍️", "💰",
        "📞", "🏃", "💧", "🍎", "😴", "🧘", "❤️", "🎯", "🌱", "🕋", "📿", "🚶",
    )
}
