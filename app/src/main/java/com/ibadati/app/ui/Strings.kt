package com.ibadati.app.ui

import com.ibadati.app.data.AthkarDef
import com.ibadati.app.data.PrayerDef
import com.ibadati.app.data.Status

/*
 * All app text, English and Arabic side by side.
 * To change any wording, edit it here.
 */

// ---- App & navigation ----
val Lang.appName get() = t("Ibadati", "عبادتي")
val Lang.tabPrayers get() = t("Prayers", "الصلوات")
val Lang.tabAthkar get() = t("Athkar", "الأذكار")
val Lang.tabQuran get() = t("Quran", "القرآن")
val Lang.tabDhikr get() = t("Dhikr", "الذكر")
val Lang.tabReports get() = t("Reports", "التقارير")
val Lang.settings get() = t("Settings", "الإعدادات")
val Lang.back get() = t("Back", "رجوع")

// ---- Common ----
val Lang.cancel get() = t("Cancel", "إلغاء")
val Lang.save get() = t("Save", "حفظ")
val Lang.clear get() = t("Clear", "مسح")
val Lang.done get() = t("Done", "تم")
val Lang.none get() = t("None", "لا شيء")
val Lang.today get() = t("Today", "اليوم")
val Lang.yesterday get() = t("Yesterday", "أمس")
val Lang.backToToday get() = t("Back to today", "العودة لليوم")
val Lang.goal get() = t("Goal", "الهدف")
val Lang.ofGoal get() = t("of goal", "من الهدف")
fun Lang.goalOf(v: String) = t("Goal: $v", "الهدف: $v")

// ---- Welcome ----
val Lang.welcomeTagEn get() = "Track your prayers, athkar and Quran"
val Lang.welcomeTagAr get() = "تابع صلواتك وأذكارك وقراءتك للقرآن"
val Lang.chooseLanguageEn get() = "Choose your language"
val Lang.chooseLanguageAr get() = "اختر لغتك"

// ---- Prayers ----
fun Lang.prayerName(p: PrayerDef) = t(p.en, p.ar)
val Lang.sunnah get() = t("Sunnah", "سنة")
val Lang.fardh get() = t("Fardh", "فرض")
val Lang.rakaat get() = t("raka'at", "ركعات")
val Lang.witr get() = t("Witr", "الوتر")
val Lang.qiyam get() = t("Qiyam", "قيام الليل")
val Lang.witrHint get() = t("Odd raka'at · 1 to 13", "عدد فردي · من ١ إلى ١٣".let { if (arDigits) it else "عدد فردي · من 1 إلى 13" })
val Lang.qiyamHint get() = t("Even raka'at · up to 20", "عدد زوجي · حتى ٢٠".let { if (arDigits) it else "عدد زوجي · حتى 20" })
val Lang.nightPrayers get() = t("Night prayers", "صلاة الليل")
val Lang.dailyPrayers get() = t("Daily prayers", "الصلوات الخمس")
val Lang.prayedToday get() = t("prayers prayed", "صلوات مؤداة")
val Lang.howDidYouPray get() = t("How did you pray it?", "كيف صليتها؟")
val Lang.statusJamaa get() = t("In Jama'a", "جماعة")
val Lang.statusOnTime get() = t("On time", "في الوقت")
val Lang.statusMissed get() = t("Missed", "فائتة")
val Lang.statusNotSet get() = t("Not set", "لم تُحدد")
val Lang.statusJamaaHint get() = t("Prayed with the congregation", "صليتها مع الجماعة")
val Lang.statusOnTimeHint get() = t("Prayed on time, by myself", "صليتها في وقتها منفردًا")
val Lang.statusMissedHint get() = t("The prayer time passed", "خرج وقت الصلاة")
val Lang.clearStatus get() = t("Clear status", "مسح الحالة")
val Lang.dhuhrTip get() = t(
    "Tip: long-press a Dhuhr Sunnah circle to switch between 2 and 4 raka'at.",
    "تلميح: اضغط مطولًا على دائرة سنة الظهر للاختيار بين ركعتين وأربع.",
)
fun Lang.sunnahRakaatTitle(before: Boolean) =
    if (before) t("Sunnah before Dhuhr", "سنة الظهر القبلية") else t("Sunnah after Dhuhr", "سنة الظهر البعدية")
