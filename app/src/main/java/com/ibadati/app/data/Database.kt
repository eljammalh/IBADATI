package com.ibadati.app.data

import android.content.Context
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

@Dao
interface WorshipDao {
    @Query("SELECT * FROM days ORDER BY day")
    fun days(): Flow<List<DayLog>>

    @Query("SELECT * FROM prayers ORDER BY day, prayer")
    fun prayers(): Flow<List<PrayerLog>>

    @Query("SELECT * FROM khatmas ORDER BY id")
    fun khatmas(): Flow<List<Khatma>>

    @Query("SELECT * FROM days ORDER BY day")
    suspend fun allDays(): List<DayLog>

    @Query("SELECT * FROM prayers ORDER BY day, prayer")
    suspend fun allPrayers(): List<PrayerLog>

    @Query("SELECT * FROM khatmas ORDER BY id")
    suspend fun allKhatmas(): List<Khatma>

    @Upsert
    suspend fun upsertDay(d: DayLog)

    @Upsert
    suspend fun upsertPrayer(p: PrayerLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putKhatma(k: Khatma): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(d: List<DayLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(p: List<PrayerLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhatmas(k: List<Khatma>)

    @Query("DELETE FROM days")
    suspend fun clearDays()

    @Query("DELETE FROM prayers")
    suspend fun clearPrayers()

    @Query("DELETE FROM khatmas")
    suspend fun clearKhatmas()

    @Transaction
    suspend fun replaceAll(days: List<DayLog>, prayers: List<PrayerLog>, khatmas: List<Khatma>) {
        clearDays()
        clearPrayers()
        clearKhatmas()
        insertDays(days)
        insertPrayers(prayers)
        insertKhatmas(khatmas)
    }
}

@Database(entities = [DayLog::class, PrayerLog::class, Khatma::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): WorshipDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "ibadati.db")
                .build().also { instance = it }
        }
    }
}
