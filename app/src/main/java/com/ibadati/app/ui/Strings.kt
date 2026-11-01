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
val Lang.statusMosque get() = t("In the mosque", "في المسجد")
val Lang.statusMosqueHint get() = t("Prayed in congregation at the mosque", "صليتها جماعة في المسجد")
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
    Status.MOSQUE -> statusMosque
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
val Lang.rangeToday get() = t("Today", "اليوم")
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

// ---- Navigation (More) ----
val Lang.tabMore get() = t("More", "المزيد")
val Lang.secChallenge get() = t("40-Day Challenge", "تحدي الأربعين")
val Lang.secFasting get() = t("Fasting", "الصيام")
val Lang.secHifz get() = t("Memorization", "الحفظ")
val Lang.secReading get() = t("Reading", "القراءة")
val Lang.secSins get() = t("Muhasaba", "محاسبة النفس")
val Lang.secChallengeHint get() = t("40 days in the mosque", "أربعون يومًا في المسجد")
val Lang.secFastingHint get() = t("Mondays, Thursdays & White Days", "الاثنين والخميس والأيام البيض")
val Lang.secHifzHint get() = t("Surahs you've memorized", "السور التي حفظتها")
val Lang.secReadingHint get() = t("Books you're reading", "الكتب التي تقرؤها")
val Lang.secSinsHint get() = t("Private · PIN protected", "خاص · محمي برمز")
val Lang.secReportsHint get() = t("Your progress & charts", "تقدمك والرسوم البيانية")

// ---- 40-day challenge ----
val Lang.hadithTranslation get() = t(
    "\u201CWhoever prays to Allah for forty days in congregation, catching the opening takbir, two freedoms are written for him: freedom from the Fire and freedom from hypocrisy.\u201D (Tirmidhi)",
    "",
)
fun Lang.dayOfForty(c: Int) = t("Day ${n(c)} of ${n(40)}", "اليوم ${n(c)} من ${n(40)}")
fun Lang.daysLeft(n: Int) = t("${daysCount(n)} to go", "متبقٍ ${daysCount(n)}")
val Lang.challengeRule get() = t(
    "A day counts when all 5 prayers are marked \u201CIn the mosque\u201D in the Prayers tab. Missing a day restarts the challenge.",
    "يُحتسب اليوم عندما تُسجَّل الصلوات الخمس «في المسجد» في تبويب الصلوات. وإذا فات يومٌ يبدأ التحدي من جديد.",
)
val Lang.todayCounts get() = t("Today counts ✓", "اليوم محتسب ✓")
val Lang.todayPending get() = t("Today: pray all 5 in the mosque", "اليوم: صلِّ الخمس في المسجد")
val Lang.highestStreak get() = t("Highest streak", "أعلى سلسلة")
val Lang.challengesCompleted get() = t("Challenges completed", "تحديات مكتملة")
fun Lang.startedOn(d: String) = t("Started $d", "بدأ في $d")
val Lang.notStarted get() = t("Start today by praying all 5 prayers in the mosque.", "ابدأ اليوم بأداء الصلوات الخمس في المسجد.")
val Lang.challengeDoneTitle get() = t("40 days complete!", "أتممت الأربعين!")
val Lang.challengeDoneText get() = t(
    "May Allah write for you freedom from the Fire and freedom from hypocrisy.",
    "نسأل الله أن يكتب لك براءةً من النار وبراءةً من النفاق.",
)
val Lang.completedOn get() = t("Completed", "اكتمل")
val Lang.ameen get() = t("Ameen", "آمين")

// ---- Fasting ----
val Lang.monday get() = t("Monday", "الاثنين")
val Lang.thursday get() = t("Thursday", "الخميس")
val Lang.whiteDays get() = t("White Days", "الأيام البيض")
val Lang.fasted get() = t("Fasted", "صُمت")
val Lang.fastedThisMonth get() = t("Days fasted this month", "أيام صمتها هذا الشهر")
val Lang.sunnahFasts get() = t("Sunnah fasts", "صيام السنة")
val Lang.nextSunnahFast get() = t("Next Sunnah fast", "صيام السنة القادم")
val Lang.tapDayToMark get() = t("Tap a day to mark that you fasted.", "اضغط على اليوم لتسجيل صيامه.")
val Lang.fastingHadith get() = t(
    "Deeds are presented on Mondays and Thursdays, and I love that my deeds are presented while I am fasting. (Tirmidhi)",
    "«تُعرَضُ الأعمالُ يومَ الاثنينِ والخميسِ، فأُحبُّ أن يُعرَضَ عملي وأنا صائمٌ» رواه الترمذي",
)
val Lang.fastingTotal get() = t("Days fasted", "أيام الصيام")
val Lang.remFasting get() = t("Sunnah fasting (evening before)", "صيام السنة (مساء اليوم السابق)")
fun Lang.ofN(a: Int, b: Int) = digits("$a/$b")

