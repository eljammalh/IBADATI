package com.ibadati.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.PrayerLog
import com.ibadati.app.data.Prayers
import com.ibadati.app.data.Status

@Composable
fun PrayersScreen(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    var statusFor by remember { mutableStateOf<Int?>(null) }
    var rakaatFor by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }
    var witrPicker by remember { mutableStateOf(false) }
    var qiyamPicker by remember { mutableStateOf(false) }

    ScreenColumn {
        DateNavigator(state.selected, state.today, state.settings.showHijri, state.records::hasAnyData, vm::selectDay)
        PrayerSummary(state)

        SectionTitle(L.dailyPrayers)
        Prayers.all.forEach { def ->
            PrayerRow(
                name = L.prayerName(def),
                log = state.prayer(def.index),
                hasBefore = def.before > 0,
                hasAfter = def.after > 0,
                fardhRakaat = def.fardh,
                flexible = def.flexible,
                onFardh = { vm.toggleFardh(def.index) },
                onBefore = { vm.toggleSunnahBefore(def.index) },
                onAfter = { vm.toggleSunnahAfter(def.index) },
                onLongBefore = { rakaatFor = def.index to true },
                onLongAfter = { rakaatFor = def.index to false },
                onStatus = { statusFor = def.index },
            )
        }
        Text(
            L.dhuhrTip,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp),
        )

        SectionTitle(L.nightPrayers)
        NightPrayerRow(L.witr, L.witrHint, state.day.witr) { witrPicker = true }
        NightPrayerRow(L.qiyam, L.qiyamHint, state.day.qiyam) { qiyamPicker = true }
    }

    statusFor?.let { i ->
        StatusDialog(L.prayerName(Prayers.all[i]), state.prayer(i).status, onDismiss = { statusFor = null }) {
            vm.setStatus(i, it)
            statusFor = null
        }
    }
    rakaatFor?.let { (i, before) ->
        val log = state.prayer(i)
        ChoiceDialog(
            title = L.sunnahRakaatTitle(before),
            subtitle = null,
            options = listOf(2, 4),
            current = if (before) log.beforeRakaat else log.afterRakaat,
            allowNone = false,
            onDismiss = { rakaatFor = null },
        ) {
            vm.setSunnahRakaat(i, before, it)
            rakaatFor = null
        }
    }
    if (witrPicker) {
        ChoiceDialog(L.chooseRakaat(L.witr), L.witrHint, Prayers.WITR_OPTIONS, state.day.witr, true, { witrPicker = false }) {
            vm.setWitr(it)
            witrPicker = false
        }
    }
    if (qiyamPicker) {
        ChoiceDialog(L.chooseRakaat(L.qiyam), L.qiyamHint, Prayers.QIYAM_OPTIONS, state.day.qiyam, true, { qiyamPicker = false }) {
            vm.setQiyam(it)
            qiyamPicker = false
        }
    }
}

@Composable
private fun PrayerSummary(state: UiState) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val logs = (0..4).map { state.prayer(it) }
    val prayed = logs.count { it.fardh }
    val jamaa = logs.count { it.status == Status.JAMAA }
    val onTime = logs.count { it.status == Status.ON_TIME }
    val missed = logs.count { it.status == Status.MISSED }
    val sunnah = Prayers.all.sumOf { d ->
        val p = logs[d.index]
        (if (d.before > 0 && p.sunnahBefore) 1 else 0) + (if (d.after > 0 && p.sunnahAfter) 1 else 0)
    }
    HeroCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = prayed / 5f,
                modifier = Modifier.size(92.dp),
                color = Color(0xFFE6C27A),
                track = Color.White.copy(alpha = 0.15f),
                stroke = 9.dp,
            ) {
                Text(L.digits("$prayed/5"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(L.prayedToday, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MiniStat(L.n(jamaa), L.statusJamaa, wc.jamaa)
                    MiniStat(L.n(onTime), L.statusOnTime, wc.onTime)
                    MiniStat(L.n(missed), L.statusMissed, wc.missed)
                }
                Text(
                    L.digits("${L.sunnah}: $sunnah/${Prayers.SUNNAH_SLOTS}") + if (state.day.witr > 0) " · ${L.witr} ✓" else "",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, color: Color) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text("$value $label", color = Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun PrayerRow(
    name: String,
    log: PrayerLog,
    hasBefore: Boolean,
    hasAfter: Boolean,
    fardhRakaat: Int,
    flexible: Boolean,
    onFardh: () -> Unit,
    onBefore: () -> Unit,
    onAfter: () -> Unit,
    onLongBefore: () -> Unit,
    onLongAfter: () -> Unit,
    onStatus: () -> Unit,
) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    val statusColor = wc.status(log.status)
    val tint by animateColorAsState(statusColor ?: Color.Transparent, tween(400), label = "tint")
    val base = cs.surfaceContainer
    val strength = if (wc.dark) 0.22f else 0.16f
    val bg = Brush.horizontalGradient(
        listOf(lerp(base, tint, if (statusColor != null) strength * 1.4f else 0f), lerp(base, tint, if (statusColor != null) strength * 0.6f else 0f)),
    )
    val borderColor = if (statusColor != null) tint.copy(alpha = 0.75f) else cs.outlineVariant.copy(alpha = 0.6f)
    val sunnahColor = if (wc.dark) Color(0xFF4FA88C) else Color(0xFF3E8C75)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(if (statusColor != null) 1.5.dp else 1.dp, borderColor),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(bg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.width(76.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    L.statusLabel(log.status),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor ?: cs.onSurfaceVariant,
                    fontWeight = if (statusColor != null) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (hasBefore) {
                    CaptionedCircle(L.sunnah) {
                        WorshipCircle(
                            filled = log.sunnahBefore,
                            size = 40.dp,
                            label = L.n(log.beforeRakaat),
                            color = sunnahColor,
                            onLongPress = if (flexible) onLongBefore else null,
                            onTap = onBefore,
                        )
                    }
                }
                CaptionedCircle(L.fardh) {
                    WorshipCircle(filled = log.fardh, size = 58.dp, label = L.n(fardhRakaat), onTap = onFardh)
                }
                if (hasAfter) {
                    CaptionedCircle(L.sunnah) {
                        WorshipCircle(
                            filled = log.sunnahAfter,
                            size = 40.dp,
                            label = L.n(log.afterRakaat),
                            color = sunnahColor,
                            onLongPress = if (flexible) onLongAfter else null,
                            onTap = onAfter,
                        )
                    }
                }
            }
            StatusButton(log.status, statusColor, onStatus)
        }
    }
}

@Composable
private fun StatusButton(status: Int, color: Color?, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color ?: cs.surfaceContainerHigh)
            .border(1.dp, if (color == null) cs.outlineVariant else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val icon = when (status) {
            Status.JAMAA -> Icons.Filled.Star
            Status.ON_TIME -> Icons.Filled.Check
            Status.MISSED -> Icons.Filled.Close
            else -> Icons.Filled.Add
        }
        Icon(icon, contentDescription = null, tint = if (color != null) Color.White else cs.onSurfaceVariant, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun NightPrayerRow(name: String, hint: String, rakaat: Int, onClick: () -> Unit) {
    val L = LocalLang.current
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (rakaat > 0) L.rakaatCount(rakaat) else hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (rakaat > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WorshipCircle(
                filled = rakaat > 0,
                size = 54.dp,
                label = if (rakaat > 0) L.n(rakaat) else "+",
                color = MaterialTheme.colorScheme.tertiary,
                onTap = onClick,
            )
        }
    }
}