fun Lang.rakaatCount(c: Int): String = if (!ar) "${n(c)} raka'at" else when (c) {
    1 -> "ركعة واحدة"
    2 -> "ركعتان"
    in 3..10 -> "${n(c)} ركعات"
    else -> "${n(c)} ركعة"
}
fun Lang.statusLabel(s: Int) = when (s) {
    Status.JAMAA -> statusJamaa
    Status.ON_TIME -> statusOnTime
    Status.MISSED -> statusMissed
    else -> statusNotSet
}
fun Lang.chooseRakaat(name: String) = t("$name raka'at", "عدد ركعات $name")

// ---- Athkar ----
fun Lang.athkarName(a: AthkarDef) = t(a.en, a.ar)
fun Lang.athkarHint(a: AthkarDef) = t(a.hintEn, a.hintAr)
val Lang.dua get() = t("Dua", "الدعاء")
val Lang.duaTime get() = t("Dua time", "وقت الدعاء")
val Lang.duaHowLong get() = t("How long did you make dua today?", "كم دقيقة دعوت اليوم؟")
fun Lang.minutes(v: Int): String = if (!ar) (if (v == 1) "1 minute" else "${n(v)} minutes") else when (v) {
    1 -> "دقيقة واحدة"
    2 -> "دقيقتان"
    in 3..10 -> "${n(v)} دقائق"
    else -> "${n(v)} دقيقة"
}
val Lang.minShort get() = t("min", "د")
val Lang.athkarCompleted get() = t("athkar completed", "أذكار مكتملة")
val Lang.tapToMark get() = t("Tap a circle to mark it done", "اضغط على الدائرة لتسجيلها")

// ---- Quran ----
val Lang.khatmaProgress get() = t("Khatma progress", "تقدم الختمة")
fun Lang.khatmaNumber(i: Int) = t("Khatma #${n(i)}", "الختمة رقم ${n(i)}")
fun Lang.pageOf(p: Int) = t("Page ${n(p)} of ${n(604)}", "صفحة ${n(p)} من ${n(604)}")
fun Lang.pagesRemaining(p: Int) = t("${n(p)} pages remaining", "متبقٍ ${n(p)} صفحة")
fun Lang.pages(v: Int): String = if (!ar) (if (v == 1) "1 page" else "${n(v)} pages") else when (v) {
    1 -> "صفحة واحدة"
    2 -> "صفحتان"
    in 3..10 -> "${n(v)} صفحات"
    else -> "${n(v)} صفحة"
}
val Lang.todaysReading get() = t("Today's reading", "قراءة اليوم")
val Lang.daysReading get() = t("This day's reading", "قراءة هذا اليوم")
val Lang.startPage get() = t("Started at page", "بدأت من صفحة")
val Lang.stopPage get() = t("Stopped at page", "توقفت عند صفحة")
val Lang.pagesRead get() = t("Pages read", "الصفحات المقروءة")
val Lang.tapToChoose get() = t("Tap to choose", "اضغط للاختيار")
val Lang.dailyAverage get() = t("Daily average", "المعدل اليومي")
val Lang.totalPages get() = t("Total pages", "مجموع الصفحات")
val Lang.choosePage get() = t("Choose page", "اختر الصفحة")
val Lang.surahKahf get() = t("Surah Al-Kahf", "سورة الكهف")
val Lang.kahfHint get() = t("Friday Sunnah · tap when you've read it", "سنة يوم الجمعة · اضغط عند قراءتها")
val Lang.khatmaHistory get() = t("Completed khatmas", "الختمات المكتملة")
val Lang.noKhatmasYet get() = t("Your completed khatmas will appear here.", "ستظهر ختماتك المكتملة هنا.")
fun Lang.khatmaRange(from: String, to: String, days: Int) =
    t("$from → $to · ${daysCount(days)}", "$from ← $to · ${daysCount(days)}")
