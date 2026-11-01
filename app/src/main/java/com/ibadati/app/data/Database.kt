package com.ibadati.app.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Everything logged for one day, except the five prayers (see [PrayerLog]).
 * [day] is the epoch day (LocalDate.toEpochDay()).
 */
@Entity(tableName = "days")
data class DayLog(
    @PrimaryKey val day: Long,
    /** Witr raka'at prayed (0 = none). */
    val witr: Int = 0,
    /** Qiyam raka'at prayed (0 = none). */
    val qiyam: Int = 0,
    /** Bit i set = athkar i done (see Athkar.items). */
    val athkar: Int = 0,
    val duaMinutes: Int = 0,
    val istighfar: Int = 0,
    val salawat: Int = 0,
    val kahf: Boolean = false,
    /** First and last Quran page read this day (0 = not logged). */
    val quranFrom: Int = 0,
    val quranTo: Int = 0,
    /** The khatma this reading belongs to. */
    val khatmaId: Long = 0,
    /** Fasted this day (added in database version 2). */
    @ColumnInfo(defaultValue = "0") val fasted: Boolean = false,
) {
    val quranPages: Int get() = if (quranFrom > 0 && quranTo >= quranFrom) quranTo - quranFrom + 1 else 0
    fun athkarDone(i: Int): Boolean = (athkar shr i) and 1 == 1
    val athkarCount: Int get() = Integer.bitCount(athkar and 0x7F)
}

object Status {
    const val NONE = 0
    const val JAMAA = 1
    const val ON_TIME = 2
    const val MISSED = 3
    const val MOSQUE = 4

    /** Counts as praying on time (mosque, jama'a or alone on time). */
    fun prayed(s: Int) = s == MOSQUE || s == JAMAA || s == ON_TIME
}

/** One of the five daily prayers on a given day. */
@Entity(tableName = "prayers", primaryKeys = ["day", "prayer"])
data class PrayerLog(
    val day: Long,
    /** 0 Fajr, 1 Dhuhr, 2 Asr, 3 Maghrib, 4 Isha */
    val prayer: Int,
    val fardh: Boolean = false,
    val status: Int = Status.NONE,
    val sunnahBefore: Boolean = false,
    val sunnahAfter: Boolean = false,
    /** Raka'at chosen for the Sunnah before/after (matters for Dhuhr: 2 or 4). */
    val beforeRakaat: Int = 0,
    val afterRakaat: Int = 0,
)

@Entity(tableName = "khatmas")
data class Khatma(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDay: Long,
    /** Day the Quran was completed; null while in progress. */
    val endDay: Long? = null,
)

/** A surah the user has memorized. */
@Entity(tableName = "memorized")
data class Memorized(@PrimaryKey val surahId: Int)

/** A sin ticked on a given day. [sinKey] is a built-in key (see Sins) or "c<id>" for a custom item. */
@Entity(tableName = "sin_logs", primaryKeys = ["day", "sinKey"])
data class SinLog(val day: Long, val sinKey: String)

@Entity(tableName = "custom_sins")
data class CustomSin(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val major: Boolean,
)

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val totalPages: Int,
    /** File name of the cover image inside the app's private "covers" folder. */
    val cover: String? = null,
    val addedDay: Long,
    /** Day the book was finished; null while reading. */
    val completedDay: Long? = null,
)

@Entity(tableName = "reading_logs", primaryKeys = ["bookId", "day"])
data class ReadingLog(val bookId: Long, val day: Long, val pages: Int)

@Dao
interface WorshipDao {
    // ---- Live data ----
    @Query("SELECT * FROM days ORDER BY day")
    fun days(): Flow<List<DayLog>>

    @Query("SELECT * FROM prayers ORDER BY day, prayer")
    fun prayers(): Flow<List<PrayerLog>>

    @Query("SELECT * FROM khatmas ORDER BY id")
    fun khatmas(): Flow<List<Khatma>>

    @Query("SELECT * FROM memorized")
    fun memorized(): Flow<List<Memorized>>

    @Query("SELECT * FROM sin_logs")
    fun sinLogs(): Flow<List<SinLog>>

    @Query("SELECT * FROM custom_sins ORDER BY id")
    fun customSins(): Flow<List<CustomSin>>

    @Query("SELECT * FROM books ORDER BY id")
    fun books(): Flow<List<Book>>

    @Query("SELECT * FROM reading_logs")
    fun readingLogs(): Flow<List<ReadingLog>>

    // ---- One-off reads ----
    @Query("SELECT * FROM days ORDER BY day")
    suspend fun allDays(): List<DayLog>

    @Query("SELECT * FROM prayers ORDER BY day, prayer")
    suspend fun allPrayers(): List<PrayerLog>

    @Query("SELECT * FROM khatmas ORDER BY id")
    suspend fun allKhatmas(): List<Khatma>

    @Query("SELECT * FROM memorized")
    suspend fun allMemorized(): List<Memorized>

