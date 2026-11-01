package com.ibadati.app.data

/** A category of sins, e.g. "The tongue". */
data class SinCategory(val key: String, val en: String, val ar: String)

/**
 * One built-in sin. [key] is stored in the database, so it must never change.
 * [major] = one of the major sins (al-kaba'ir).
 */
data class SinDef(val key: String, val category: String, val major: Boolean, val en: String, val ar: String)

/**
 * The built-in list, for private self-accountability (muhasabah).
 * Major sins follow well-known scholarly lists such as al-Dhahabi's "Al-Kaba'ir";
 * scholars differ on how some items are classified.
 */
object Sins {
    val categories = listOf(
        SinCategory("worship", "Worship & belief", "العقيدة والعبادات"),
        SinCategory("tongue", "The tongue", "اللسان"),
        SinCategory("eyes", "Eyes, ears & chastity", "النظر والسمع والعفة"),
        SinCategory("family", "Family & relations", "الأهل والأرحام والناس"),
        SinCategory("wealth", "Wealth & dealings", "المال والمعاملات"),
        SinCategory("heart", "The heart", "القلب"),
        SinCategory("body", "Conduct & body", "الجوارح والسلوك"),
    )

    val all = listOf(
        // ---- Major sins (الكبائر)
        SinDef("shirk", "worship", true, "Associating partners with Allah (shirk)", "الشرك بالله"),
        SinDef("leave_prayer", "worship", true, "Leaving an obligatory prayer deliberately", "ترك الصلاة المفروضة عمدًا"),
        SinDef("delay_prayer", "worship", true, "Delaying a prayer past its time without excuse", "تأخير الصلاة عن وقتها بغير عذر"),
        SinDef("zakat", "worship", true, "Withholding zakat", "منع الزكاة"),
        SinDef("ramadan", "worship", true, "Breaking a Ramadan fast without excuse", "الفطر في رمضان بغير عذر"),
        SinDef("fortune", "worship", true, "Believing fortune-tellers and soothsayers", "تصديق الكهّان والعرّافين"),
        SinDef("magic", "worship", true, "Practising or seeking magic", "السحر أو طلبه"),
        SinDef("false_witness", "tongue", true, "Giving false testimony", "شهادة الزور"),
        SinDef("false_oath", "tongue", true, "Swearing a false oath", "اليمين الغموس (الحلف كذبًا)"),
        SinDef("lie_religion", "tongue", true, "Lying about Allah or the Prophet ﷺ", "الكذب على الله ورسوله ﷺ"),
        SinDef("slander", "tongue", true, "Slandering chaste people (qadhf)", "قذف المحصنات"),
        SinDef("backbiting", "tongue", true, "Backbiting (ghibah)", "الغيبة"),
        SinDef("talebearing", "tongue", true, "Spreading tales to cause discord (namimah)", "النميمة"),
        SinDef("lying", "tongue", true, "Lying", "الكذب"),
        SinDef("cursing", "tongue", true, "Cursing a Muslim", "لعن المسلم"),
        SinDef("zina", "eyes", true, "Zina (fornication or adultery)", "الزنا"),
        SinDef("parents", "family", true, "Disobeying or mistreating parents", "عقوق الوالدين"),
        SinDef("kinship", "family", true, "Cutting ties of kinship", "قطيعة الرحم"),
        SinDef("oppression", "family", true, "Oppressing or wronging people", "ظلم الناس"),
        SinDef("neighbor", "family", true, "Harming one's neighbour", "إيذاء الجار"),
        SinDef("riba", "wealth", true, "Dealing in riba (usury / interest)", "أكل الربا"),
        SinDef("orphan", "wealth", true, "Consuming an orphan's wealth", "أكل مال اليتيم"),
        SinDef("theft", "wealth", true, "Theft", "السرقة"),
        SinDef("bribery", "wealth", true, "Giving or taking bribes", "الرشوة"),
        SinDef("cheating", "wealth", true, "Cheating and fraud", "الغش والخداع"),
        SinDef("gambling", "wealth", true, "Gambling", "الميسر (القمار)"),
        SinDef("betrayal", "wealth", true, "Betraying a trust", "خيانة الأمانة"),
        SinDef("showing_off", "heart", true, "Showing off in worship (riya')", "الرياء في العبادة"),
        SinDef("arrogance", "heart", true, "Arrogance (kibr)", "الكِبر"),
        SinDef("alcohol", "body", true, "Drinking alcohol", "شرب الخمر"),
        SinDef("drugs", "body", true, "Taking intoxicating drugs", "تعاطي المخدرات"),
        SinDef("killing", "body", true, "Killing a soul unjustly", "قتل النفس بغير حق"),

        // ---- Minor sins (الصغائر)
        SinDef("pass_prayer", "worship", false, "Passing in front of someone praying", "المرور بين يدي المصلي"),
        SinDef("khutbah", "worship", false, "Talking during the Friday khutbah", "الكلام أثناء خطبة الجمعة"),
        SinDef("foul", "tongue", false, "Foul or obscene language", "الفحش والبذاءة في الكلام"),
        SinDef("mocking", "tongue", false, "Mocking or ridiculing others", "السخرية من الآخرين"),
        SinDef("arguing", "tongue", false, "Arguing and quarrelling", "الجدال والمراء"),
        SinDef("promise", "tongue", false, "Breaking a promise", "إخلاف الوعد"),
        SinDef("joke_lie", "tongue", false, "Lying in jest", "الكذب مزاحًا"),
        SinDef("idle_talk", "tongue", false, "Idle, harmful talk", "الخوض في الباطل"),
        SinDef("gaze", "eyes", false, "Looking at what is forbidden", "النظر إلى المحرّمات"),
        SinDef("listen_ghibah", "eyes", false, "Listening to backbiting", "الاستماع إلى الغيبة"),
        SinDef("spying", "eyes", false, "Spying on others", "التجسس على الآخرين"),
        SinDef("harsh_family", "family", false, "Harshness with family", "الغلظة مع الأهل"),
        SinDef("anger", "family", false, "Losing one's temper unjustly", "الغضب لغير الله"),
        SinDef("waste", "wealth", false, "Wastefulness and extravagance", "الإسراف والتبذير"),
        SinDef("debt", "wealth", false, "Delaying a debt while able to pay", "المماطلة في سداد الدَّين مع القدرة"),
        SinDef("envy", "heart", false, "Envy (hasad)", "الحسد"),
        SinDef("suspicion", "heart", false, "Thinking badly of others", "سوء الظن بالآخرين"),
        SinDef("grudge", "heart", false, "Holding grudges", "الحقد"),
        SinDef("vanity", "heart", false, "Self-admiration (ujb)", "العُجب"),
        SinDef("time", "body", false, "Wasting time on what is forbidden", "إضاعة الوقت فيما حرّم الله"),
    )

    val byKey: Map<String, SinDef> = all.associateBy { it.key }

    fun customKey(id: Long) = "c$id"
}
