package com.ibadati.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.UiState
import com.ibadati.app.data.Athkar
import com.ibadati.app.data.Prayers
import com.ibadati.app.logic.Point
import com.ibadati.app.logic.Range
import com.ibadati.app.logic.Report
import com.ibadati.app.logic.Reports
import com.ibadati.app.logic.Stats
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun ReportsScreen(state: UiState) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    var range by rememberSaveable { mutableStateOf(Range.WEEK) }
    val single = range == Range.TODAY
    val report = remember(state.records, state.today, state.settings, range) {
        Reports.build(state.records, range, state.today, state.settings)
    }
    val streaks = remember(state.records, state.today) { Stats.streaks(state.records, state.today) }

    ScreenColumn {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Range.TODAY to L.rangeToday, Range.WEEK to L.rangeWeek, Range.MONTH to L.rangeMonth, Range.YEAR to L.rangeYear, Range.ALL to L.rangeAll)
                .forEach { (r, label) ->
                    Pill(label, selected = range == r, modifier = Modifier.weight(1f), compact = true) { range = r }
                }
        }

        if (!report.hasData) {
            SectionCard {
                Text(L.noDataYet, style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            return@ScreenColumn
        }

        // ---- Overview: streaks and score
        HeroCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(
                        progress = report.avgScore.toFloat(),
                        modifier = Modifier.size(84.dp),
                        color = Color(0xFFE6C27A),
                        track = Color.White.copy(alpha = 0.15f),
                        stroke = 8.dp,
                    ) {
                        Text(L.pct(report.avgScore), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(L.overallScore, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(L.trackedDays(report.days), color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (single) L.date(report.to) else "${L.shortDate(report.from)} – ${L.shortDate(report.to)}",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StreakBox(L.n(streaks.first), L.currentStreak, Modifier.weight(1f))
                    StreakBox(L.n(streaks.second), L.bestStreak, Modifier.weight(1f))
                }
                Text(L.streakHint, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelSmall)
            }
        }

        // ---- Prayers
        SectionCard(title = L.tabPrayers) {
            val total = report.fardhTotal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Donut(
                    values = listOf(report.mosque, report.jamaa, report.onTime, report.missed, report.notLogged),
                    colors = listOf(wc.mosque, wc.jamaa, wc.onTime, wc.missed, cs.outlineVariant),
                    modifier = Modifier.size(120.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(L.pct(report.frac(report.mosque + report.jamaa + report.onTime, total)), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(L.prayedOnTime, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Legend(wc.mosque, L.statusMosque, L.pct(report.frac(report.mosque, total)))
                    Legend(wc.jamaa, L.statusJamaa, L.pct(report.frac(report.jamaa, total)))
                    Legend(wc.onTime, L.statusOnTime, L.pct(report.frac(report.onTime, total)))
                    Legend(wc.missed, L.statusMissed, L.pct(report.frac(report.missed, total)))
                    Legend(cs.outlineVariant, L.notLogged, L.pct(report.frac(report.notLogged, total)))
                }
            }
            LabeledBar(L.sunnahCompleted, L.pct(report.frac(report.sunnahDone, report.sunnahTotal)), report.frac(report.sunnahDone, report.sunnahTotal))
            Text(L.sunnahRakaatTotal(report.sunnahRakaat), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }

        SectionCard(title = L.sunnahByPrayer) {
            Prayers.all.forEach { def ->
                val (done, possible) = report.sunnahByPrayer[def.index]
                LabeledBar(L.prayerName(def), L.digits("$done/$possible"), report.frac(done, possible), color = cs.primary)
            }
        }

        SectionCard(title = L.witrQiyam) {
            LabeledBar(
                "${L.witr} · ${L.avgRakaat(report.witrAvg)}",
                L.nightsOf(report.witrNights, report.days),
                report.frac(report.witrNights, report.days),
                color = cs.tertiary,
            )
            LabeledBar(
                "${L.qiyam} · ${L.avgRakaat(report.qiyamAvg)}",
                L.nightsOf(report.qiyamNights, report.days),
                report.frac(report.qiyamNights, report.days),
                color = cs.tertiary,
            )
        }

        // ---- Athkar
        SectionCard(title = L.athkarCompletion) {
            Athkar.items.forEach { a ->
                val v = report.athkarDone[a.index]
                LabeledBar(L.athkarName(a), L.pct(report.frac(v, report.days)), report.frac(v, report.days))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(L.minutes(report.duaTotal), L.duaTotal, Modifier.weight(1f), accent = wc.gold)
                StatTile(L.dec(report.duaTotal.toDouble() / report.days), "${L.minShort} ${L.perDay}", Modifier.weight(1f), accent = wc.gold)
            }
        }

        // ---- Quran
        SectionCard(title = L.tabQuran) {
            if (!single) {
                ChartHeader(L.pagesPerDay, report.quran.firstOrNull()?.monthly == true)
                BarChart(report.quran, cs.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(L.n(report.quranTotal), L.totalPages, Modifier.weight(1f))
                StatTile(L.dec(report.quranTotal.toDouble() / report.days), L.dailyAverage, Modifier.weight(1f))
            }
            val k = Stats.khatma(state.records)
            LabeledBar(L.khatmaNumber(k.number), L.pct(k.progress), k.progress, color = wc.gold)
            Text(
                "${L.khatmaHistory}: ${L.n(k.history.size)}",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant,
            )
            LabeledBar(L.kahfFridays, L.fridaysOf(report.kahfDone, report.fridays), report.frac(report.kahfDone, report.fridays), color = wc.gold)
        }

        // ---- Istighfar & Salawat
        SectionCard(title = L.dhikrHeader) {
            if (!single) {
                ChartHeader(L.istighfar, report.istighfar.firstOrNull()?.monthly == true)
                BarChart(report.istighfar, cs.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(L.n(report.istighfarTotal), L.total, Modifier.weight(1f))
                StatTile(L.n(report.istighfarTotal / report.days), "${L.average} ${L.perDay}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            if (!single) {
                ChartHeader(L.salawat, report.salawat.firstOrNull()?.monthly == true)
                BarChart(report.salawat, wc.gold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(L.n(report.salawatTotal), L.total, Modifier.weight(1f), accent = wc.gold)
                StatTile(L.n(report.salawatTotal / report.days), "${L.average} ${L.perDay}", Modifier.weight(1f), accent = wc.gold)
            }
        }

        // ---- Fasting
        SectionCard(title = L.secFasting) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(L.n(report.fastDays), L.fastingTotal, Modifier.weight(1f), accent = wc.gold)
                StatTile(L.ofN(report.sunnahFastDone, report.sunnahFastAvailable), L.sunnahFasts, Modifier.weight(1f))
            }
            if (report.sunnahFastAvailable > 0) {
                LabeledBar(L.sunnahFasts, L.pct(report.frac(report.sunnahFastDone, report.sunnahFastAvailable)), report.frac(report.sunnahFastDone, report.sunnahFastAvailable), color = wc.gold)
            }
        }

        // ---- Reading
        if (state.records.books.isNotEmpty()) {
            SectionCard(title = L.readingSection) {
                if (!single) {
                    ChartHeader(L.pagesReadBooks, report.reading.firstOrNull()?.monthly == true)
                    BarChart(report.reading, cs.tertiary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(L.n(report.readingTotal), L.pagesReadBooks, Modifier.weight(1f), accent = cs.tertiary)
                    StatTile(L.n(report.booksCompleted), L.booksFinished, Modifier.weight(1f), accent = cs.tertiary)
                }
            }
        }

        // ---- Heatmap
        if (!single) {
            SectionCard(title = L.heatmapTitle) {
                Heatmap(state)
            }
        }
    }
}

@Composable
private fun StreakBox(value: String, label: String, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp),
    ) {
        Text(value, color = Color(0xFFE6C27A), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun Legend(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(96.dp), maxLines = 1)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChartHeader(title: String, monthly: Boolean) {
    val L = LocalLang.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(if (monthly) L.perMonthNote else L.perDayNote, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Ring chart split into colored segments. */
@Composable
private fun Donut(values: List<Int>, colors: List<Color>, modifier: Modifier, content: @Composable () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(values) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(900))
    }
    val total = values.sum().coerceAtLeast(1)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val sw = 14.dp.toPx()
            val tl = Offset(sw / 2, sw / 2)
            val sz = Size(size.width - sw, size.height - sw)
            var start = -90f
            values.forEachIndexed { i, v ->
                val sweep = 360f * v / total * anim.value
                if (sweep > 0f) {
                    drawArc(colors[i], start, (sweep - 1.5f).coerceAtLeast(0.5f), false, tl, sz, style = Stroke(sw, cap = StrokeCap.Butt))
                }
                start += sweep
            }
        }
        content()
    }
}

/** Simple bar chart. Bars follow reading direction (right-to-left in Arabic). */
@Composable
private fun BarChart(points: List<Point>, color: Color) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val anim = remember { Animatable(0f) }
    LaunchedEffect(points) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(800))
    }
    val max = (points.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    val grid = cs.outlineVariant

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(L.t("max ", "الأعلى ") + L.n(max), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(130.dp),
        ) {
            val n = points.size.coerceAtLeast(1)
            val slot = size.width / n
            val barW = (slot * 0.62f).coerceAtMost(28.dp.toPx())
            for (g in 0..2) {
                val y = size.height * g / 2f
                drawLine(grid.copy(alpha = 0.5f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            points.forEachIndexed { i, p ->
                val h = size.height * p.value / max * anim.value
                val cx = slot * i + slot / 2
                val x = if (rtl) size.width - cx else cx
                if (p.value > 0) {
                    drawRoundRect(
                        color = lerp(color, Color.White, 0.08f),
                        topLeft = Offset(x - barW / 2, size.height - h),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(barW / 2.5f, barW / 2.5f),
                    )
                } else {
                    drawCircle(grid, radius = 2.dp.toPx(), center = Offset(x, size.height - 2.dp.toPx()))
                }
            }
        }
        // First, middle and last labels.
        if (points.isNotEmpty()) {
            val first = points.first()
            val last = points.last()
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(pointLabel(first), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                if (points.size > 2) {
                    Text(pointLabel(points[points.size / 2]), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
                Text(pointLabel(last), style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun pointLabel(p: Point): String {
    val L = LocalLang.current
    val d = LocalDate.ofEpochDay(p.key)
    return if (p.monthly) L.digits("${L.monthShort(d.monthValue)} ${d.year % 100}") else L.shortDate(p.key)
}

/** GitHub-style grid of the last 16 weeks; columns are weeks starting Saturday. */
@Composable
private fun Heatmap(state: UiState) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    val base = cs.primary
    val empty = cs.outlineVariant.copy(alpha = 0.5f)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val today = state.today
    val todayDate = LocalDate.ofEpochDay(today)
    // Saturday on or before today = start of the current week.
    val shift = (todayDate.dayOfWeek.value - DayOfWeek.SATURDAY.value + 7) % 7
    val weekStart = today - shift
    val weeks = 16
    val firstDay = weekStart - (weeks - 1) * 7
    val scores = remember(state.records, state.settings, today) {
        (0 until weeks * 7).map { i ->
            val d = firstDay + i
            if (d > today) -2f else Stats.dayScore(state.records, d, state.settings)
        }
    }
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        val gap = 3.dp.toPx()
        val cell = minOf((size.width - gap * (weeks - 1)) / weeks, (size.height - gap * 6) / 7)
        val totalW = cell * weeks + gap * (weeks - 1)
        val offX = (size.width - totalW) / 2
        for (w in 0 until weeks) {
            for (r in 0 until 7) {
                val s = scores[w * 7 + r]
                if (s == -2f) continue
                val col = if (rtl) weeks - 1 - w else w
                val x = offX + col * (cell + gap)
                val y = r * (cell + gap)
                val color = if (s < 0f) empty else lerp(empty, base, 0.25f + 0.75f * s)
                drawRoundRect(color, Offset(x, y), Size(cell, cell), CornerRadius(cell / 4, cell / 4))
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(L.less, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        listOf(-1f, 0f, 0.35f, 0.7f, 1f).forEach { s ->
            Box(
                Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (s < 0f) empty else lerp(empty, base, 0.25f + 0.75f * s)),
            )
        }
        Text(L.more, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
    }
}
