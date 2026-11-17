package com.ibadati.app.ui

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.HabitType
import com.ibadati.app.logic.DayMark
import com.ibadati.app.logic.HabitEngine
import com.ibadati.app.logic.HabitState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitDetail(s: HabitState, state: UiState, vm: MainViewModel, onBack: () -> Unit, onEdit: () -> Unit, onNextStep: () -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val wc = LocalWorshipColors.current
    val h = s.habit
    val color = habitColor(h.color)
    val paused = HabitEngine.isPaused(h)
    var confirmDelete by remember { mutableStateOf(false) }

    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back) }
            Text(L.tabHabits, style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
        }

        HeroCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Text(h.icon, fontSize = 28.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(h.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(L.levelN(s.level) + " · " + L.habitTarget(h, s.round.target), color = Color(0xFFE6C27A), style = MaterialTheme.typography.labelLarge)
                    }
                    ProgressRing(
                        progress = s.progress.toFloat() / s.goal,
                        modifier = Modifier.size(70.dp),
                        color = Color(0xFFE6C27A),
                        track = Color.White.copy(alpha = 0.15f),
                        stroke = 7.dp,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(L.n(s.progress), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(L.digits("/${s.goal}"), color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                }
                Text(L.stackSentence(h), color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                Text(
                    listOfNotNull(L.frequencyText(h), L.linkName(h.link)?.let { L.autoHint + ": " + it }).joinToString(" · "),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (s.goalReached && !h.archived) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(wc.gold.copy(alpha = 0.18f)).clickable(onClick = onNextStep).padding(14.dp),
            ) { Text(L.goalReachedBanner, color = wc.gold, fontWeight = FontWeight.Bold) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(L.daysCount(s.streak), L.currentStreakLabel, Modifier.weight(1f), accent = color)
            StatTile(L.daysCount(s.best), L.bestStreakLabel, Modifier.weight(1f), accent = wc.gold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(L.pct(s.strength), L.strength, Modifier.weight(1f), accent = color)
            StatTile(
                if (h.strikes == 0) "—" else L.digits("${s.strikesUsed}/${h.strikes}"),
                L.strikesLabelShort,
                Modifier.weight(1f),
                accent = if (h.strikes > 0 && s.strikesUsed >= h.strikes) wc.missed else cs.primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(L.n(s.totalDays), L.totalDaysLabel, Modifier.weight(1f))
            if (h.type == HabitType.AMOUNT && !HabitEngine.isLinkedCheck(h)) {
                StatTile(L.amountWithUnit(s.totalAmount, h.unit), L.totalAmountLabel, Modifier.weight(1f))
            } else {
                StatTile(L.n(s.roundNumber), L.t("Round", "الجولة"), Modifier.weight(1f))
            }
        }
        Text(L.strengthHint, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)

        SectionCard {
            HabitCalendar(s, state.today, color)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LegendBox(color, L.legendDone)
                LegendBox(color.copy(alpha = 0.4f), L.legendMin)
                LegendBox(wc.missed, L.legendMiss)
                LegendBox(cs.outlineVariant.copy(alpha = 0.5f), L.legendOff)
                LegendBox(cs.tertiary.copy(alpha = 0.35f), L.legendPaused)
            }
        }

        SectionCard(title = L.levelLadder) {
            // Each level once, with the date it started.
            val steps = s.levels.fold(mutableListOf<com.ibadati.app.data.HabitRound>()) { acc, r ->
                if (acc.isEmpty() || acc.last().level != r.level || acc.last().target != r.target) acc.add(r)
                acc
            }
            steps.forEachIndexed { i, r ->
                val current = i == steps.lastIndex
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape).background(if (current) color else color.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) { Text(L.n(r.level), color = if (current) Color.White else color, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(L.levelN(r.level) + " · " + L.habitTarget(h, r.target), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(L.since(L.date(r.startDay)), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
                if (!current) Box(Modifier.padding(start = 16.dp).width(2.dp).height(14.dp).background(color.copy(alpha = 0.3f)))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text(L.editHabit) }
            if (!h.archived) {
                OutlinedButton(onClick = { vm.setHabitPaused(h, !paused) }, modifier = Modifier.weight(1f)) {
                    Text(if (paused) L.resume else L.pause)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.setHabitArchived(h, !h.archived) }, modifier = Modifier.weight(1f)) {
                Text(if (h.archived) L.unarchive else L.archive)
            }
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.weight(1f)) {
                Text(L.delete, color = wc.missed)
            }
        }
    }

    if (confirmDelete) {
        AppDialog(
            title = L.deleteConfirm(h.name),
            onDismiss = { confirmDelete = false },
            buttons = {
                TextButton(onClick = { confirmDelete = false }) { Text(L.cancel) }
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteHabit(h)
                    onBack()
                }) { Text(L.delete, color = wc.missed, fontWeight = FontWeight.Bold) }
            },
        ) { Text(L.deleteHabitText, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun LegendBox(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun HabitCalendar(s: HabitState, today: Long, color: Color) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val wc = LocalWorshipColors.current
    var monthKey by rememberSaveable(s.habit.id) { mutableStateOf(YearMonth.from(LocalDate.ofEpochDay(today)).toString()) }
    val month = YearMonth.parse(monthKey)
    val order = listOf(
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { monthKey = month.minusMonths(1).toString() }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
        Text(
            L.digits("${L.month(month.monthValue)} ${month.year}"),
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        IconButton(onClick = { monthKey = month.plusMonths(1).toString() }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
    }
    Row(Modifier.fillMaxWidth()) {
        order.forEach {
            Text(L.weekdayShort(it), Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
    }
    val first = month.atDay(1)
    val lead = order.indexOf(first.dayOfWeek)
    val rows = (lead + month.lengthOfMonth() + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in 0 until 7) {
                    val idx = r * 7 + c - lead
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (idx in 0 until month.lengthOfMonth()) {
                            val day = first.plusDays(idx.toLong()).toEpochDay()
                            val m = s.mark(day)
                            val bg = when (m) {
                                DayMark.DONE -> color
                                DayMark.MIN -> color.copy(alpha = 0.4f)
                                DayMark.MISS -> wc.missed
                                DayMark.OFF -> cs.outlineVariant.copy(alpha = 0.5f)
                                DayMark.PAUSED -> cs.tertiary.copy(alpha = 0.35f)
                                DayMark.PENDING -> Color.Transparent
                                DayMark.NONE -> Color.Transparent
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(bg)
                                    .border(
                                        if (day == today) 2.dp else 0.dp,
                                        if (day == today) cs.primary else Color.Transparent,
                                        RoundedCornerShape(9.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    L.n(idx + 1),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (m) {
                                        DayMark.DONE, DayMark.MISS -> Color.White
                                        DayMark.NONE -> cs.onSurface.copy(alpha = 0.35f)
                                        else -> cs.onSurface
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