val Lang.khatmaComplete get() = t("Khatma complete!", "ختمة مباركة!")
val Lang.khatmaCompleteText get() = t(
    "You have completed the whole Quran. May Allah accept it from you.",
    "أتممت قراءة القرآن كاملًا. تقبّل الله منك.",
)
val Lang.startNewKhatma get() = t("Start a new khatma", "ابدأ ختمة جديدة")
val Lang.khatmaFinishedBanner get() = t(
    "You finished this khatma. Start a new one whenever you're ready.",
    "أتممت هذه الختمة. ابدأ ختمة جديدة متى شئت.",
)
val Lang.alhamdulillah get() = t("Alhamdulillah", "الحمد لله")
fun Lang.daysCount(v: Int): String = if (!ar) (if (v == 1) "1 day" else "${n(v)} days") else when (v) {
    1 -> "يوم واحد"
    2 -> "يومان"
    in 3..10 -> "${n(v)} أيام"
    else -> "${n(v)} يومًا"
}

// ---- Istighfar & Salawat ----
val Lang.istighfar get() = t("Istighfar", "الاستغفار")
val Lang.salawat get() = t("Salah 'ala an-Nabi ﷺ", "الصلاة على النبي ﷺ")
val Lang.istighfarPhrase get() = "أستغفر الله وأتوب إليه"
val Lang.salawatPhrase get() = "اللهم صلِّ وسلِّم على نبينا محمد ﷺ"
val Lang.typeCount get() = t("Type today's count", "اكتب العدد")
val Lang.reset get() = t("Reset", "تصفير")
val Lang.dhikrHeader get() = t("Istighfar & Salawat", "الاستغفار والصلاة على النبي ﷺ")

// ---- Reports ----
val Lang.rangeWeek get() = t("Week", "أسبوع")
val Lang.rangeMonth get() = t("Month", "شهر")
val Lang.rangeYear get() = t("Year", "سنة")
val Lang.rangeAll get() = t("All time", "الكل")
val Lang.overview get() = t("Overview", "نظرة عامة")
val Lang.currentStreak get() = t("Current streak", "السلسلة الحالية")
val Lang.bestStreak get() = t("Best streak", "أفضل سلسلة")
val Lang.streakHint get() = t("Days with all 5 prayers on time", "أيام بالصلوات الخمس في وقتها")
val Lang.overallScore get() = t("Overall score", "التقييم العام")
val Lang.prayedOnTime get() = t("Prayed on time", "في الوقت")
val Lang.notLogged get() = t("Not logged", "غير مسجلة")
val Lang.sunnahCompleted get() = t("Sunnah prayers completed", "السنن الرواتب المؤداة")
fun Lang.sunnahRakaatTotal(v: Int) = t("${n(v)} Sunnah raka'at prayed", "${n(v)} ركعة من السنن")
val Lang.sunnahByPrayer get() = t("Sunnah by prayer", "السنن حسب الصلاة")
val Lang.witrQiyam get() = t("Witr & Qiyam", "الوتر وقيام الليل")
fun Lang.nightsOf(done: Int, total: Int) = t("${n(done)} of ${n(total)} nights", "${n(done)} من ${n(total)} ليلة")
fun Lang.avgRakaat(v: Double) = t("avg ${dec(v)} raka'at", "متوسط ${dec(v)} ركعة")
val Lang.athkarCompletion get() = t("Athkar completion", "إتمام الأذكار")
val Lang.duaTotal get() = t("Total dua time", "مجموع وقت الدعاء")
val Lang.perDay get() = t("per day", "يوميًا")
val Lang.pagesPerDay get() = t("Pages read", "الصفحات المقروءة")
val Lang.kahfFridays get() = t("Al-Kahf Fridays", "جُمَع سورة الكهف")
fun Lang.fridaysOf(done: Int, total: Int) = t("${n(done)} of ${n(total)} Fridays", "${n(done)} من ${n(total)} جمعة")
val Lang.total get() = t("Total", "المجموع")
val Lang.average get() = t("Average", "المتوسط")
val Lang.heatmapTitle get() = t("Daily performance · last 16 weeks", "الأداء اليومي · آخر ١٦ أسبوعًا".let { if (arDigits) it else "الأداء اليومي · آخر 16 أسبوعًا" })
val Lang.less get() = t("Less", "أقل")
val Lang.more get() = t("More", "أكثر")
val Lang.noDataYet get() = t(
    "Start logging and your reports will appear here.",
    "ابدأ التسجيل وستظهر تقاريرك هنا.",
)
fun Lang.trackedDays(v: Int) = t("Based on ${daysCount(v)}", "بناءً على ${daysCount(v)}")
val Lang.perMonthNote get() = t("Monthly totals", "المجموع الشهري")
val Lang.perDayNote get() = t("Daily totals", "المجموع اليومي")

