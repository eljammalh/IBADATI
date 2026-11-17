package com.ibadati.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.Anchors
import com.ibadati.app.data.Habit
import com.ibadati.app.data.HabitType
import com.ibadati.app.logic.DayMark
import com.ibadati.app.logic.HabitEngine
import com.ibadati.app.logic.HabitState

/** Palette for habit colors (index stored in Habit.color). */
val HabitPalette = listOf(
    Color(0xFF0F7A62), Color(0xFF2F6FB0), Color(0xFFC08A1E), Color(0xFF5B4BA8),
    Color(0xFF00897B), Color(0xFF3E8E41), Color(0xFFB0563C), Color(0xFF8E5CC2),
)

fun habitColor(i: Int) = HabitPalette[i.mod(HabitPalette.size)]

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitsScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editing by remember { mutableStateOf<Habit?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var amountFor by remember { mutableStateOf<HabitState?>(null) }
    var celebrate by remember { mutableStateOf<HabitState?>(null) }
    var tipsOpen by remember { mutableStateOf(false) }
    var showArchived by rememberSaveable { mutableStateOf(false) }
    val states = state.habitStates
    val sel = state.selected

    // First visit: the short introduction.
    LaunchedEffect(state.settings.habitTipsSeen) {
        if (!state.settings.habitTipsSeen) tipsOpen = true
    }
    // Celebrate each reached goal once.
    LaunchedEffect(states.map { it.round.id to it.goalReached }) {
        if (celebrate == null) {
            states.firstOrNull { it.goalReached && !it.habit.archived && it.round.id !in state.settings.celebratedRounds && it.round.id != 0L }
                ?.let { celebrate = it }
        }
    }

    val detail = detailId?.let { id -> states.firstOrNull { it.habit.id == id } }
    BackHandler(enabled = detail != null) { detailId = null }

    if (detail != null) {
        HabitDetail(detail, state, vm, onBack = { detailId = null }, onEdit = { editing = detail.habit }, onNextStep = { celebrate = detail })
    } else {
        val active = states.filter { !it.habit.archived && !HabitEngine.isPaused(it.habit) }
        val paused = states.filter { !it.habit.archived && HabitEngine.isPaused(it.habit) }
        val archived = states.filter { it.habit.archived }
        val due = active.filter { HabitEngine.scheduledWeekday(it.habit, sel) && it.mark(sel) != DayMark.NONE }
        val doneCount = due.count { it.mark(sel) == DayMark.DONE || it.mark(sel) == DayMark.MIN }

        ScreenColumn {
            HeroCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(L.themeHadith, fontFamily = Amiri, fontSize = 22.sp, color = Color(0xFFF3E4BE), textAlign = TextAlign.Center)
                    Text(L.themeHadithEn, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                    if (due.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProgressRing(
                                progress = doneCount.toFloat() / due.size,
                                modifier = Modifier.size(54.dp),
                                color = Color(0xFFE6C27A),
                                track = Color.White.copy(alpha = 0.15f),
                                stroke = 6.dp,
                            ) {
                                Text(L.digits("$doneCount/${due.size}"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(L.habitsDoneToday(doneCount, due.size), color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (state.settings.tipDismissedDay != state.today && states.isNotEmpty()) {
                val tip = L.tips[(state.today % L.tips.size).toInt()]
                SectionCard(container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)) {
                    Text("💡 " + L.tipOfDay + " · " + tip.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(tip.body, style = MaterialTheme.typography.bodySmall)
                    Row {
                        TextButton(onClick = { tipsOpen = true }) { Text(L.allTips) }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { vm.dismissTipToday() }) { Text(L.hide) }
                    }
                }
            }

            DateNavigator(sel, state.today, state.settings.showHijri, state.records::hasAnyData, vm::selectDay)

            Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(6.dp))
                Text(L.newHabit)
            }

            if (states.isEmpty()) {
                SectionCard {
                    Text(L.noHabitsYet, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = { tipsOpen = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(L.allTips) }
                }
            }

            if (active.isNotEmpty()) {
                SectionTitle(L.myDay)
                // Grouped by anchor, in the order of the day.
                active.groupBy { it.habit.anchor to (if (it.habit.anchor == Anchors.CUSTOM) it.habit.anchorText.trim() else "") }
                    .toList()
                    .sortedBy { (k, _) -> Anchors.order(k.first) }
                    .forEach { (k, list) ->
                        Text(
                            L.anchorHeader(k.first, k.second),
                            style = MaterialTheme.typography.labelLarge,
                            color = LocalWorshipColors.current.gold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp),
                        )
                        list.forEach { s ->
                            HabitRow(
                                s, sel, state.today,
                                onOpen = { detailId = s.habit.id },
                                onQuick = { quickComplete(s, sel, vm) { amountFor = s } },
                                onMinimum = { vm.setHabitValue(s.habit.id, minValue(s), sel) },
                                onGoal = { celebrate = s },
                            )
                        }
                    }
            }

            if (paused.isNotEmpty()) {
                SectionTitle(L.pausedHabits)
                paused.forEach { s -> HabitRow(s, sel, state.today, onOpen = { detailId = s.habit.id }, onQuick = {}, onMinimum = {}, onGoal = {}) }
            }
            if (archived.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { showArchived = !showArchived }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SectionTitle(L.archivedHabits + " · " + L.n(archived.size))
                    Spacer(Modifier.weight(1f))
                    Icon(if (showArchived) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, null)
                }
                if (showArchived) archived.forEach { s ->
                    HabitRow(s, sel, state.today, onOpen = { detailId = s.habit.id }, onQuick = {}, onMinimum = {}, onGoal = {})
                }
            }
        }
    }

    if (creating || editing != null) {
        HabitEditor(
            state = state,
            existing = editing,
            onDismiss = { creating = false; editing = null },
        ) { h ->
            vm.saveHabit(h)
            creating = false
            editing = null
        }
    }
    amountFor?.let { s ->
        AmountDialog(s, current = state.records.habitLogs[s.habit.id]?.get(sel) ?: 0.0, onDismiss = { amountFor = null }) { v ->
            vm.setHabitValue(s.habit.id, v, sel)
            amountFor = null
        }
    }
    celebrate?.let { s ->
        GoalReachedDialog(
            s,
            onLater = {
                vm.markRoundCelebrated(s.round.id)
                celebrate = null
            },
        ) { delta, target, minimum, goal ->
            vm.markRoundCelebrated(s.round.id)
            vm.nextRound(s, delta, target, minimum, goal)
            celebrate = null
        }
    }
    if (tipsOpen) {
        TipsDialog(onDismiss = {
            tipsOpen = false
            vm.setHabitTipsSeen()
        })
    }
}

private fun minValue(s: HabitState): Double =
    if (s.habit.type == HabitType.CHECK) 0.5 else s.round.minimum

/** Done/not-done habits toggle; amount habits open the amount entry. Linked habits are automatic. */
private fun quickComplete(s: HabitState, day: Long, vm: MainViewModel, openAmount: () -> Unit) {
    if (s.habit.link != null) return
    if (s.habit.type == HabitType.CHECK) {
        val done = s.mark(day) == DayMark.DONE || s.mark(day) == DayMark.MIN
        vm.setHabitValue(s.habit.id, if (done) 0.0 else 1.0, day)
    } else {
        openAmount()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitRow(
    s: HabitState,
    day: Long,
    today: Long,
    onOpen: () -> Unit,
    onQuick: () -> Unit,
    onMinimum: () -> Unit,
    onGoal: () -> Unit,
) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val wc = LocalWorshipColors.current
    val h = s.habit
    val color = habitColor(h.color)
    val mark = s.mark(day)
    val done = mark == DayMark.DONE
    val min = mark == DayMark.MIN
    val scheduled = HabitEngine.scheduledWeekday(h, day)
    val inactive = h.archived || HabitEngine.isPaused(h)
    val bg by animateColorAsState(
        when {
            done -> color.copy(alpha = 0.14f)
            min -> color.copy(alpha = 0.08f)
            else -> cs.surfaceContainer
        },
        label = "habitBg",
    )
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, if (done || min) color.copy(alpha = 0.55f) else cs.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                    Text(h.icon, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(h.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val sub = buildString {
                        append(L.habitTarget(h, s.round.target))
                        if (h.type == HabitType.AMOUNT && s.round.minimum < s.round.target) append(" · " + L.minLabel(L.amt(s.round.minimum)))
                        if (h.link != null) append(" · " + L.auto)
                    }
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (!inactive) {
                    QuickAction(s, day, color, scheduled, onQuick, onMinimum)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = s.progress.toFloat() / s.goal,
                    modifier = Modifier.size(30.dp),
                    color = color,
                    stroke = 4.dp,
                ) {}
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        L.dayXofY(s.progress, s.goal) + "  " + L.streakDays(s.streak) + "  · " + L.levelN(s.level),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        when {
                            !scheduled -> L.notToday
                            h.strikes == 0 -> L.noStrikes
                            else -> L.strikesUsed(s.strikesUsed, h.strikes)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (s.strikesUsed > 0 && s.strikesUsed >= h.strikes) wc.missed else cs.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                if (min) Text(L.minimumDone, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
            if (s.lastReset != null && today - s.lastReset <= 2 && !inactive) {
                Text(L.recentMissTip, style = MaterialTheme.typography.labelSmall, color = wc.gold)
            }
            if (s.goalReached && !inactive) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(wc.gold.copy(alpha = 0.18f))
                        .clickable(onClick = onGoal)
                        .padding(10.dp),
                ) {
                    Text(L.goalReachedBanner, style = MaterialTheme.typography.labelLarge, color = wc.gold, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickAction(s: HabitState, day: Long, color: Color, scheduled: Boolean, onQuick: () -> Unit, onMinimum: () -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val h = s.habit
    val mark = s.mark(day)
    val done = mark == DayMark.DONE
    val min = mark == DayMark.MIN
    val linked = h.link != null
    val pop by animateFloatAsState(if (done) 1f else 0.92f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium), label = "pop")
    if (h.type == HabitType.CHECK || HabitEngine.isLinkedCheck(h)) {
        Box(
            Modifier
                .size(46.dp)
                .scale(pop)
                .clip(CircleShape)
                .background(if (done) color else if (min) color.copy(alpha = 0.35f) else Color.Transparent)
                .border(2.dp, if (done || min) color else cs.outlineVariant, CircleShape)
                .combinedClickable(
                    enabled = !linked,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onQuick()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMinimum()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (done || min) Icon(Icons.Filled.Check, null, tint = Color.White)
            else if (linked) Text(L.auto, fontSize = 9.sp, color = cs.onSurfaceVariant)
        }
    } else {
        val v = when (mark) {
            DayMark.DONE, DayMark.MIN -> true
            else -> false
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(if (done) color else if (min) color.copy(alpha = 0.35f) else cs.surfaceContainerHigh)
                .border(1.dp, if (v) color else cs.outlineVariant, RoundedCornerShape(50))
                .clickable(enabled = !linked, onClick = onQuick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                if (linked) L.auto else if (v) "✓" else "+",
                color = if (v) Color.White else cs.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// ---------------------------------------------------------------- Amount entry

@Composable
fun AmountDialog(s: HabitState, current: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    val L = LocalLang.current
    val h = s.habit
    val step = if (s.round.target <= 2) 0.25 else if (s.round.target < 20) 1.0 else 5.0
    var value by remember { mutableStateOf(if (current > 0) current else s.round.target) }
    var text by remember { mutableStateOf("") }
    AppDialog(
        title = "${h.icon} ${h.name}",
        onDismiss = onDismiss,
        buttons = {
            TextButton(onClick = { onSave(0.0) }) { Text(L.notDone) }
            TextButton(onClick = onDismiss) { Text(L.cancel) }
            TextButton(onClick = { onSave(value) }) { Text(L.save, fontWeight = FontWeight.Bold) }
        },
    ) {
        Text(L.howMuchToday, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            FilledTonalIconButton(onClick = { value = (value - step).coerceAtLeast(0.0); text = "" }, modifier = Modifier.size(48.dp)) {
                Text("−", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.width(150.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(L.amt(value), fontSize = 42.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(L.unitName(h.unit), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalIconButton(onClick = { value += step; text = "" }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Add, null)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(L.target + " · " + L.amt(s.round.target), selected = value == s.round.target, modifier = Modifier.weight(1f), compact = true) {
                value = s.round.target; text = ""
            }
            Pill(L.minimum + " · " + L.amt(s.round.minimum), selected = value == s.round.minimum, modifier = Modifier.weight(1f), compact = true) {
                value = s.round.minimum; text = ""
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { t ->
                text = t.filter { it.isDigit() || it == '.' || it == '٫' || it in '٠'..'٩' }.take(8)
                parseDecimal(text)?.let { value = it }
            },
            label = { Text(L.t("Or type the amount", "أو اكتب الكمية")) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Accepts Western or Arabic-Indic digits with a decimal point. */
fun parseDecimal(s: String): Double? {
    val norm = buildString {
        for (c in s) when (c) {
            in '0'..'9' -> append(c)
            in '٠'..'٩' -> append('0' + (c - '٠'))
            '.', '٫', ',' -> append('.')
        }
    }
    return norm.toDoubleOrNull()
}

// ---------------------------------------------------------------- Goal reached

@Composable
fun GoalReachedDialog(
    s: HabitState,
    onLater: () -> Unit,
    onChoose: (levelDelta: Int, target: Double, minimum: Double, goalDays: Int) -> Unit,
) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val h = s.habit
    val isAmount = h.type == HabitType.AMOUNT && !HabitEngine.isLinkedCheck(h)
    var choice by remember { mutableIntStateOf(-1) } // 0 stay, 1 up, 2 easier
    var target by remember { mutableStateOf(s.round.target) }
    var minimum by remember { mutableStateOf(s.round.minimum) }
    var goal by remember { mutableIntStateOf(s.goal) }

    AppDialog(
        title = "🎉 " + L.goalReachedTitle,
        onDismiss = onLater,
        buttons = {
            TextButton(onClick = onLater) { Text(L.later) }
            if (choice >= 0) {
                TextButton(onClick = {
                    when (choice) {
                        0 -> onChoose(0, s.round.target, s.round.minimum, goal)
                        1 -> onChoose(1, target, minimum, goal)
                        else -> onChoose(if (s.level > 1) -1 else 0, target, minimum, goal)
                    }
                }) { Text(L.confirm, fontWeight = FontWeight.Bold) }
            }
        },
    ) {
        Box(Modifier.fillMaxWidth().height(70.dp), contentAlignment = Alignment.Center) {
            Sparkles(Modifier.fillMaxWidth().height(70.dp))
            Text(h.icon, fontSize = 38.sp)
        }
        Text(L.goalReachedText(s.goal, h.name), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        ChoiceCard("🔁", L.stayLevel, L.stayLevelHint, choice == 0, wc.onTime) { choice = 0 }
        if (isAmount) {
            ChoiceCard("⬆️", L.levelUp, L.levelUpHint, choice == 1, wc.gold) {
                choice = 1
                target = HabitEngine.suggestLevelUp(s.round.target, h.unit)
                minimum = s.round.minimum
            }
            ChoiceCard("🌿", L.makeEasier, L.makeEasierHint, choice == 2, wc.mosque) {
                choice = 2
                target = HabitEngine.suggestEasier(s.round.target)
                minimum = minOf(s.round.minimum, target)
            }
        }
        if (choice == 1 || choice == 2) {
            StepperRow(L.newTarget, target, h.unit) { target = it.coerceAtLeast(0.25); if (minimum > target) minimum = target }
            StepperRow(L.newMinimum, minimum, h.unit) { minimum = it.coerceIn(0.25, target) }
        }
        if (choice >= 0) {
            Text(L.goalLength, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(7, 21, 40, 66).forEach { g -> Pill(L.n(g), selected = goal == g, modifier = Modifier.weight(1f), compact = true) { goal = g } }
            }
        }
    }
}

@Composable
private fun ChoiceCard(emoji: String, title: String, hint: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (selected) 0.2f else 0.07f))
            .border(1.5.dp, color.copy(alpha = if (selected) 0.9f else 0.25f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** A labelled − value + row for amounts (steps of ¼ for small amounts). */
@Composable
fun StepperRow(label: String, value: Double, unit: String, onChange: (Double) -> Unit) {
    val L = LocalLang.current
    val step = if (value < 2 || (value == 2.0)) 0.25 else if (value < 20) 1.0 else 5.0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        FilledTonalIconButton(onClick = { onChange((value - step).coerceAtLeast(0.0)) }, modifier = Modifier.size(38.dp)) {
            Text("−", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Text(
            L.amountWithUnit(value, unit),
            modifier = Modifier.width(110.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        FilledTonalIconButton(onClick = { onChange(value + step) }, modifier = Modifier.size(38.dp)) {
            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
        }
    }
}

// ---------------------------------------------------------------- Tips

@Composable
fun TipsDialog(onDismiss: () -> Unit) {
    val L = LocalLang.current
    val tips = L.tips
    var i by remember { mutableIntStateOf(0) }
    AppDialog(
        title = L.stepOf(i + 1, tips.size).let { "💡 $it" },
        onDismiss = onDismiss,
        buttons = {
            if (i > 0) TextButton(onClick = { i-- }) { Text(L.back) }
            if (i < tips.size - 1) TextButton(onClick = { i++ }) { Text(L.next, fontWeight = FontWeight.Bold) }
            else TextButton(onClick = onDismiss) { Text(L.startBuilding, fontWeight = FontWeight.Bold) }
        },
    ) {
        HeroCard {
            Column(Modifier.fillMaxWidth().height(170.dp), verticalArrangement = Arrangement.Center) {
                Text(tips[i].title, color = Color(0xFFE6C27A), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(tips[i].body, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            tips.indices.forEach { k ->
                Box(
                    Modifier
                        .padding(3.dp)
                        .size(if (k == i) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (k == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
    }
}
