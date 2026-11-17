package com.ibadati.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ibadati.app.UiState
import com.ibadati.app.data.Anchors
import com.ibadati.app.data.Freq
import com.ibadati.app.data.Habit
import com.ibadati.app.data.HabitIcons
import com.ibadati.app.data.HabitLinks
import com.ibadati.app.data.HabitTemplates
import com.ibadati.app.data.HabitType
import com.ibadati.app.data.Units
import java.time.DayOfWeek

private const val STEPS = 4

/** The step-by-step "New habit" flow; also used to edit an existing habit (starting at step 2). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitEditor(state: UiState, existing: Habit?, onDismiss: () -> Unit, onSave: (Habit) -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val e = existing
    var step by rememberSaveable { mutableIntStateOf(if (e == null) 0 else 1) }
    var template by rememberSaveable { mutableStateOf(e?.template) }
    var name by rememberSaveable { mutableStateOf(e?.name ?: "") }
    var icon by rememberSaveable { mutableStateOf(e?.icon ?: "⭐") }
    var color by rememberSaveable { mutableIntStateOf(e?.color ?: 0) }
    var type by rememberSaveable { mutableIntStateOf(e?.type ?: HabitType.CHECK) }
    var unit by rememberSaveable {
        mutableStateOf(e?.unit?.let { if (it.isBlank()) "pages" else if (Units.byKey[it] == null) "custom" else it } ?: "pages")
    }
    var target by rememberSaveable { mutableStateOf(e?.target ?: 1.0) }
    var minimum by rememberSaveable { mutableStateOf(e?.minimum ?: 1.0) }
    var link by rememberSaveable { mutableStateOf(e?.link) }
    var freq by rememberSaveable { mutableIntStateOf(e?.freq ?: Freq.DAILY) }
    var days by rememberSaveable { mutableIntStateOf(e?.days ?: Freq.ALL_DAYS) }
    var perWeek by rememberSaveable { mutableIntStateOf(e?.perWeek ?: 3) }
    var goal by rememberSaveable { mutableIntStateOf(e?.goalDays ?: 40) }
    var strikes by rememberSaveable { mutableIntStateOf(e?.strikes ?: 1) }
    var anchor by rememberSaveable { mutableStateOf(e?.anchor ?: "fajr") }
    var anchorText by rememberSaveable { mutableStateOf(e?.anchorText ?: "") }
    var reminder by rememberSaveable { mutableStateOf(e?.reminder ?: false) }
    var remHour by rememberSaveable { mutableIntStateOf(e?.remHour ?: 8) }
    var remMinute by rememberSaveable { mutableIntStateOf(e?.remMinute ?: 0) }
    var customUnit by rememberSaveable { mutableStateOf(e?.unit?.takeIf { it.isNotBlank() && Units.byKey[it] == null } ?: "") }
    var otherGoal by rememberSaveable { mutableStateOf(false) }
    var timePicker by remember { mutableStateOf(false) }
    var tried by remember { mutableStateOf(false) }

    val linkDef = link?.let { HabitLinks.byKey[it] }
    val effectiveType = if (linkDef?.check == true) HabitType.CHECK else type
    val effectiveUnit = if (unit == "custom") customUnit.trim() else unit
    val draft = Habit(
        id = e?.id ?: 0,
        name = name.trim(),
        icon = icon,
        color = color,
        type = effectiveType,
        unit = if (effectiveType == HabitType.CHECK) "" else effectiveUnit,
        target = if (effectiveType == HabitType.CHECK) 1.0 else target,
        minimum = if (effectiveType == HabitType.CHECK) 1.0 else minimum.coerceAtMost(target),
        freq = freq,
        days = if (freq == Freq.DAYS) days.takeIf { it != 0 } ?: Freq.ALL_DAYS else Freq.ALL_DAYS,
        perWeek = perWeek,
        goalDays = goal.coerceAtLeast(1),
        anchor = anchor,
        anchorText = anchorText.trim(),
        strikes = strikes,
        reminder = reminder,
        remHour = remHour,
        remMinute = remMinute,
        link = link,
        template = template,
        pauses = e?.pauses ?: "",
        archived = e?.archived ?: false,
        created = e?.created ?: state.today,
    )
    val stepValid = when (step) {
        1 -> name.isNotBlank() && (effectiveType == HabitType.CHECK || (target > 0 && (unit != "custom" || customUnit.isNotBlank())))
        3 -> anchor != Anchors.CUSTOM || anchorText.isNotBlank()
        else -> true
    }

    fun applyTemplate(key: String?) {
        template = key
        val t = key?.let { HabitTemplates.byKey[it] }
        if (t == null) {
            name = ""; icon = "⭐"; color = 0; type = HabitType.CHECK; unit = "pages"; target = 1.0; minimum = 1.0
            link = null; anchor = "fajr"
        } else {
            name = L.t(t.en, t.ar); icon = t.icon; color = t.color; type = t.type
            unit = if (t.unit.isBlank()) "pages" else t.unit
            target = t.target; minimum = t.minimum; link = t.link; anchor = t.anchor
        }
        step = 1
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Column(
            Modifier
                .fillMaxSize()
                .background(cs.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // Header
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = L.cancel) }
                Column(Modifier.weight(1f)) {
                    Text(if (e == null) L.newHabit else L.editHabit, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(L.stepOf(step + 1, STEPS), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
            }
            LinearProgressIndicator(
                progress = { (step + 1f) / STEPS },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(5.dp).clip(RoundedCornerShape(3.dp)),
                drawStopIndicator = {},
                gapSize = 0.dp,
            )

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                modifier = Modifier.weight(1f),
                label = "step",
            ) { st ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (st) {
                        0 -> {
                            val newOnes = state.habitStates.count { !it.habit.archived && it.roundNumber == 1 && !it.goalReached }
                            if (newOnes >= 2) {
                                SectionCard(container = cs.secondaryContainer.copy(alpha = 0.5f)) {
                                    Text("💡 " + L.manyNewHabits, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text(L.chooseStart, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TemplateTile("✨", L.custom, L.customHint, habitColor(0)) { applyTemplate(null) }
                            HabitTemplates.all.chunked(2).forEach { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    pair.forEach { t ->
                                        val sub = if (t.type == HabitType.CHECK) L.t("Done / not done", "تم / لم يتم") else L.amountWithUnit(t.target, t.unit)
                                        TemplateTile(t.icon, L.t(t.en, t.ar), sub, habitColor(t.color), Modifier.weight(1f)) { applyTemplate(t.key) }
                                    }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        1 -> {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it.take(60) },
                                label = { Text(L.habitName) },
                                singleLine = true,
                                isError = tried && name.isBlank(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            // Link to app data
                            Text(L.linkLabel, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                            LinkPicker(link) { k ->
                                link = k
                                val d = k?.let { HabitLinks.byKey[it] }
                                if (d != null && !d.check) {
                                    type = HabitType.AMOUNT
                                    unit = d.unit.ifBlank { "times" }
                                    if (target <= 0) target = d.target
                                }
                            }
                            if (linkDef?.check != true) {
                                Text(L.habitTypeLabel, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Pill(L.typeCheck, selected = type == HabitType.CHECK, modifier = Modifier.weight(1f)) { if (link == null) type = HabitType.CHECK }
                                    Pill(L.typeAmount, selected = type == HabitType.AMOUNT, modifier = Modifier.weight(1f)) { type = HabitType.AMOUNT }
                                }
                            }
                            if (effectiveType == HabitType.AMOUNT) {
                                if (link == null) {
                                    Text(L.unitLabel, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Units.all.forEach { u -> Pill(L.t(u.en, u.ar), selected = unit == u.key) { unit = u.key } }
                                        Pill(L.customUnit, selected = unit == "custom") { unit = "custom" }
                                    }
                                    if (unit == "custom") {
                                        OutlinedTextField(
                                            value = customUnit,
                                            onValueChange = { customUnit = it.take(20) },
                                            label = { Text(L.customUnitHint) },
                                            singleLine = true,
                                            isError = tried && customUnit.isBlank(),
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                                SectionCard {
                                    AmountField(L.dailyTarget, target) { target = it; if (minimum > it) minimum = it }
                                    AmountField(L.minimumVersion, minimum) { minimum = it.coerceAtMost(target) }
                                    Text(L.minimumHint, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                                }
                                val starter = template?.let { HabitTemplates.byKey[it] }?.target
                                val big = starter != null && target > starter * 2.5
                                Text(
                                    "💡 " + if (big) L.sizeTipStrong else L.sizeTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (big) LocalWorshipColors.current.gold else cs.onSurfaceVariant,
                                    fontWeight = if (big) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            } else {
                                Text("💡 " + L.sizeTip, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                        }

                        2 -> {
                            Text(L.howOften, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Pill(L.everyDay, selected = freq == Freq.DAILY, modifier = Modifier.weight(1f), compact = true) { freq = Freq.DAILY }
                                Pill(L.specificDays, selected = freq == Freq.DAYS, modifier = Modifier.weight(1f), compact = true) { freq = Freq.DAYS }
                                Pill(L.timesPerWeek, selected = freq == Freq.PER_WEEK, modifier = Modifier.weight(1f), compact = true) { freq = Freq.PER_WEEK }
                            }
                            if (freq == Freq.DAYS) {
                                val order = listOf(
                                    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                                    DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    order.forEach { d ->
                                        val bit = 1 shl (d.value - 1)
                                        val on = days and bit != 0
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (on) cs.primary else cs.surfaceContainerHigh)
                                                .clickable { days = days xor bit },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(L.weekdayShort(d), color = if (on) cs.onPrimary else cs.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                            if (freq == Freq.PER_WEEK) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    (1..6).forEach { k -> Pill(L.n(k), selected = perWeek == k, modifier = Modifier.weight(1f), compact = true) { perWeek = k } }
                                }
                                Text(L.perWeekN(perWeek), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }

                            Text(L.goalLength, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(7, 21, 40, 66).forEach { g ->
                                    Pill(L.n(g), selected = goal == g && !otherGoal, modifier = Modifier.weight(1f), compact = true) { goal = g; otherGoal = false }
                                }
                                Pill(L.otherDays, selected = otherGoal || goal !in listOf(7, 21, 40, 66), modifier = Modifier.weight(1.4f), compact = true) { otherGoal = true }
                            }
                            if (otherGoal || goal !in listOf(7, 21, 40, 66)) {
                                OutlinedTextField(
                                    value = goal.toString(),
                                    onValueChange = { v -> parseNumber(v)?.let { goal = it.coerceIn(1, 1000) } },
                                    label = { Text(L.goalLength) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Text(L.daysN(goal), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)

                            Text(L.strikesLabel, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (0..5).forEach { k -> Pill(L.n(k), selected = strikes == k, modifier = Modifier.weight(1f), compact = true) { strikes = k } }
                            }
                            SectionCard(container = cs.surfaceContainerHigh) {
                                Text(L.strikesHint, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        else -> {
                            Text(L.anchorLabel, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                            Text(L.anchorHint, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Anchors.all.forEach { a ->
                                    Pill(L.anchorHeader(a.key, ""), selected = anchor == a.key) { anchor = a.key }
                                }
                                Pill(L.customAnchor, selected = anchor == Anchors.CUSTOM) { anchor = Anchors.CUSTOM }
                            }
                            if (anchor == Anchors.CUSTOM) {
                                OutlinedTextField(
                                    value = anchorText,
                                    onValueChange = { anchorText = it.take(40) },
                                    label = { Text(L.customAnchorHint) },
                                    singleLine = true,
                                    isError = tried && anchorText.isBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            Text(L.iconColor, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                HabitIcons.all.forEach { ic ->
                                    Box(
                                        Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (icon == ic) habitColor(color).copy(alpha = 0.25f) else cs.surfaceContainerHigh)
                                            .border(if (icon == ic) 2.dp else 0.dp, if (icon == ic) habitColor(color) else Color.Transparent, RoundedCornerShape(12.dp))
                                            .clickable { icon = ic },
                                        contentAlignment = Alignment.Center,
                                    ) { Text(ic, fontSize = 20.sp) }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HabitPalette.indices.forEach { i ->
                                    Box(
                                        Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(HabitPalette[i])
                                            .border(3.dp, if (color == i) cs.onSurface else Color.Transparent, CircleShape)
                                            .clickable { color = i },
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(L.reminderLabel, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        L.time(remHour, remMinute),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cs.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { timePicker = true }.padding(vertical = 2.dp),
                                    )
                                }
                                Switch(checked = reminder, onCheckedChange = { reminder = it; if (it) timePicker = true })
                            }

                            // Plan preview
                            HeroCard {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(L.preview, color = Color(0xFFE6C27A), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(draft.icon, fontSize = 26.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Text(L.stackSentence(draft), color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(
                                        listOfNotNull(
                                            L.frequencyText(draft),
                                            L.daysN(draft.goalDays),
                                            if (draft.strikes == 0) L.noStrikes else L.t("${L.n(draft.strikes)} strikes", "${L.n(draft.strikes)} فرص"),
                                            if (draft.type == HabitType.AMOUNT && draft.minimum < draft.target) L.minLabel(L.amountWithUnit(draft.minimum, draft.unit)) else null,
                                            L.linkName(draft.link)?.let { L.autoHint + ": " + it },
                                        ).joinToString(" · "),
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer buttons
            if (step > 0) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (step > (if (e == null) 0 else 1)) {
                        OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) { Text(L.back) }
                    }
                    Button(
                        onClick = {
                            tried = true
                            if (!stepValid) return@Button
                            tried = false
                            if (step < STEPS - 1) step++ else onSave(draft)
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(if (step < STEPS - 1) L.next else L.saveHabit, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (timePicker) {
        TimeDialog(remHour, remMinute, onDismiss = { timePicker = false }) { h, m ->
            remHour = h; remMinute = m; timePicker = false
        }
    }
}

@Composable
private fun TemplateTile(icon: String, title: String, sub: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cs.surfaceContainer)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, maxLines = 1)
        }
    }
}

/** − value + with ¼ steps for small amounts; tapping the value lets you type it. */
@Composable
private fun AmountField(label: String, value: Double, onChange: (Double) -> Unit) {
    val L = LocalLang.current
    var typing by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    val step = if (value <= 2) 0.25 else if (value < 20) 1.0 else 5.0
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onChange((value - step).coerceAtLeast(0.25)) }) { Text("−", fontWeight = FontWeight.Bold) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (typing) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { t ->
                            text = t.take(8)
                            parseDecimal(text)?.takeIf { it > 0 }?.let(onChange)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(120.dp),
                    )
                } else {
                    Text(
                        L.amt(value),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { typing = true; text = "" }.padding(horizontal = 16.dp),
                    )
                }
            }
            OutlinedButton(onClick = { onChange(value + step) }) { Text("+", fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkPicker(current: String?, onPick: (String?) -> Unit) {
    val L = LocalLang.current
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { open = true }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            L.linkName(current) ?: L.linkNone,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (current != null) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f),
        )
        Text("▾", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (open) {
        AppDialog(title = L.linkLabel, onDismiss = { open = false }, buttons = { TextButton(onClick = { open = false }) { Text(L.cancel) } }) {
            Column(Modifier.height(380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinkOption(L.linkNone, current == null) { onPick(null); open = false }
                HabitLinks.all.forEach { d ->
                    LinkOption(L.t(d.en, d.ar), current == d.key) { onPick(d.key); open = false }
                }
            }
        }
    }
}

@Composable
private fun LinkOption(text: String, selected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) cs.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
    )
}