// ---- Memorization ----
val Lang.surahsMemorized get() = t("Surahs", "السور")
val Lang.pagesMemorized get() = t("Pages", "الصفحات")
val Lang.juzMemorized get() = t("Complete juz", "أجزاء كاملة")
val Lang.ofTheQuran get() = t("of the Quran memorized", "من القرآن محفوظ")
val Lang.searchSurah get() = t("Search surahs", "ابحث عن سورة")
val Lang.juzGrid get() = t("Juz progress", "تقدم الأجزاء")
fun Lang.juzN(c: Int) = t("Juz ${n(c)}", "الجزء ${n(c)}")
fun Lang.ayahs(c: Int) = t("${n(c)} ayahs", "${n(c)} آية")
val Lang.memorizedLabel get() = t("Memorized", "محفوظة")
val Lang.tapSurahHint get() = t("Tap a surah to mark it memorized.", "اضغط على السورة لتسجيلها محفوظة.")

// ---- Sins (muhasaba) ----
val Lang.majorSins get() = t("Major sins", "الكبائر")
val Lang.minorSins get() = t("Minor sins", "الصغائر")
val Lang.mySins get() = t("My own items", "بنود أضفتها")
val Lang.createPin get() = t("Create a 4-digit PIN", "أنشئ رمزًا من ٤ أرقام".let { if (arDigits) it else "أنشئ رمزًا من 4 أرقام" })
val Lang.confirmPin get() = t("Enter the PIN again", "أعد إدخال الرمز")
val Lang.enterPin get() = t("Enter your PIN", "أدخل الرمز")
val Lang.wrongPin get() = t("Wrong PIN", "رمز خاطئ")
val Lang.pinMismatch get() = t("The PINs don't match. Try again.", "الرمزان غير متطابقين. حاول مرة أخرى.")
val Lang.pinPrivacy get() = t(
    "This section is private. Only you can open it with your PIN.",
    "هذا القسم خاص، ولا يُفتح إلا برمزك.",
)
val Lang.forgotPin get() = t("Forgot PIN?", "نسيت الرمز؟")
val Lang.resetSinsTitle get() = t("Reset this section?", "إعادة تعيين هذا القسم؟")
val Lang.resetSinsText get() = t(
    "This permanently erases all records in this section and removes the PIN. Your other records are not affected.",
    "سيؤدي هذا إلى حذف جميع سجلات هذا القسم نهائيًا وإزالة الرمز. لن تتأثر سجلاتك الأخرى.",
)
val Lang.erase get() = t("Erase", "حذف")
val Lang.lock get() = t("Lock", "قفل")
val Lang.tawbahVerse get() = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا"
val Lang.tawbahVerseEn get() = t(
    "\u201CSay: O My servants who have transgressed against themselves, do not despair of the mercy of Allah. Indeed, Allah forgives all sins.\u201D (Az-Zumar 53)",
    "(الزمر: ٥٣)".let { if (arDigits) it else "(الزمر: 53)" },
)
val Lang.sinsIntro get() = t(
    "Tick what you fell into on this day, then seek forgiveness. Watch the days since each one grow.",
    "حدِّد ما وقعت فيه في هذا اليوم ثم استغفر، وراقب الأيام التي تمضي دونه.",
)
fun Lang.sinCounts(week: Int, month: Int, all: Int) = t(
    "Week ${n(week)} · Month ${n(month)} · All ${n(all)}",
    "الأسبوع ${n(week)} · الشهر ${n(month)} · الكل ${n(all)}",
)
fun Lang.daysSince(d: Int?) = when (d) {
    null -> t("Never", "لم يحدث")
    0 -> t("Today", "اليوم")
    else -> t("${daysCount(d)} since", "منذ ${daysCount(d)}")
}
val Lang.addItem get() = t("Add an item", "إضافة بند")
val Lang.itemName get() = t("Name", "الاسم")
val Lang.isMajor get() = t("Major sin", "من الكبائر")
val Lang.delete get() = t("Delete", "حذف")
fun Lang.deleteConfirm(name: String) = t("Delete \u201C$name\u201D?", "حذف «$name»؟")
val Lang.todayCount get() = t("ticked on this day", "محدد في هذا اليوم")
val Lang.classificationNote get() = t(
    "Based on well-known scholarly lists such as al-Dhahabi's Al-Kaba'ir; scholars differ on the classification of some items.",
    "مستند إلى قوائم العلماء المشهورة ككتاب «الكبائر» للذهبي، وقد يختلف العلماء في تصنيف بعضها.",
)
val Lang.changePin get() = t("Change Muhasaba PIN", "تغيير رمز محاسبة النفس")
val Lang.oldPin get() = t("Current PIN", "الرمز الحالي")
val Lang.newPin get() = t("New PIN", "الرمز الجديد")
val Lang.pinChanged get() = t("PIN changed", "تم تغيير الرمز")
val Lang.noPinYet get() = t("Open the Muhasaba section to create a PIN.", "افتح قسم محاسبة النفس لإنشاء رمز.")

