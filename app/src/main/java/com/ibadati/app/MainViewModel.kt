package com.ibadati.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibadati.app.data.AppDatabase
import com.ibadati.app.data.Backup
import com.ibadati.app.data.DayLog
import com.ibadati.app.data.Khatma
import com.ibadati.app.data.PrayerLog
import com.ibadati.app.data.Prayers
import com.ibadati.app.data.Quran
import com.ibadati.app.data.Settings
import com.ibadati.app.data.SettingsRepository
import com.ibadati.app.data.Status
import com.ibadati.app.logic.Records
import com.ibadati.app.notify.Reminders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class UiState(
    val loaded: Boolean = false,
    val settings: Settings = Settings(),
    val records: Records = Records(),
    val today: Long = LocalDate.now().toEpochDay(),
    /** The day being viewed/edited. */
    val selected: Long = LocalDate.now().toEpochDay(),
) {
    val isToday: Boolean get() = selected == today
    val day: DayLog get() = records.day(selected)
    fun prayer(i: Int): PrayerLog = records.prayer(selected, i)
}

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dao()
    private val repo = SettingsRepository(app)
    private val writeLock = Mutex()

    private val todayFlow = flow {
        while (true) {
            emit(LocalDate.now().toEpochDay())
            delay(30_000)
        }
    }.distinctUntilChanged()

    /** null = follow today (so the app moves to the new day at midnight). */
    private val selectedFlow = MutableStateFlow<Long?>(null)

    private val recordsFlow = combine(dao.days(), dao.prayers(), dao.khatmas()) { days, prayers, khatmas ->
        Records(
            days = days.associateBy { it.day },
            prayers = prayers.groupBy { it.day }.mapValues { (_, list) -> list.associateBy { it.prayer } },
            khatmas = khatmas,
        )
    }

    val state: StateFlow<UiState> = combine(recordsFlow, repo.settings, todayFlow, selectedFlow) { records, settings, today, sel ->
        UiState(
            loaded = true,
            settings = settings,
            records = records,
            today = today,
            selected = (sel ?: today).coerceAtMost(today),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        viewModelScope.launch {
            if (dao.allKhatmas().isEmpty()) {
                dao.putKhatma(Khatma(startDay = LocalDate.now().toEpochDay()))
            }
        }
    }

    // ---- Day selection ----

    fun selectDay(day: Long) {
        val today = state.value.today
        selectedFlow.value = if (day >= today) null else day
    }

    fun shiftDay(by: Int) = selectDay(state.value.selected + by)

    // ---- Prayers ----

    private fun updatePrayer(index: Int, change: (PrayerLog) -> PrayerLog) {
        val day = state.value.selected
        viewModelScope.launch {
            writeLock.withLock {
                val current = dao.prayer(day, index) ?: Prayers.default(day, index)
                dao.upsertPrayer(change(current))
            }
        }
    }

    fun toggleFardh(i: Int) = updatePrayer(i) { it.copy(fardh = !it.fardh) }
    fun toggleSunnahBefore(i: Int) = updatePrayer(i) { it.copy(sunnahBefore = !it.sunnahBefore) }
    fun toggleSunnahAfter(i: Int) = updatePrayer(i) { it.copy(sunnahAfter = !it.sunnahAfter) }

    fun setStatus(i: Int, status: Int) = updatePrayer(i) {
        when (status) {
            Status.JAMAA, Status.ON_TIME -> it.copy(status = status, fardh = true)
            Status.MISSED -> it.copy(status = status, fardh = false)
            else -> it.copy(status = Status.NONE)
        }
    }

    fun setSunnahRakaat(i: Int, before: Boolean, rakaat: Int) = updatePrayer(i) {
        if (before) it.copy(beforeRakaat = rakaat, sunnahBefore = true)
        else it.copy(afterRakaat = rakaat, sunnahAfter = true)
    }

    // ---- Day fields ----

    private fun updateDay(change: (DayLog) -> DayLog) {
        val day = state.value.selected
        viewModelScope.launch {
            writeLock.withLock {
                dao.upsertDay(change(dao.day(day) ?: DayLog(day)))
            }
        }
    }

    fun setWitr(v: Int) = updateDay { it.copy(witr = v) }
    fun setQiyam(v: Int) = updateDay { it.copy(qiyam = v) }
    fun toggleAthkar(i: Int) = updateDay { it.copy(athkar = it.athkar xor (1 shl i)) }
    fun setDua(min: Int) = updateDay { it.copy(duaMinutes = min.coerceIn(0, 1440)) }
    fun setIstighfar(v: Int) = updateDay { it.copy(istighfar = v.coerceIn(0, 1_000_000)) }
    fun addIstighfar(v: Int) = updateDay { it.copy(istighfar = (it.istighfar + v).coerceIn(0, 1_000_000)) }
    fun setSalawat(v: Int) = updateDay { it.copy(salawat = v.coerceIn(0, 1_000_000)) }
    fun addSalawat(v: Int) = updateDay { it.copy(salawat = (it.salawat + v).coerceIn(0, 1_000_000)) }
    fun toggleKahf() = updateDay { it.copy(kahf = !it.kahf) }

    // ---- Quran ----

    /**
     * Saves the selected day's reading. Returns (through [onKhatma]) true when this
     * reading reached the last page and completed the khatma.
     */
    fun saveQuran(from: Int, to: Int, onKhatma: () -> Unit) {
        val day = state.value.selected
        viewModelScope.launch {
            var completed = false
            writeLock.withLock {
                val existing = dao.day(day) ?: DayLog(day)
                var khatmaId = existing.khatmaId
                val khatmas = dao.allKhatmas()
                if (khatmaId == 0L || khatmas.none { it.id == khatmaId }) {
                    val last = khatmas.lastOrNull()
                    // When the previous khatma is finished, reading again starts a new one.
                    khatmaId = if (last == null || last.endDay != null) dao.putKhatma(Khatma(startDay = day)) else last.id
                }
                val f = from.coerceIn(1, Quran.PAGES)
                val t = to.coerceIn(f, Quran.PAGES)
                dao.upsertDay(existing.copy(quranFrom = f, quranTo = t, khatmaId = khatmaId))
                val k = dao.allKhatmas().firstOrNull { it.id == khatmaId }
                if (t == Quran.PAGES && k != null && k.endDay == null) {
                    dao.putKhatma(k.copy(endDay = day))
                    completed = true
                }
            }
            if (completed) onKhatma()
        }
    }

    fun clearQuran() {
        val day = state.value.selected
        viewModelScope.launch {
            writeLock.withLock {
                val existing = dao.day(day) ?: return@withLock
                dao.upsertDay(existing.copy(quranFrom = 0, quranTo = 0))
                // If this reading had completed its khatma, reopen it.
                val k = dao.allKhatmas().firstOrNull { it.id == existing.khatmaId }
                if (k != null && k.endDay == day && existing.quranTo == Quran.PAGES) {
                    dao.putKhatma(k.copy(endDay = null))
                }
            }
        }
    }

    fun startNewKhatma() {
        viewModelScope.launch {
            writeLock.withLock {
                dao.putKhatma(Khatma(startDay = state.value.today))
            }
        }
    }

    // ---- Settings ----

    fun chooseLanguage(arabic: Boolean) = viewModelScope.launch { repo.chooseLanguage(arabic) }
    fun setArabic(v: Boolean) = viewModelScope.launch { repo.setArabic(v) }
    fun setArabicDigits(v: Boolean) = viewModelScope.launch { repo.setArabicDigits(v) }
    fun setShowHijri(v: Boolean) = viewModelScope.launch { repo.setShowHijri(v) }
    fun setTheme(v: Int) = viewModelScope.launch { repo.setTheme(v) }
    fun setGoal(which: String, v: Int) = viewModelScope.launch { repo.setGoal(which, v.coerceAtLeast(1)) }

    fun setReminder(type: String, enabled: Boolean) = viewModelScope.launch {
        repo.setReminder(type, enabled)
        val r = repo.settings.first().reminders[type] ?: return@launch
        Reminders.schedule(getApplication(), type, enabled, r.hour, r.minute)
    }

    fun setReminderTime(type: String, hour: Int, minute: Int) = viewModelScope.launch {
        repo.setReminderTime(type, hour, minute)
        val r = repo.settings.first().reminders[type] ?: return@launch
        Reminders.schedule(getApplication(), type, r.enabled, hour, minute)
    }

    // ---- Backup ----

    fun exportBackup(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            try {
                val json = Backup.toJson(dao.allDays(), dao.allPrayers(), dao.allKhatmas())
                getApplication<Application>().contentResolver.openOutputStream(uri, "wt")?.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                } ?: return@withContext false
                true
            } catch (e: Exception) {
                false
            }
        }
        onDone(ok)
    }

    fun importBackup(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            try {
                val text = getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                } ?: return@withContext false
                val data = Backup.fromJson(text) ?: return@withContext false
                writeLock.withLock {
                    val khatmas = data.khatmas.ifEmpty { listOf(Khatma(startDay = LocalDate.now().toEpochDay())) }
                    dao.replaceAll(data.days, data.prayers, khatmas)
                }
                true
            } catch (e: Exception) {
                false
            }
        }
        onDone(ok)
    }

}
