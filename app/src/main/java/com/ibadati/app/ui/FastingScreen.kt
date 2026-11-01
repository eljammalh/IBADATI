package com.ibadati.app.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.ibadati.app.logic.Fasting
import com.ibadati.app.logic.SunnahFast
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val MONDAY_COLOR = Color(0xFF3F7FBF)
private val THURSDAY_COLOR = Color(0xFF8E5CC2)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FastingScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    val today = state.today
    val thisMonth = YearMonth.from(LocalDate.ofEpochDay(today))
    var monthKey by rememberSaveable { mutableStateOf(thisMonth.toString()) }
    val month = YearMonth.parse(monthKey)
    val first = month.atDay(1).toEpochDay()
    val last = month.atEndOfMonth().toEpochDay()
    val days = (first..last).toList()
    val fasted = days.count { state.records.day(it).fasted }
    val sunnahDays = days.filter { Fasting.isSunnah(it) }
    val sunnahFasted = sunnahDays.count { state.records.day(it).fasted }
    val next = Fasting.next(if (state.records.day(today).fasted) today + 1 else today)

    ScreenColumn {
        HeroCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = if (sunnahDays.isEmpty()) 0f else sunnahFasted.toFloat() / sunnahDays.size,
                        modifier = Modifier.size(88.dp),
                        color = Color(0xFFE6C27A),
                        track = Color.White.copy(alpha = 0.15f),
                        stroke = 9.dp,
                    ) {
                        Text(L.n(fasted), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(L.fastedThisMonth, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${L.sunnahFasts}: ${L.ofN(sunnahFasted, sunnahDays.size)}",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "${L.nextSunnahFast}: ${if (next == today) L.today else L.weekday(LocalDate.ofEpochDay(next).dayOfWeek) + " " + L.shortDate(next)}",
                            color = Color(0xFFE6C27A),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(L.fastingHadith, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall, fontFamily = if (L.ar) Amiri else Plex)
            }
        }

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { monthKey = month.minusMonths(1).toString() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(L.digits("${L.month(month.monthValue)} ${month.year}"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val h1 = L.hijri(first)?.substringAfter(" ")
                    val h2 = L.hijri(last)?.substringAfter(" ")
                    if (h1 != null && h2 != null) {
                        Text(
                            if (h1 == h2) h1 else "$h1 – $h2",
                            style = MaterialTheme.typography.labelSmall,
                            color = wc.gold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                IconButton(onClick = { monthKey = month.plusMonths(1).toString() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
            MonthGrid(month, today, state, onToggle = vm::toggleFast)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LegendDot(MONDAY_COLOR, L.monday)
                LegendDot(THURSDAY_COLOR, L.thursday)
                LegendDot(wc.gold, L.whiteDays)
                LegendDot(wc.mosque, L.fasted, filled = true)
            }
            Text(L.tapDayToMark, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, filled: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (filled) color else color.copy(alpha = 0.18f))
                .border(1.5.dp, color, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MonthGrid(month: YearMonth, today: Long, state: UiState, onToggle: (Long) -> Unit) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val order = listOf(
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
    )
    Row(Modifier.fillMaxWidth()) {
        order.forEach {
            Text(
                L.weekdayShort(it),
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = when (it) {
                    DayOfWeek.MONDAY -> MONDAY_COLOR
                    DayOfWeek.THURSDAY -> THURSDAY_COLOR
                    else -> cs.onSurfaceVariant
                },
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
    val firstDate = month.atDay(1)
    val lead = order.indexOf(firstDate.dayOfWeek)
    val rows = (lead + month.lengthOfMonth() + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in 0 until 7) {
                    val idx = r * 7 + c - lead
                    Box(Modifier.weight(1f).aspectRatio(0.82f), contentAlignment = Alignment.Center) {
                        if (idx in 0 until month.lengthOfMonth()) {
                            val day = firstDate.plusDays(idx.toLong()).toEpochDay()
                            DayCell(day, today, state.records.day(day).fasted, onToggle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Long, today: Long, fasted: Boolean, onToggle: (Long) -> Unit) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    val kinds = Fasting.kinds(day)
    val accent = when {
        SunnahFast.WHITE in kinds -> wc.gold
        SunnahFast.MONDAY in kinds -> MONDAY_COLOR
        SunnahFast.THURSDAY in kinds -> THURSDAY_COLOR
        else -> null
    }
    val future = day > today
    val bg by animateColorAsState(
        when {
            fasted -> wc.mosque
            accent != null -> accent.copy(alpha = 0.14f)
            else -> cs.surfaceContainerHigh.copy(alpha = 0.6f)
        },
        label = "fastBg",
    )
    val date = LocalDate.ofEpochDay(day)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                when {
                    day == today -> 2.dp
                    accent != null && !fasted -> 1.dp
                    else -> 0.dp
                },
                when {
                    day == today -> cs.primary
                    accent != null -> accent.copy(alpha = 0.7f)
                    else -> Color.Transparent
                },
                RoundedCornerShape(12.dp),
            )
            .clickable(enabled = !future) { onToggle(day) }
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            if (fasted) "✓" else L.n(date.dayOfMonth),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                fasted -> Color.White
                future -> cs.onSurface.copy(alpha = 0.45f)
                else -> cs.onSurface
            },
        )
        Text(
            L.n(Fasting.hijriDay(day) ?: 0),
            fontSize = 10.sp,
            color = if (fasted) Color.White.copy(alpha = 0.85f) else accent ?: cs.onSurfaceVariant,
            fontWeight = if (SunnahFast.WHITE in kinds) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
