package com.ibadati.app.ui

import com.ibadati.app.data.Anchors
import com.ibadati.app.data.Freq
import com.ibadati.app.data.Habit
import com.ibadati.app.data.HabitLinks
import com.ibadati.app.data.HabitTemplates
import com.ibadati.app.data.HabitType
import com.ibadati.app.data.Units
import java.time.DayOfWeek
import kotlin.math.abs
import kotlin.math.floor

/*
 * All text for the Habits feature, English and Arabic side by side.
 */

// ---- Numbers ----

/** 0.5 -> "½", 1.25 -> "1¼", 3 -> "3", 2.4 -> "2.4". */
fun Lang.amt(v: Double): String {
    val whole = floor(v + 1e-9).toLong()
    val f = v - whole
    val frac = when {
        abs(f) < 0.01 -> ""
        abs(f - 0.25) < 0.01 -> "¼"
        abs(f - 0.5) < 0.01 -> "½"
        abs(f - 0.75) < 0.01 -> "¾"
        else -> null
    }
    if (frac == null) return dec(v)
    return if (whole == 0L && frac.isNotEmpty()) frac else digits(whole.toString()) + frac
}

fun Lang.unitName(unit: String): String = Units.byKey[unit]?.let { t(it.en, it.ar) } ?: unit

/** "½ page", "10 minutes", "100 times". */
fun Lang.amountWithUnit(v: Double, unit: String): String {
    val u = Units.byKey[unit]
    if (u == null) return if (unit.isBlank()) amt(v) else "${amt(v)} $unit"
    if (!ar) {
        val single = abs(v - 1.0) < 0.01 || v < 1.0
        val name = when {
            single && unit == "pages" -> "page"
            single && unit == "ayahs" -> "ayah"
            single && unit == "minutes" -> "minute"
            single && unit == "times" -> "time"
            single && unit == "rakaat" -> "rak'ah"
            else -> u.en
        }
        return "${amt(v)} $name"
    }
    val sing = mapOf("pages" to "صفحة", "ayahs" to "آية", "minutes" to "دقيقة", "times" to "مرة", "rakaat" to "ركعة")
    val plural = mapOf("pages" to "صفحات", "ayahs" to "آيات", "minutes" to "دقائق", "times" to "مرات", "rakaat" to "ركعات")
    val isWhole = abs(v - Math.round(v)) < 0.01
    val n = Math.round(v).toInt()
    return when {
        !isWhole || v < 1 -> "${amt(v)} ${sing[unit]}"
        n in 3..10 -> "${amt(v)} ${plural[unit]}"
        else -> "${amt(v)} ${sing[unit]}"
    }
}

fun Lang.habitTarget(h: Habit, target: Double = h.target): String =
    if (h.type == HabitType.CHECK) t("Once", "مرة واحدة") else amountWithUnit(target, h.unit)

fun Lang.anchorName(h: Habit): String =
    if (h.anchor == Anchors.CUSTOM) h.anchorText.ifBlank { t("my own anchor", "موعد أختاره") }
    else Anchors.byKey[h.anchor]?.let { t(it.en, it.ar) } ?: h.anchor

/** "After praying Fajr" — group headers in My Day. */
fun Lang.anchorHeader(key: String, text: String): String {
    if (key == Anchors.CUSTOM) return t("After ", "بعد ") + text.ifBlank { t("my own anchor", "موعد أختاره") }
    val a = Anchors.byKey[key] ?: return key
    return if (key == "sleep") t("Before sleep", "قبل النوم") else t("After ${a.en}", "بعد ${a.ar}")
}

