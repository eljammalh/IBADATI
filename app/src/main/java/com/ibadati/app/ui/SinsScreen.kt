package com.ibadati.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.Sins
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun SinsScreen(state: UiState, vm: MainViewModel, unlocked: Boolean) {
    when {
        state.settings.sinsPin == null -> CreatePin(vm)
        !unlocked -> EnterPin(vm)
        else -> SinsContent(state, vm)
    }
}

// ---------------------------------------------------------------- PIN screens

@Composable
private fun CreatePin(vm: MainViewModel) {
    val L = LocalLang.current
    var first by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    PinPad(
        title = if (first == null) L.createPin else L.confirmPin,
        subtitle = L.pinPrivacy,
        error = error,
        resetKey = first,
    ) { pin ->
        val f = first
        if (f == null) {
            first = pin
            error = null
            true
        } else if (f == pin) {
            vm.createSinsPin(pin)
            true
        } else {
            first = null
            error = L.pinMismatch
            false
        }
    }
}

@Composable
private fun EnterPin(vm: MainViewModel) {
    val L = LocalLang.current
    var error by remember { mutableStateOf<String?>(null) }
    var attempt by remember { mutableStateOf(0) }
    var confirmReset by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            PinPad(title = L.enterPin, subtitle = L.pinPrivacy, error = error, resetKey = attempt) { pin ->
                vm.unlockSins(pin) { ok ->
                    if (!ok) {
                        error = L.wrongPin
                        attempt++
                    }
                }
                true
            }
        }
        TextButton(onClick = { confirmReset = true }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)) {
            Text(L.forgotPin)
        }
    }
    if (confirmReset) {
        AppDialog(
            title = L.resetSinsTitle,
            onDismiss = { confirmReset = false },
            buttons = {
                TextButton(onClick = { confirmReset = false }) { Text(L.cancel) }
                TextButton(onClick = {
                    confirmReset = false
                    vm.resetSins()
                }) { Text(L.erase, color = LocalWorshipColors.current.missed, fontWeight = FontWeight.Bold) }
            },
        ) {
            Text(L.resetSinsText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Four dots and a number pad. [onComplete] gets the 4 digits; return false to shake and clear.
 * [resetKey] clears the entry whenever it changes.
 */
@Composable
fun PinPad(title: String, subtitle: String?, error: String?, resetKey: Any?, onComplete: (String) -> Boolean) {
    val L = LocalLang.current
    val cs = MaterialTheme.colorScheme
    var digits by remember(resetKey) { mutableStateOf("") }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(error, resetKey) {
        if (error != null) {
            for (x in listOf(14f, -12f, 9f, -6f, 3f, 0f)) shake.animateTo(x, tween(45))
        }
    }
    fun press(d: Char) {
        if (digits.length >= 4) return
        digits += d
        if (digits.length == 4) {
            val ok = onComplete(digits)
            if (!ok) scope.launch { digits = "" }
        }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(cs.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Lock, null, tint = cs.onPrimaryContainer, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(22.dp))
        Row(
            Modifier.offset { IntOffset(shake.value.roundToInt(), 0) },
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            repeat(4) { i ->
                val filled = i < digits.length
                Box(
                    Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (filled) cs.primary else Color.Transparent)
                        .border(2.dp, if (error != null && digits.isEmpty()) LocalWorshipColors.current.missed else cs.primary, CircleShape),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(error ?: " ", color = LocalWorshipColors.current.missed, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(14.dp))
        val keys = listOf("123", "456", "789")
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                row.forEach { k -> PinKey(L.digits(k.toString())) { press(k) } }
            }
            Spacer(Modifier.height(14.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Spacer(Modifier.size(70.dp))
            PinKey(L.digits("0")) { press('0') }
            Box(
                Modifier.size(70.dp).clip(CircleShape).clickable { digits = digits.dropLast(1) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PinKey(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------------------------------------------------------- Content

private data class SinItem(val key: String, val name: String, val major: Boolean, val category: String?, val customId: Long?)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SinsContent(state: UiState, vm: MainViewModel) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<SinItem?>(null) }
    val expanded = remember { mutableStateListOf<String>() }
    val today = state.today
    val sel = state.selected
    val monthStart = LocalDate.ofEpochDay(today).withDayOfMonth(1).toEpochDay()

    val builtIn = Sins.all.map { SinItem(it.key, L.t(it.en, it.ar), it.major, it.category, null) }
    val custom = state.records.customSins.map { SinItem(Sins.customKey(it.id), it.name, it.major, null, it.id) }
    val tickedToday = (builtIn + custom).count { state.records.sinDays[it.key]?.contains(sel) == true }

    ScreenColumn {
        DateNavigator(sel, today, state.settings.showHijri, state.records::hasAnyData, vm::selectDay)

        HeroCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    L.tawbahVerse,
                    fontFamily = Amiri,
                    fontSize = 19.sp,
                    lineHeight = 34.sp,
                    color = Color(0xFFF3E4BE),
                    textAlign = TextAlign.Center,
                )
                Text(L.tawbahVerseEn, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${L.n(tickedToday)} ${L.todayCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFE6C27A),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(L.sinsIntro, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.lockSins() }) {
                Icon(Icons.Filled.Lock, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(L.lock)
            }
        }

        listOf(true to L.majorSins, false to L.minorSins).forEach { (major, title) ->
            SectionTitle(title)
            Sins.categories.forEach { cat ->
                val items = builtIn.filter { it.major == major && it.category == cat.key }
                if (items.isNotEmpty()) {
                    val id = "${if (major) "M" else "m"}_${cat.key}"
                    val open = id in expanded
                    val count = items.count { state.records.sinDays[it.key]?.contains(sel) == true }
                    SectionCard {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                                if (open) expanded.remove(id) else expanded.add(id)
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(if (major) wc.missed else wc.gold))
                            Spacer(Modifier.width(10.dp))
                            Text(L.t(cat.en, cat.ar), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (count > 0) {
                                Box(Modifier.clip(RoundedCornerShape(50)).background(wc.missed.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                    Text(L.n(count), color = wc.missed, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(L.n(items.size), style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            Icon(if (open) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, null, tint = cs.onSurfaceVariant)
                        }
                        if (open) {
                            items.forEach { SinRow(it, state, today, monthStart) { vm.toggleSin(it.key) } }
                        }
                    }
                }
            }
        }

        SectionTitle(L.mySins)
        SectionCard {
            custom.forEach { item ->
                SinRow(item, state, today, monthStart, showTag = true, onLongClick = { deleting = item }) { vm.toggleSin(item.key) }
            }
            OutlinedButton(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(6.dp))
                Text(L.addItem)
            }
        }
        Text(L.classificationNote, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
    }

    if (adding) AddSinDialog(onDismiss = { adding = false }) { name, major ->
        vm.addCustomSin(name, major)
        adding = false
    }
    deleting?.let { item ->
        AppDialog(
            title = L.deleteConfirm(item.name),
            onDismiss = { deleting = null },
            buttons = {
                TextButton(onClick = { deleting = null }) { Text(L.cancel) }
                TextButton(onClick = {
                    item.customId?.let { vm.deleteCustomSin(it) }
                    deleting = null
                }) { Text(L.delete, color = wc.missed, fontWeight = FontWeight.Bold) }
            },
        ) {}
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SinRow(
    item: SinItem,
    state: UiState,
    today: Long,
    monthStart: Long,
    showTag: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onToggle: () -> Unit,
) {
    val L = LocalLang.current
    val wc = LocalWorshipColors.current
    val cs = MaterialTheme.colorScheme
    val days = state.records.sinDays[item.key] ?: emptySet()
    val checked = state.selected in days
    val week = days.count { it in (today - 6)..today }
    val month = days.count { it in monthStart..today }
    val last = days.filter { it <= today }.maxOrNull()
    val since = last?.let { (today - it).toInt() }
    val bg by animateColorAsState(if (checked) wc.missed.copy(alpha = 0.10f) else Color.Transparent, label = "sinBg")
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .combinedClickable(onClick = onToggle, onLongClick = onLongClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f, fill = false))
                if (showTag) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (item.major) L.majorSins else L.minorSins,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.major) wc.missed else wc.gold,
                    )
                }
            }
            Text(
                L.sinCounts(week, month, days.size) + " · " + L.daysSince(since),
                style = MaterialTheme.typography.labelSmall,
                color = if (since == null || since >= 7) wc.onTime else cs.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddSinDialog(onDismiss: () -> Unit, onAdd: (String, Boolean) -> Unit) {
    val L = LocalLang.current
    var name by rememberSaveable { mutableStateOf("") }
    var major by rememberSaveable { mutableStateOf(false) }
    AppDialog(
        title = L.addItem,
        onDismiss = onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text(L.cancel) }
            TextButton(onClick = { onAdd(name, major) }, enabled = name.isNotBlank()) { Text(L.save, fontWeight = FontWeight.Bold) }
        },
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it.take(80) }, label = { Text(L.itemName) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(L.isMajor, modifier = Modifier.weight(1f))
            Switch(checked = major, onCheckedChange = { major = it })
        }
    }
}

/** Settings dialog for changing the PIN: current PIN, then new PIN twice. */
@Composable
fun ChangePinDialog(vm: MainViewModel, onDone: (Boolean) -> Unit, onDismiss: () -> Unit) {
    val L = LocalLang.current
    var step by remember { mutableStateOf(0) }
    var old by remember { mutableStateOf("") }
    var new1 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            PinPad(
                title = when (step) {
                    0 -> L.oldPin
                    1 -> L.newPin
                    else -> L.confirmPin
                },
                subtitle = null,
                error = error,
                resetKey = step to error,
            ) { pin ->
                when (step) {
                    0 -> { old = pin; error = null; step = 1 }
                    1 -> { new1 = pin; error = null; step = 2 }
                    else -> {
                        if (pin != new1) {
                            error = L.pinMismatch
                            step = 1
                        } else {
                            vm.changeSinsPin(old, new1) { ok ->
                                if (ok) onDone(true)
                                else {
                                    error = L.wrongPin
                                    step = 0
                                }
                            }
                        }
                    }
                }
                true
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) { Text(L.cancel) }
        }
    }
}
