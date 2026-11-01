package com.ibadati.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import com.ibadati.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ibadati.app.data.Status
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

/** Shared frame for all dialogs: rounded card, title, content, buttons row. */
@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    buttons: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).widthIn(max = 460.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                content()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { buttons() }
            }
        }
    }
}

// ---------------------------------------------------------------- Calendar

@Composable
fun CalendarDialog(
    selected: Long,
    today: Long,
    hasData: (Long) -> Boolean,
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit,
) {
    val L = LocalLang.current
    var month by remember { mutableStateOf(YearMonth.from(LocalDate.ofEpochDay(selected))) }
    val thisMonth = YearMonth.from(LocalDate.ofEpochDay(today))
    // Week starts on Saturday.
    val order = listOf(
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
    )
    AppDialog(
        title = L.t("Choose a day", "اختر يومًا"),
        onDismiss = onDismiss,
        buttons = { TextButton(onClick = onDismiss) { Text(L.cancel) } },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { month = month.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
            }
            Text(
                L.digits("${L.month(month.monthValue)} ${month.year}"),
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = { month = month.plusMonths(1) }, enabled = month.isBefore(thisMonth)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }
        }
        Row(Modifier.fillMaxWidth()) {
            order.forEach {
                Text(
                    L.weekdayShort(it),
                    Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val first = month.atDay(1)
        val lead = order.indexOf(first.dayOfWeek)
        val cells = lead + month.lengthOfMonth()
        val rows = (cells + 6) / 7
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth()) {
                    for (c in 0 until 7) {
                        val idx = r * 7 + c - lead
                        Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (idx in 0 until month.lengthOfMonth()) {
                                val day = first.plusDays(idx.toLong()).toEpochDay()
                                CalendarCell(
                                    label = L.n(idx + 1),
                                    selected = day == selected,
                                    isToday = day == today,
                                    enabled = day <= today,
                                    dot = hasData(day),
                                ) { onPick(day) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(label: String, selected: Boolean, isToday: Boolean, enabled: Boolean, dot: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (selected) cs.primary else Color.Transparent)
            .border(if (isToday && !selected) 1.5.dp else 0.dp, if (isToday) cs.primary else Color.Transparent, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = when {
                selected -> cs.onPrimary
                !enabled -> cs.onSurface.copy(alpha = 0.3f)
                else -> cs.onSurface
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        if (dot) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (selected) cs.onPrimary else LocalWorshipColors.current.gold),
            )
        }
    }
}

// ---------------------------------------------------------------- Number picker

/** Pick a number with −/+ buttons, a slider, or by typing it. */
@Composable
fun NumberPickerDialog(
    title: String,
    initial: Int,
    min: Int,
    max: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val L = LocalLang.current
    var value by remember { mutableIntStateOf(initial.coerceIn(min, max)) }
    var text by remember { mutableStateOf(value.toString()) }
    fun set(v: Int) {
        value = v.coerceIn(min, max)
        text = value.toString()
    }
    AppDialog(
        title = title,
        onDismiss = onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text(L.cancel) }
            TextButton(onClick = { onConfirm(value) }) { Text(L.save, fontWeight = FontWeight.Bold) }
        },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            FilledTonalIconButton(onClick = { set(value - 1) }, modifier = Modifier.size(48.dp)) {
                Text("−", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                L.n(value),
                Modifier.width(140.dp),
                textAlign = TextAlign.Center,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            FilledTonalIconButton(onClick = { set(value + 1) }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Add, null)
            }
        }
        if (max - min > 10) {
            Slider(
                value = value.toFloat(),
                onValueChange = { set(it.roundToInt()) },
                valueRange = min.toFloat()..max.toFloat(),
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { t ->
                text = t.filter { it.isDigit() }.take(6)
                parseNumber(text)?.let { value = it.coerceIn(min, max) }
            },
            label = { Text(L.t("Or type a number", "أو اكتب الرقم")) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ---------------------------------------------------------------- Choice grid

/** A grid of numbers to choose from (Witr / Qiyam raka'at, Dhuhr 2 or 4). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChoiceDialog(
    title: String,
    subtitle: String?,
    options: List<Int>,
    current: Int,
    allowNone: Boolean,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit,
) {
    val L = LocalLang.current
    AppDialog(title = title, onDismiss = onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(L.cancel) } }) {
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { o ->
                val sel = o == current
                Box(
                    Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { onPick(o) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        L.n(o),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        if (allowNone) {
            Pill(L.none, selected = current == 0) { onPick(0) }
        }
    }
}

// ---------------------------------------------------------------- Prayer status

@Composable
fun StatusDialog(prayerName: String, current: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    AppDialog(
        title = "$prayerName · ${L.howDidYouPray}",
        onDismiss = onDismiss,
        buttons = {
            if (current != Status.NONE) TextButton(onClick = { onPick(Status.NONE) }) { Text(L.clearStatus) }
            TextButton(onClick = onDismiss) { Text(L.cancel) }
        },
    ) {
        StatusOption(L.statusMosque, L.statusMosqueHint, wc.mosque, painterResource(R.drawable.ic_tab_prayer), current == Status.MOSQUE) { onPick(Status.MOSQUE) }
        StatusOption(L.statusJamaa, L.statusJamaaHint, wc.jamaa, rememberVectorPainter(Icons.Filled.Star), current == Status.JAMAA) { onPick(Status.JAMAA) }
        StatusOption(L.statusOnTime, L.statusOnTimeHint, wc.onTime, rememberVectorPainter(Icons.Filled.Check), current == Status.ON_TIME) { onPick(Status.ON_TIME) }
        StatusOption(L.statusMissed, L.statusMissedHint, wc.missed, rememberVectorPainter(Icons.Filled.Close), current == Status.MISSED) { onPick(Status.MISSED) }
    }
}

@Composable
private fun StatusOption(title: String, hint: String, color: Color, icon: Painter, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = if (selected) 0.22f else 0.09f))
            .border(1.5.dp, color.copy(alpha = if (selected) 0.9f else 0.3f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ---------------------------------------------------------------- Dua minutes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DuaDialog(initial: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val L = LocalLang.current
    var value by remember { mutableIntStateOf(initial) }
    AppDialog(
        title = L.duaTime,
        onDismiss = onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text(L.cancel) }
            TextButton(onClick = { onConfirm(value) }) { Text(L.save, fontWeight = FontWeight.Bold) }
        },
    ) {
        Text(L.duaHowLong, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            FilledTonalIconButton(onClick = { value = (value - 1).coerceAtLeast(0) }, modifier = Modifier.size(48.dp)) {
                Text("−", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.width(140.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(L.n(value), fontSize = 46.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(L.t("minutes", "دقيقة"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalIconButton(onClick = { value = (value + 1).coerceAtMost(240) }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Filled.Add, null)
            }
        }
        Slider(value = value.toFloat(), onValueChange = { value = it.roundToInt() }, valueRange = 0f..120f)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15, 20, 30, 45, 60).forEach { m ->
                Pill(L.digits("$m ${L.minShort}"), selected = value == m) { value = m }
            }
        }
    }
}

// ---------------------------------------------------------------- Time

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDialog(hour: Int, minute: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val L = LocalLang.current
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
    AppDialog(
        title = L.chooseTime,
        onDismiss = onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text(L.cancel) }
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text(L.save, fontWeight = FontWeight.Bold) }
        },
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) }
    }
}

// ---------------------------------------------------------------- Khatma celebration

@Composable
fun KhatmaDialog(onDismiss: () -> Unit) {
    val L = LocalLang.current
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
            HeroCard {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Sparkles(Modifier.fillMaxWidth().height(260.dp))
                    Column(
                        Modifier.padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("ختمة مباركة", fontFamily = Amiri, fontSize = 40.sp, color = Color(0xFFE6C27A), fontWeight = FontWeight.Bold)
                        if (!L.ar) {
                            Text(L.khatmaComplete, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            L.khatmaCompleteText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE6C27A))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 28.dp, vertical = 12.dp),
                        ) {
                            Text(L.alhamdulillah, color = Color(0xFF2B1E04), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