/** "After I pray Fajr, I will memorize ½ page." */
fun Lang.stackSentence(h: Habit): String {
    val anchor = when {
        h.anchor == Anchors.CUSTOM -> h.anchorText.ifBlank { t("…", "…") }
        h.anchor == "sleep" -> null
        else -> Anchors.byKey[h.anchor]?.let { t(it.en.replace("praying", "pray").replace("waking", "wake").replace("arriving", "arrive"), it.ar) }
    }
    val what = if (h.type == HabitType.CHECK) h.name else "${h.name} · ${amountWithUnit(h.target, h.unit)}"
    return when {
        anchor == null -> t("Before I sleep, I will: $what", "قبل أن أنام سأقوم بـ: $what")
        else -> t("After I $anchor, I will: $what", "بعد $anchor سأقوم بـ: $what")
    }
}

fun Lang.templateName(key: String?): String? = key?.let { HabitTemplates.byKey[it] }?.let { t(it.en, it.ar) }

fun Lang.linkName(key: String?): String? = key?.let { HabitLinks.byKey[it] }?.let { t(it.en, it.ar) }

fun Lang.frequencyText(h: Habit): String = when (h.freq) {
    Freq.DAYS -> DayOfWeek.values().filter { (h.days shr (it.value - 1)) and 1 == 1 }.joinToString(t(", ", "، ")) { weekday(it) }
    Freq.PER_WEEK -> t("${n(h.perWeek)}× a week", "${n(h.perWeek)} مرات في الأسبوع")
    else -> t("Every day", "كل يوم")
}

fun Lang.habitReminder(h: Habit): Pair<String, String> =
    "${h.icon} ${h.name}" to stackSentence(h)

// ---- Habits tab ----
val Lang.tabHabits get() = t("Habits", "العادات")
val Lang.themeHadith get() = "«أحبُّ الأعمالِ إلى اللهِ أدومُها وإن قلَّ»"
val Lang.themeHadithEn get() = t(
    "“The most beloved deeds to Allah are the most consistent, even if small.” (Bukhari & Muslim)",
    "متفق عليه",
)
fun Lang.habitsDoneToday(a: Int, b: Int) = t("${n(a)} of ${n(b)} done today", "أنجزت ${n(a)} من ${n(b)} اليوم")
val Lang.noHabitsDue get() = t("Nothing due on this day", "لا عادات مجدولة في هذا اليوم")
val Lang.myDay get() = t("My Day", "يومي")
val Lang.newHabit get() = t("New habit", "عادة جديدة")
val Lang.noHabitsYet get() = t(
    "Start with one small habit you can keep even on your busiest day.",
    "ابدأ بعادة صغيرة واحدة تستطيع المداومة عليها حتى في أكثر أيامك انشغالًا.",
)
val Lang.pausedHabits get() = t("Paused", "متوقفة مؤقتًا")
val Lang.archivedHabits get() = t("Archived", "مؤرشفة")
val Lang.notToday get() = t("Not scheduled on this day", "غير مجدولة في هذا اليوم")
fun Lang.streakDays(c: Int) = t("🔥 ${n(c)}", "🔥 ${n(c)}")
fun Lang.levelN(c: Int) = t("Level ${n(c)}", "المستوى ${n(c)}")
fun Lang.strikesUsed(a: Int, b: Int) = t("${n(a)} of ${n(b)} strikes used", "استُخدمت ${n(a)} من ${n(b)} فرص")
val Lang.noStrikes get() = t("No strikes · a miss restarts", "بلا فرص · أي تفويت يعيد البدء")
fun Lang.dayXofY(a: Int, b: Int) = t("Day ${n(a)} of ${n(b)}", "اليوم ${n(a)} من ${n(b)}")
fun Lang.minLabel(v: String) = t("min $v", "الأدنى $v")
val Lang.minimumDone get() = t("Minimum ✓", "الحد الأدنى ✓")
val Lang.auto get() = t("Auto", "تلقائي")
val Lang.autoHint get() = t("Completes automatically from", "يكتمل تلقائيًا من")
val Lang.goalReachedBanner get() = t("🎉 Goal reached! Choose your next step", "🎉 بلغت الهدف! اختر خطوتك التالية")
val Lang.tipOfDay get() = t("Tip of the day", "نصيحة اليوم")
val Lang.allTips get() = t("All tips", "كل النصائح")
val Lang.hide get() = t("Hide", "إخفاء")
val Lang.next get() = t("Next", "التالي")
val Lang.startBuilding get() = t("Start building", "لنبدأ")
fun Lang.nextHabitNudge(name: String) = t("Next: $name", "التالي: $name")
val Lang.recentMissTip get() = t(
    "A slip happened. That's normal. Just do today's, even the minimum.",
    "حدث تقصير، وهذا طبيعي. فقط أنجز عادة اليوم ولو بالحد الأدنى.",
)