    @Query("SELECT * FROM sin_logs")
    suspend fun allSinLogs(): List<SinLog>

    @Query("SELECT * FROM custom_sins ORDER BY id")
    suspend fun allCustomSins(): List<CustomSin>

    @Query("SELECT * FROM books ORDER BY id")
    suspend fun allBooks(): List<Book>

    @Query("SELECT * FROM reading_logs")
    suspend fun allReadingLogs(): List<ReadingLog>

    @Query("SELECT * FROM days WHERE day = :day")
    suspend fun day(day: Long): DayLog?

    @Query("SELECT * FROM prayers WHERE day = :day AND prayer = :prayer")
    suspend fun prayer(day: Long, prayer: Int): PrayerLog?

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun book(id: Long): Book?

    @Query("SELECT COALESCE(SUM(pages), 0) FROM reading_logs WHERE bookId = :bookId")
    suspend fun pagesRead(bookId: Long): Int

    // ---- Writes ----
    @Upsert
    suspend fun upsertDay(d: DayLog)

    @Upsert
    suspend fun upsertPrayer(p: PrayerLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putKhatma(k: Khatma): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putMemorized(m: Memorized)

    @Query("DELETE FROM memorized WHERE surahId = :id")
    suspend fun deleteMemorized(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSinLog(s: SinLog)

    @Query("DELETE FROM sin_logs WHERE day = :day AND sinKey = :key")
    suspend fun deleteSinLog(day: Long, key: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putCustomSin(s: CustomSin): Long

    @Query("DELETE FROM custom_sins WHERE id = :id")
    suspend fun deleteCustomSin(id: Long)

    @Query("DELETE FROM sin_logs WHERE sinKey = :key")
    suspend fun deleteSinLogsFor(key: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putBook(b: Book): Long

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: Long)

    @Query("DELETE FROM reading_logs WHERE bookId = :id")
    suspend fun deleteReadingFor(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putReading(r: ReadingLog)

    @Query("DELETE FROM reading_logs WHERE bookId = :bookId AND day = :day")
    suspend fun deleteReading(bookId: Long, day: Long)

    // ---- Bulk (backup restore, sins reset) ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(d: List<DayLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(p: List<PrayerLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhatmas(k: List<Khatma>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemorized(m: List<Memorized>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSinLogs(s: List<SinLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomSins(s: List<CustomSin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(b: List<Book>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingLogs(r: List<ReadingLog>)

    @Query("DELETE FROM days") suspend fun clearDays()
    @Query("DELETE FROM prayers") suspend fun clearPrayers()
    @Query("DELETE FROM khatmas") suspend fun clearKhatmas()
    @Query("DELETE FROM memorized") suspend fun clearMemorized()
    @Query("DELETE FROM sin_logs") suspend fun clearSinLogs()
    @Query("DELETE FROM custom_sins") suspend fun clearCustomSins()
    @Query("DELETE FROM books") suspend fun clearBooks()
    @Query("DELETE FROM reading_logs") suspend fun clearReadingLogs()

    @Transaction
    suspend fun clearSins() {
        clearSinLogs()
        clearCustomSins()
    }

    @Transaction
    suspend fun replaceAll(d: Backup.Data) {
        clearDays()
        clearPrayers()
        clearKhatmas()
        clearMemorized()
        clearSinLogs()
        clearCustomSins()
        clearBooks()
        clearReadingLogs()
        insertDays(d.days)
        insertPrayers(d.prayers)
        insertKhatmas(d.khatmas)
        insertMemorized(d.memorized)
        insertSinLogs(d.sinLogs)
        insertCustomSins(d.customSins)
        insertBooks(d.books)
        insertReadingLogs(d.readingLogs)
    }
}

/** Version 1 → 2: fasting, memorization, sins and reading. Keeps every existing record. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE days ADD COLUMN fasted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS memorized (surahId INTEGER NOT NULL, PRIMARY KEY(surahId))")
        db.execSQL("CREATE TABLE IF NOT EXISTS sin_logs (day INTEGER NOT NULL, sinKey TEXT NOT NULL, PRIMARY KEY(day, sinKey))")
        db.execSQL("CREATE TABLE IF NOT EXISTS custom_sins (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, major INTEGER NOT NULL)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, " +
                "author TEXT NOT NULL, totalPages INTEGER NOT NULL, cover TEXT, addedDay INTEGER NOT NULL, completedDay INTEGER)",
        )
        db.execSQL("CREATE TABLE IF NOT EXISTS reading_logs (bookId INTEGER NOT NULL, day INTEGER NOT NULL, pages INTEGER NOT NULL, PRIMARY KEY(bookId, day))")
    }
}

@Database(
    entities = [
        DayLog::class, PrayerLog::class, Khatma::class, Memorized::class,
        SinLog::class, CustomSin::class, Book::class, ReadingLog::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): WorshipDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ibadati.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { instance = it }
        }
    }
}