// ---- Settings ----
val Lang.language get() = t("Language", "اللغة")
val Lang.numerals get() = t("Numerals", "الأرقام")
val Lang.numeralsArabic get() = "١٢٣"
val Lang.numeralsWestern get() = "123"
val Lang.showHijri get() = t("Show Hijri date", "إظهار التاريخ الهجري")
val Lang.appearance get() = t("Appearance", "المظهر")
val Lang.themeSystem get() = t("System", "تلقائي")
val Lang.themeLight get() = t("Light", "فاتح")
val Lang.themeDark get() = t("Dark", "داكن")
val Lang.dailyGoals get() = t("Daily goals", "الأهداف اليومية")
val Lang.goalDua get() = t("Dua (minutes)", "الدعاء (دقائق)")
val Lang.goalIstighfar get() = t("Istighfar", "الاستغفار")
val Lang.goalSalawat get() = t("Salawat", "الصلاة على النبي ﷺ")
val Lang.goalQuran get() = t("Quran (pages)", "القرآن (صفحات)")
val Lang.reminders get() = t("Reminders", "التذكيرات")
val Lang.remMorning get() = t("Morning athkar", "أذكار الصباح")
val Lang.remEvening get() = t("Evening athkar", "أذكار المساء")
val Lang.remQuran get() = t("Quran reading", "قراءة القرآن")
val Lang.remKahf get() = t("Surah Al-Kahf (Fridays)", "سورة الكهف (الجمعة)")
val Lang.chooseTime get() = t("Choose time", "اختر الوقت")
val Lang.notificationsBlocked get() = t(
    "Notifications are blocked. Allow them in your phone's settings to get reminders.",
    "الإشعارات متوقفة. اسمح بها من إعدادات الهاتف لتصلك التذكيرات.",
)
val Lang.backup get() = t("Backup & restore", "النسخ الاحتياطي والاستعادة")
val Lang.backupHint get() = t(
    "Save all your records to a file, or restore them from one.",
    "احفظ جميع سجلاتك في ملف، أو استعدها منه.",
)
val Lang.exportBackup get() = t("Save backup", "حفظ نسخة احتياطية")
val Lang.importBackup get() = t("Restore", "استعادة")
val Lang.restoreConfirmTitle get() = t("Restore backup?", "استعادة النسخة الاحتياطية؟")
val Lang.restoreConfirmText get() = t(
    "This replaces all records on this phone with the ones in the backup file.",
    "سيتم استبدال جميع السجلات على هذا الهاتف بما في ملف النسخة الاحتياطية.",
)
val Lang.backupSaved get() = t("Backup saved", "تم حفظ النسخة الاحتياطية")
val Lang.backupRestored get() = t("Backup restored", "تمت الاستعادة بنجاح")
val Lang.backupFailed get() = t("That file couldn't be read as a backup.", "تعذرت قراءة هذا الملف كنسخة احتياطية.")
val Lang.about get() = t("About", "عن التطبيق")
val Lang.aboutText get() = t(
    "Works fully offline. Your records stay on this phone.",
    "يعمل دون إنترنت. سجلاتك محفوظة على هذا الهاتف فقط.",
)

// ---- Notifications ----
fun Lang.reminderText(type: String): Pair<String, String> = when (type) {
    "morning" -> t("Morning athkar", "أذكار الصباح") to t("Start your day with the morning athkar.", "ابدأ يومك بأذكار الصباح.")
    "evening" -> t("Evening athkar", "أذكار المساء") to t("Time for the evening athkar.", "حان وقت أذكار المساء.")
    "quran" -> t("Quran reading", "ورد القرآن") to t("Have you read your Quran pages today?", "هل قرأت وردك من القرآن اليوم؟")
    else -> t("Surah Al-Kahf", "سورة الكهف") to t("It's Friday. Don't forget Surah Al-Kahf.", "إنها الجمعة. لا تنسَ قراءة سورة الكهف.")
}