// ---- Amount entry ----
val Lang.howMuchToday get() = t("How much did you do?", "كم أنجزت؟")
val Lang.target get() = t("Target", "الهدف")
val Lang.minimum get() = t("Minimum", "الحد الأدنى")
val Lang.markDone get() = t("Done", "تم")
val Lang.markMinimum get() = t("Did the minimum", "أنجزت الحد الأدنى")
val Lang.notDone get() = t("Not done", "لم أنجز")

// ---- Editor ----
val Lang.editHabit get() = t("Edit habit", "تعديل العادة")
fun Lang.stepOf(a: Int, b: Int) = t("Step ${n(a)} of ${n(b)}", "الخطوة ${n(a)} من ${n(b)}")
val Lang.chooseStart get() = t("Start from a template or make your own", "ابدأ من قالب أو أنشئ عادتك الخاصة")
val Lang.custom get() = t("Custom", "مخصصة")
val Lang.customHint get() = t("Anything you want to build", "أي عادة تريد بناءها")
val Lang.manyNewHabits get() = t(
    "You're already building a few new habits. It often works best to build one at a time and add the next once it feels automatic. It's your choice.",
    "لديك بالفعل عدة عادات جديدة قيد البناء. غالبًا ما يكون الأفضل بناء عادة واحدة في كل مرة، ثم إضافة التالية بعد أن تصبح تلقائية. والقرار لك.",
)
val Lang.habitName get() = t("Habit name", "اسم العادة")
val Lang.habitTypeLabel get() = t("How do you track it?", "كيف تتابعها؟")
val Lang.typeCheck get() = t("Done / not done", "تم / لم يتم")
val Lang.typeAmount get() = t("An amount", "بكمية")
val Lang.unitLabel get() = t("Unit", "الوحدة")
val Lang.customUnit get() = t("Other…", "أخرى…")
val Lang.customUnitHint get() = t("e.g. km, glasses, lessons", "مثلًا: كم، أكواب، دروس")
val Lang.dailyTarget get() = t("Daily target", "الهدف اليومي")
val Lang.minimumVersion get() = t("Minimum version (hard days)", "الحد الأدنى (للأيام الصعبة)")
val Lang.minimumHint get() = t(
    "On hard days, doing just this still keeps your streak. Never zero.",
    "في الأيام الصعبة يكفيك هذا للحفاظ على سلسلتك. لا تجعلها صفرًا.",
)
val Lang.sizeTip get() = t(
    "Choose an amount you could do even on your busiest, most tired day.",
    "اختر قدرًا تستطيع فعله حتى في أكثر أيامك انشغالًا وتعبًا.",
)
val Lang.sizeTipStrong get() = t(
    "This looks like a big start. Starting smaller makes it far more likely to last. You can always level up later.",
    "تبدو هذه بداية كبيرة. البداية الأصغر أدعى للاستمرار، ويمكنك زيادتها لاحقًا.",
)
val Lang.linkLabel get() = t("Complete automatically from app data", "الإكمال تلقائيًا من بيانات التطبيق")
val Lang.linkNone get() = t("No, I'll mark it myself", "لا، سأسجلها بنفسي")
val Lang.howOften get() = t("How often?", "كم مرة؟")
val Lang.everyDay get() = t("Every day", "كل يوم")
val Lang.specificDays get() = t("Specific days", "أيام محددة")
val Lang.timesPerWeek get() = t("Times a week", "مرات في الأسبوع")
fun Lang.perWeekN(c: Int) = t("${n(c)} times a week", "${n(c)} مرات في الأسبوع")
val Lang.goalLength get() = t("Goal length", "مدة الهدف")
fun Lang.daysN(c: Int) = t("${n(c)} days", "${n(c)} يومًا")
val Lang.otherDays get() = t("Other…", "مدة أخرى…")
val Lang.strikesLabel get() = t("Strikes allowed before the streak resets", "عدد الفرص قبل أن تبدأ السلسلة من جديد")
val Lang.strikesHint get() = t(
    "0 means one miss restarts the goal. With strikes, you can miss that many days in a round and keep going.",
    "صفر يعني أن تفويت يوم واحد يعيد الهدف من البداية. ومع الفرص يمكنك تفويت هذا العدد من الأيام في الجولة والاستمرار.",
)
val Lang.anchorLabel get() = t("Stack it: after what will you do it?", "اربطها: بعد ماذا ستفعلها؟")
val Lang.anchorHint get() = t(
    "Attach the new habit to something you already do every day, like a prayer.",
    "اربط العادة الجديدة بشيء تفعله كل يوم، مثل الصلاة.",
)
val Lang.customAnchor get() = t("My own…", "موعد آخر…")
val Lang.customAnchorHint get() = t("e.g. my morning coffee", "مثلًا: قهوة الصباح")
val Lang.iconColor get() = t("Icon & color", "الأيقونة واللون")
val Lang.reminderLabel get() = t("Daily reminder", "تذكير يومي")
val Lang.preview get() = t("Your plan", "خطتك")
val Lang.saveHabit get() = t("Save habit", "حفظ العادة")

