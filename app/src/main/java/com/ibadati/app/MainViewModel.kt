package com.ibadati.app

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ibadati.app.data.AppDatabase
import com.ibadati.app.data.Backup
import com.ibadati.app.data.Book
import com.ibadati.app.data.CustomSin
import com.ibadati.app.data.Memorized
import com.ibadati.app.data.Mushaf
import com.ibadati.app.data.Pin
import com.ibadati.app.data.ReadingLog
import com.ibadati.app.data.SinLog
import com.ibadati.app.data.Sins
import com.ibadati.app.logic.Reading
import com.ibadati.app.ui.Images
import androidx.core.content.FileProvider
import java.io.File
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

    private val worshipFlow = combine(dao.days(), dao.prayers(), dao.khatmas(), dao.memorized()) { days, prayers, khatmas, mem ->
        Records(
            days = days.associateBy { it.day },
            prayers = prayers.groupBy { it.day }.mapValues { (_, list) -> list.associateBy { it.prayer } },
            khatmas = khatmas,
            memorized = mem.map { it.surahId }.toSet(),
        )
    }

    private val recordsFlow = combine(
        worshipFlow, dao.sinLogs(), dao.customSins(), dao.books(), dao.readingLogs(),
    ) { base, sins, custom, books, reading ->
        base.copy(
            sinDays = sins.groupBy { it.sinKey }.mapValues { (_, l) -> l.map { it.day }.toSet() },
            customSins = custom,
            books = books,
            reading = Reading.logsOf(reading),
        )
    }

    /** True once the PIN was entered; cleared when leaving the sins tab or the app goes to the background. */
    private val _sinsUnlocked = MutableStateFlow(false)
    val sinsUnlocked: StateFlow<Boolean> = _sinsUnlocked

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
            Status.MOSQUE, Status.JAMAA, Status.ON_TIME -> it.copy(status = status, fardh = true)
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

    /** Fasting can be marked on any past day or today, independent of the selected day. */
    fun toggleFast(day: Long) {
        if (day > state.value.today) return
        viewModelScope.launch {
            writeLock.withLock {
                val d = dao.day(day) ?: DayLog(day)
                dao.upsertDay(d.copy(fasted = !d.fasted))
            }
        }
    }

    // ---- Memorization ----

    fun toggleMemorized(surahId: Int) = viewModelScope.launch {
        writeLock.withLock {
            if (surahId in state.value.records.memorized) dao.deleteMemorized(surahId)
            else dao.putMemorized(Memorized(surahId))
        }
    }

    fun setJuzMemorized(juz: Int, memorized: Boolean) = viewModelScope.launch {
        writeLock.withLock {
            Mushaf.surahs.filter { it.juzStart == juz && it.juzEnd == juz }.forEach {
                if (memorized) dao.putMemorized(Memorized(it.id)) else dao.deleteMemorized(it.id)
            }
        }
    }

    // ---- 40-day challenge ----

    fun markChallengesSeen(n: Int) = viewModelScope.launch { repo.setChallengesSeen(n) }

    // ---- Sins (private) ----

    fun lockSins() { _sinsUnlocked.value = false }

    fun createSinsPin(pin: String) = viewModelScope.launch {
        val hash = withContext(Dispatchers.Default) { Pin.hash(pin) }
        repo.setSinsPin(hash)
        _sinsUnlocked.value = true
    }

    fun unlockSins(pin: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.Default) { Pin.verify(pin, state.value.settings.sinsPin) }
        if (ok) _sinsUnlocked.value = true
        onResult(ok)
    }

    fun changeSinsPin(old: String, new: String, onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.Default) { Pin.verify(old, state.value.settings.sinsPin) }
        if (ok) repo.setSinsPin(withContext(Dispatchers.Default) { Pin.hash(new) })
        onResult(ok)
    }

    /** Forgotten PIN: erases only the sins records and the PIN. */
    fun resetSins() = viewModelScope.launch {
        writeLock.withLock { dao.clearSins() }
        repo.setSinsPin(null)
        _sinsUnlocked.value = false
    }

    fun toggleSin(key: String) {
        val day = state.value.selected
        viewModelScope.launch {
            writeLock.withLock {
                if (state.value.records.sinDays[key]?.contains(day) == true) dao.deleteSinLog(day, key)
                else dao.putSinLog(SinLog(day, key))
            }
        }
    }

    fun addCustomSin(name: String, major: Boolean) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        writeLock.withLock { dao.putCustomSin(CustomSin(name = name.trim(), major = major)) }
    }

    fun deleteCustomSin(id: Long) = viewModelScope.launch {
        writeLock.withLock {
            dao.deleteSinLogsFor(Sins.customKey(id))
            dao.deleteCustomSin(id)
        }
    }

    // ---- Reading ----

    private fun coversDir(): File = File(getApplication<Application>().filesDir, "covers").apply { mkdirs() }

    fun coverFile(name: String?): File? = name?.let { File(coversDir(), it) }?.takeIf { it.exists() }

    /** A temporary file for the camera to write into; returns its content Uri. */
    fun newCameraUri(): Uri {
        val app = getApplication<Application>()
        val dir = File(app.cacheDir, "camera").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val f = File(dir, "shot_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(app, app.packageName + ".files", f)
    }

    /**
     * Saves a book. [coverUri] is a newly picked image (copied and resized into private storage);
     * [removeCover] clears the existing one.
     */
    fun saveBook(existing: Book?, title: String, author: String, pages: Int, coverUri: Uri?, removeCover: Boolean) =
        viewModelScope.launch {
            val app = getApplication<Application>()
            var cover = if (removeCover) null else existing?.cover
            if (coverUri != null) {
                val name = "cover_${System.currentTimeMillis()}.jpg"
                val ok = withContext(Dispatchers.IO) { Images.saveResized(app, coverUri, File(coversDir(), name)) }
                if (ok) cover = name
            }
            if (cover != existing?.cover) existing?.cover?.let { File(coversDir(), it).delete() }
            writeLock.withLock {
                val base = existing ?: Book(title = "", author = "", totalPages = 1, addedDay = state.value.today)
                val total = pages.coerceIn(1, 100_000)
                val read = if (existing != null) dao.pagesRead(existing.id) else 0
                val completed = when {
                    read >= total -> base.completedDay ?: state.value.today
                    else -> null
                }
                dao.putBook(base.copy(title = title.trim(), author = author.trim(), totalPages = total, cover = cover, completedDay = completed))
            }
        }

    fun deleteBook(b: Book) = viewModelScope.launch {
        writeLock.withLock {
            dao.deleteReadingFor(b.id)
            dao.deleteBook(b.id)
        }
        b.cover?.let { File(coversDir(), it).delete() }
    }

    /** Sets the pages read for a book on the selected day. Calls [onCompleted] when this finishes the book. */
    fun setReading(bookId: Long, pages: Int, onCompleted: () -> Unit) {
        val day = state.value.selected
        viewModelScope.launch {
            var finished = false
            writeLock.withLock {
                if (pages <= 0) dao.deleteReading(bookId, day) else dao.putReading(ReadingLog(bookId, day, pages))
                val b = dao.book(bookId) ?: return@withLock
                val read = dao.pagesRead(bookId)
                if (read >= b.totalPages && b.completedDay == null) {
                    dao.putBook(b.copy(completedDay = day))
                    finished = true
                } else if (read < b.totalPages && b.completedDay != null) {
                    dao.putBook(b.copy(completedDay = null))
                }
            }
            if (finished) onCompleted()
        }
    }

    fun setBookCompleted(b: Book, completed: Boolean) = viewModelScope.launch {
        writeLock.withLock { dao.putBook(b.copy(completedDay = if (completed) state.value.today else null)) }
    }

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
                val books = dao.allBooks()
                val covers = books.mapNotNull { b ->
                    val f = coverFile(b.cover) ?: return@mapNotNull null
                    b.cover!! to android.util.Base64.encodeToString(f.readBytes(), android.util.Base64.NO_WRAP)
                }.toMap()
                val json = Backup.toJson(
                    Backup.Data(
                        dao.allDays(), dao.allPrayers(), dao.allKhatmas(), dao.allMemorized(),
                        dao.allSinLogs(), dao.allCustomSins(), books, dao.allReadingLogs(), covers,
                    ),
                )
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
                data.covers.forEach { (name, b64) ->
                    if (name.matches(Regex("[A-Za-z0-9_.-]+"))) {
                        File(coversDir(), name).writeBytes(android.util.Base64.decode(b64, android.util.Base64.NO_WRAP))
                    }
                }
                writeLock.withLock {
                    val khatmas = data.khatmas.ifEmpty { listOf(Khatma(startDay = LocalDate.now().toEpochDay())) }
                    dao.replaceAll(data.copy(khatmas = khatmas))
                }
                true
            } catch (e: Exception) {
                false
            }
        }
        onDone(ok)
    }

}
