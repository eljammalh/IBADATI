package com.ibadati.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.Quran
import com.ibadati.app.logic.Stats
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun QuranScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val day = state.day
    val info = Stats.khatma(state.records)
    val suggested = Stats.suggestedStart(state.records, state.selected)
    val avg = Stats.quranDailyAverage(state.records, state.today)
    val goal = state.settings.quranGoal
    var picking by remember { mutableStateOf<String?>(null) }
    var celebrate by remember { mutableStateOf(false) }
    val isFriday = LocalDate.ofEpochDay(state.selected).dayOfWeek == DayOfWeek.FRIDAY

    ScreenColumn {
        DateNavigator(state.selected, state.today, state.settings.showHijri, state.records::hasAnyData, vm::selectDay)

        // Khatma progress
        HeroCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = info.progress.toFloat(),
                    modifier = Modifier.size(118.dp),
                    color = Color(0xFFE6C27A),
                    track = Color.White.copy(alpha = 0.15f),
                    stroke = 11.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(L.pct(info.progress), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(L.khatmaProgress, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, maxLines = 1)
                    }
                }
                Spacer(Modifier.width(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(L.khatmaNumber(info.number), color = Color(0xFFE6C27A), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(L.pageOf(info.maxPage), color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(L.pagesRemaining(info.remaining), color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (info.completed) {
            SectionCard(container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)) {
                Text(L.khatmaFinishedBanner, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { vm.startNewKhatma() }) { Text(L.startNewKhatma) }
            }
        }

        // The selected day's reading
        SectionCard(title = if (state.isToday) L.todaysReading else L.daysReading) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PageButton(
                    label = L.startPage,
                    value = day.quranFrom,
                    placeholder = suggested,
                    modifier = Modifier.weight(1f),
                ) { picking = "from" }
                PageButton(
                    label = L.stopPage,
                    value = day.quranTo,
                    placeholder = null,
                    modifier = Modifier.weight(1f),
                ) { picking = "to" }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(L.pagesRead, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(L.pages(day.quranPages), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                if (day.quranPages > 0) {
                    TextButton(onClick = { vm.clearQuran() }) { Text(L.clear) }
                }
            }
            if (goal > 0) {
                Bar(day.quranPages.toDouble() / goal)
                Text(
                    L.goalOf(L.pages(goal)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(L.n(day.quranPages), L.pagesRead, Modifier.weight(1f))
            StatTile(L.dec(avg), L.dailyAverage, Modifier.weight(1f))
            StatTile(L.n(info.remaining), L.t("Remaining", "المتبقي"), Modifier.weight(1f), accent = wc.gold)
        }

        if (isFriday) {
            SectionCard(container = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("سورة الكهف", fontFamily = Amiri, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = wc.gold)
                        if (!L.ar) Text(L.surahKahf, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(L.kahfHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    WorshipCircle(
                        filled = day.kahf,
                        size = 62.dp,
                        label = if (day.kahf) "✓" else "",
                        labelSize = 26.sp,
                        color = wc.gold,
                    ) { vm.toggleKahf() }
                }
            }
        }

        SectionCard(title = L.khatmaHistory) {
            if (info.history.isEmpty()) {
                Text(L.noKhatmasYet, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                info.history.reversed().forEach { k ->
                    val end = k.endDay ?: k.startDay
                    val number = state.records.khatmas.indexOf(k) + 1
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(38.dp).clip(CircleShape).background(wc.gold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(L.n(number), color = wc.gold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(L.khatmaNumber(number), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                L.khatmaRange(L.shortDate(k.startDay), L.shortDate(end), (end - k.startDay + 1).toInt()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    when (picking) {
        "from" -> NumberPickerDialog(
            title = L.startPage,
            initial = if (day.quranFrom > 0) day.quranFrom else suggested,
            min = 1,
            max = Quran.PAGES,
            onDismiss = { picking = null },
        ) { from ->
            picking = null
            val to = if (day.quranTo >= from) day.quranTo else from
            vm.saveQuran(from, to) { celebrate = true }
        }
        "to" -> NumberPickerDialog(
            title = L.stopPage,
            initial = if (day.quranTo > 0) day.quranTo else (if (day.quranFrom > 0) day.quranFrom else suggested) + (goal - 1).coerceAtLeast(0),
            min = if (day.quranFrom > 0) day.quranFrom else 1,
            max = Quran.PAGES,
            onDismiss = { picking = null },
        ) { to ->
            picking = null
            val from = if (day.quranFrom > 0) day.quranFrom else suggested.coerceAtMost(to)
            vm.saveQuran(from, to) { celebrate = true }
        }
    }

    if (celebrate) KhatmaDialog { celebrate = false }
}

@Composable
private fun PageButton(label: String, value: Int, placeholder: Int?, modifier: Modifier, onClick: () -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cs.surfaceContainerHigh)
            .border(1.dp, if (value > 0) cs.primary.copy(alpha = 0.5f) else cs.outlineVariant, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, maxLines = 1)
        when {
            value > 0 -> Text(L.n(value), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = cs.primary)
            placeholder != null -> Text(L.n(placeholder), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = cs.onSurface.copy(alpha = 0.3f))
            else -> Text("—", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = cs.onSurface.copy(alpha = 0.3f))
        }
        Text(L.tapToChoose, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
    }
}