// ---- Detail ----
val Lang.currentStreakLabel get() = t("Current streak", "السلسلة الحالية")
val Lang.bestStreakLabel get() = t("Best streak", "أفضل سلسلة")
val Lang.strength get() = t("Strength", "قوة العادة")
val Lang.totalDaysLabel get() = t("Days done", "أيام الإنجاز")
val Lang.totalAmountLabel get() = t("Total", "المجموع")
val Lang.strikesLabelShort get() = t("Strikes", "الفرص")
val Lang.levelLadder get() = t("Level ladder", "سُلَّم المستويات")
fun Lang.roundN(c: Int) = t("Round ${n(c)}", "الجولة ${n(c)}")
fun Lang.since(d: String) = t("since $d", "منذ $d")
val Lang.pause get() = t("Pause", "إيقاف مؤقت")
val Lang.resume get() = t("Resume", "استئناف")
val Lang.archive get() = t("Archive", "أرشفة")
val Lang.unarchive get() = t("Restore", "استعادة")
val Lang.deleteHabitText get() = t("This removes the habit and all its history.", "سيتم حذف العادة وكل سجلها.")
val Lang.legendDone get() = t("Done", "تم")
val Lang.legendMin get() = t("Minimum", "الحد الأدنى")
val Lang.legendMiss get() = t("Missed", "فائت")
val Lang.legendOff get() = t("Not scheduled", "غير مجدول")
val Lang.legendPaused get() = t("Paused", "متوقف")
val Lang.strengthHint get() = t(
    "Based on your consistency in recent weeks. One slip barely moves it.",
    "مبنية على مداومتك في الأسابيع الأخيرة، ولا يؤثر فيها تقصير واحد إلا قليلًا.",
)

