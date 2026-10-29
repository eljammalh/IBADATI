package com.ibadati.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ibadati.app.MainViewModel
import com.ibadati.app.UiState
import com.ibadati.app.data.Reminder
import com.ibadati.app.data.ThemeMode
import java.time.LocalDate

@Composable
fun SettingsScreen(state: UiState, vm: MainViewModel, onBack: () -> Unit) {
    val L = LocalLang.current
    val st = state.settings
    val context = LocalContext.current
    var goalEdit by remember { mutableStateOf<String?>(null) }
    var timeFor by remember { mutableStateOf<String?>(null) }
    var pendingReminder by remember { mutableStateOf<String?>(null) }
    var confirmRestore by remember { mutableStateOf<android.net.Uri?>(null) }

    BackHandler(onBack = onBack)

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val t = pendingReminder
        if (t != null) {
            if (granted) vm.setReminder(t, true) else toast(L.notificationsBlocked)
        }
        pendingReminder = null
    }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportBackup(uri) { ok -> toast(if (ok) L.backupSaved else L.backupFailed) }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) confirmRestore = uri
    }

    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = L.back) }
            Text(L.settings, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        ScreenColumn {
            SectionCard(title = L.language) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pill("English", selected = !st.arabic, modifier = Modifier.weight(1f)) { vm.setArabic(false) }
                    Pill("العربية", selected = st.arabic, modifier = Modifier.weight(1f)) { vm.setArabic(true) }
                }
                Text(L.numerals, style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Pill(L.numeralsArabic, selected = st.arabicDigits, modifier = Modifier.weight(1f)) { vm.setArabicDigits(true) }
                    Pill(L.numeralsWestern, selected = !st.arabicDigits, modifier = Modifier.weight(1f)) { vm.setArabicDigits(false) }
                }
                SwitchRow(L.showHijri, L.hijri(state.today), st.showHijri) { vm.setShowHijri(it) }
            }

            SectionCard(title = L.appearance) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(L.themeSystem, selected = st.theme == ThemeMode.SYSTEM, modifier = Modifier.weight(1f)) { vm.setTheme(ThemeMode.SYSTEM) }
                    Pill(L.themeLight, selected = st.theme == ThemeMode.LIGHT, modifier = Modifier.weight(1f)) { vm.setTheme(ThemeMode.LIGHT) }
                    Pill(L.themeDark, selected = st.theme == ThemeMode.DARK, modifier = Modifier.weight(1f)) { vm.setTheme(ThemeMode.DARK) }
                }
            }

            SectionCard(title = L.dailyGoals) {
                GoalRow(L.goalQuran, L.pages(st.quranGoal)) { goalEdit = "quran" }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                GoalRow(L.goalDua, L.minutes(st.duaGoal)) { goalEdit = "dua" }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                GoalRow(L.goalIstighfar, L.n(st.istighfarGoal)) { goalEdit = "istighfar" }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                GoalRow(L.goalSalawat, L.n(st.salawatGoal)) { goalEdit = "salawat" }
            }

            SectionCard(title = L.reminders) {
                Reminder.TYPES.forEach { type ->
                    val r = st.reminders[type] ?: return@forEach
                    val name = when (type) {
                        Reminder.MORNING -> L.remMorning
                        Reminder.EVENING -> L.remEvening
                        Reminder.QURAN -> L.remQuran
                        else -> L.remKahf
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                L.time(r.hour, r.minute),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { timeFor = type }.padding(vertical = 2.dp),
                            )
                        }
                        Switch(
                            checked = r.enabled,
                            onCheckedChange = { on ->
                                if (on && Build.VERSION.SDK_INT >= 33 &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    pendingReminder = type
                                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    vm.setReminder(type, on)
                                }
                            },
                        )
                    }
                }
            }

            SectionCard(title = L.backup) {
                Text(L.backupHint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { exporter.launch("ibadati-backup-${LocalDate.now()}.json") }, modifier = Modifier.weight(1f)) {
                        Text(L.exportBackup)
                    }
                    OutlinedButton(onClick = { importer.launch(arrayOf("application/json", "application/octet-stream", "text/plain", "*/*")) }, modifier = Modifier.weight(1f)) {
                        Text(L.importBackup)
                    }
                }
            }

            SectionCard(title = L.about) {
                Text(L.appName, fontFamily = Amiri, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(L.aboutText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(L.digits("1.0"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    goalEdit?.let { which ->
        val (title, current, max) = when (which) {
            "quran" -> Triple(L.goalQuran, st.quranGoal, 604)
            "dua" -> Triple(L.goalDua, st.duaGoal, 240)
            "istighfar" -> Triple(L.goalIstighfar, st.istighfarGoal, 100000)
            else -> Triple(L.goalSalawat, st.salawatGoal, 100000)
        }
        NumberPickerDialog(title, current, 1, max, onDismiss = { goalEdit = null }) {
            vm.setGoal(which, it)
            goalEdit = null
        }
    }
    timeFor?.let { type ->
        val r = st.reminders[type]
        if (r != null) {
            TimeDialog(r.hour, r.minute, onDismiss = { timeFor = null }) { h, m ->
                vm.setReminderTime(type, h, m)
                timeFor = null
            }
        }
    }
    confirmRestore?.let { uri ->
        AppDialog(
            title = L.restoreConfirmTitle,
            onDismiss = { confirmRestore = null },
            buttons = {
                TextButton(onClick = { confirmRestore = null }) { Text(L.cancel) }
                TextButton(onClick = {
                    confirmRestore = null
                    vm.importBackup(uri) { ok -> toast(if (ok) L.backupRestored else L.backupFailed) }
                }) { Text(L.importBackup, fontWeight = FontWeight.Bold) }
            },
        ) {
            Text(L.restoreConfirmText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun GoalRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
    }
}