// ---- Reading ----
val Lang.addBook get() = t("Add a book", "إضافة كتاب")
val Lang.editBook get() = t("Edit book", "تعديل الكتاب")
val Lang.bookTitle get() = t("Book title", "اسم الكتاب")
val Lang.bookAuthor get() = t("Author", "المؤلف")
val Lang.bookPages get() = t("Number of pages", "عدد الصفحات")
val Lang.coverPhoto get() = t("Cover photo", "صورة الغلاف")
val Lang.fromGallery get() = t("Gallery", "المعرض")
val Lang.fromCamera get() = t("Camera", "الكاميرا")
val Lang.removePhoto get() = t("Remove", "إزالة")
val Lang.currentlyReading get() = t("Currently reading", "أقرأ حاليًا")
val Lang.completedBooks get() = t("Completed", "كتب مكتملة")
val Lang.noBooks get() = t("Add the book you're reading to start tracking it.", "أضف الكتاب الذي تقرؤه لتبدأ بمتابعته.")
fun Lang.pagesOfTotal(a: Int, b: Int) = t("${n(a)} of ${n(b)} pages", "${n(a)} من ${n(b)} صفحة")
fun Lang.pacePerDay(v: Double) = t("${dec(v)} pages/day", "${dec(v)} صفحة/يوم")
fun Lang.finishBy(d: String) = t("Finish ≈ $d", "الإنهاء ≈ $d")
val Lang.pagesToday get() = t("Pages read on this day", "صفحات هذا اليوم")
val Lang.logPages get() = t("Log pages", "تسجيل الصفحات")
val Lang.markCompleted get() = t("Mark completed", "تحديد كمكتمل")
val Lang.markReading get() = t("Mark as reading", "إعادة إلى القراءة")
fun Lang.finishedOn(d: String) = t("Finished $d", "أُنهي في $d")
val Lang.bookDone get() = t("Book finished!", "أتممت الكتاب!")
val Lang.bookDoneText get() = t("Well done. Keep reading and benefiting.", "أحسنت! واصل القراءة والانتفاع.")
val Lang.deleteBookText get() = t("This removes the book and its reading log.", "سيتم حذف الكتاب وسجل قراءته.")
val Lang.readingSection get() = t("Reading", "القراءة")
val Lang.booksFinished get() = t("Books finished", "كتب مكتملة")
val Lang.pagesReadBooks get() = t("Book pages read", "صفحات الكتب المقروءة")
val Lang.required get() = t("Required", "مطلوب")

fun Lang.fastingReminder(kinds: Set<com.ibadati.app.logic.SunnahFast>): Pair<String, String> {
    val names = buildList {
        if (com.ibadati.app.logic.SunnahFast.MONDAY in kinds) add(monday)
        if (com.ibadati.app.logic.SunnahFast.THURSDAY in kinds) add(thursday)
        if (com.ibadati.app.logic.SunnahFast.WHITE in kinds) add(t("a White Day", "من الأيام البيض"))
    }.joinToString(t(" & ", " و"))
    return t("Sunnah fast tomorrow", "صيام سنة غدًا") to
        t("Tomorrow is $names. Intend to fast, in sha Allah.", "غدًا $names، انوِ الصيام إن شاء الله.")
}