// ---- Goal reached ----
val Lang.goalReachedTitle get() = t("Goal reached!", "بلغت هدفك!")
fun Lang.goalReachedText(days: Int, name: String) = t(
    "You kept “$name” for ${daysCount(days)}. It is becoming part of who you are. What's next?",
    "داومت على «$name» مدة ${daysCount(days)}، وأصبحت جزءًا منك. ما خطوتك التالية؟",
)
val Lang.stayLevel get() = t("Stay at this level", "البقاء على هذا المستوى")
val Lang.stayLevelHint get() = t("Another round with the same target", "جولة أخرى بنفس الهدف")
val Lang.levelUp get() = t("Level up", "رفع المستوى")
val Lang.levelUpHint get() = t("A small, sustainable step up", "زيادة صغيرة يمكن المداومة عليها")
val Lang.makeEasier get() = t("Make it easier", "تخفيف العادة")
val Lang.makeEasierHint get() = t("If it felt heavy, ease it. That's wisdom, not failure.", "إن شعرت بثقلها فخفّفها، فهذا حكمة لا فشل.")
val Lang.newTarget get() = t("New target", "الهدف الجديد")
val Lang.newMinimum get() = t("New minimum", "الحد الأدنى الجديد")
val Lang.confirm get() = t("Confirm", "تأكيد")
val Lang.later get() = t("Later", "لاحقًا")

// ---- Reports ----
val Lang.habitsReport get() = t("Habits", "العادات")
fun Lang.completionOf(p: String) = t("$p completed", "$p إنجاز")

// ---- Tips ----
data class Tip(val title: String, val body: String)

val Lang.tips: List<Tip>
    get() = listOf(
        Tip(
            t("Small and consistent", "قليل دائم"),
            t(
                "“The most beloved deeds to Allah are the most consistent, even if small.” (Bukhari & Muslim). A little every day beats a lot for a few days.",
                "«أحبُّ الأعمالِ إلى اللهِ أدومُها وإن قلَّ» متفق عليه. القليل كل يوم خير من الكثير لأيام ثم الانقطاع.",
            ),
        ),
        Tip(
            t("Start small", "ابدأ صغيرًا"),
            t(
                "Choose an amount you could do on your worst, busiest day. Half a page you keep is better than five pages you drop.",
                "اختر قدرًا تستطيعه في أسوأ أيامك وأكثرها انشغالًا. نصف صفحة تداوم عليها خير من خمس صفحات تتركها.",
            ),
        ),
        Tip(
            t("Take what you can bear", "خذوا ما تطيقون"),
            t(
                "“Take on only the deeds you are able to do.” (Bukhari). Pace yourself so you can keep going.",
                "«خذوا من الأعمال ما تطيقون» رواه البخاري. وازن نفسك حتى تستطيع الاستمرار.",
            ),
        ),
        Tip(
            t("Stack your habits", "اربط عاداتك"),
            t(
                "Attach each new habit to something you already do. The five prayers are perfect anchors: “After I pray Fajr, I will memorize ½ page.”",
                "اربط كل عادة جديدة بشيء تفعله يوميًا. الصلوات الخمس أفضل المواعيد: «بعد صلاة الفجر سأحفظ نصف صفحة».",
            ),
        ),
        Tip(
            t("Routine beats motivation", "الروتين أقوى من الحماس"),
            t(
                "Motivation comes and goes. A routine carries you on the days motivation doesn't show up.",
                "الحماس يأتي ويذهب، أما الروتين فيحملك في الأيام التي يغيب فيها الحماس.",
            ),
        ),
        Tip(
            t("Never zero", "لا تجعلها صفرًا"),
            t(
                "On hard days, do the minimum version. Three ayahs still count, and they keep the habit alive.",
                "في الأيام الصعبة أنجز الحد الأدنى. ثلاث آيات تُحتسب وتُبقي العادة حيّة.",
            ),
        ),
        Tip(
            t("After a miss", "بعد التقصير"),
            t(
                "Missing a day doesn't undo your progress. Don't feel defeated. Simply get back on track tomorrow.",
                "تفويت يوم لا يمحو ما بنيته. لا تشعر بالهزيمة، فقط عُد إلى عادتك غدًا.",
            ),
        ),
        Tip(
            t("Grow slowly", "تدرّج"),
            t(
                "Level up only once the habit feels natural, and by a small step. Slow growth lasts.",
                "ارفع المستوى فقط حين تصبح العادة طبيعية، وبخطوة صغيرة. النمو البطيء يدوم.",
            ),
        ),
    )
