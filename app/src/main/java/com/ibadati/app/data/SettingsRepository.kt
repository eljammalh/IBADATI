package com.ibadati.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** A daily reminder. [type] is one of Reminder.TYPES. */
data class ReminderSetting(val enabled: Boolean, val hour: Int, val minute: Int)

object Reminder {
    const val MORNING = "morning"
    const val EVENING = "evening"
    const val QURAN = "quran"
    const val KAHF = "kahf"
    const val FASTING = "fasting"
    val TYPES = listOf(MORNING, EVENING, QURAN, KAHF, FASTING)

    fun defaultTime(type: String): Pair<Int, Int> = when (type) {
        MORNING -> 6 to 30
        EVENING -> 17 to 30
        QURAN -> 21 to 0
        FASTING -> 20 to 30
        else -> 10 to 0
    }
}

object ThemeMode {
    const val SYSTEM = 0
    const val LIGHT = 1
    const val DARK = 2
}

data class Settings(
    /** False until the user picks a language on the welcome screen. */
    val onboarded: Boolean = false,
    val arabic: Boolean = false,
    val arabicDigits: Boolean = false,
    val showHijri: Boolean = true,
    val theme: Int = ThemeMode.SYSTEM,
    val duaGoal: Int = 15,
    val istighfarGoal: Int = 100,
    val salawatGoal: Int = 100,
    val quranGoal: Int = 5,
    /** Salted hash of the sins-tab PIN ("salt:hash"); null until the user creates one. */
    val sinsPin: String? = null,
    /** How many completed 40-day challenges have already been celebrated. */
    val challengesSeen: Int = 0,
    val reminders: Map<String, ReminderSetting> = Reminder.TYPES.associateWith {
        val (h, m) = Reminder.defaultTime(it)
        ReminderSetting(false, h, m)
    },
)

class SettingsRepository(private val context: Context) {
    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val arabic = booleanPreferencesKey("arabic")
        val digits = booleanPreferencesKey("arabic_digits")
        val hijri = booleanPreferencesKey("show_hijri")
        val theme = intPreferencesKey("theme")
        val duaGoal = intPreferencesKey("goal_dua")
        val istGoal = intPreferencesKey("goal_istighfar")
        val salGoal = intPreferencesKey("goal_salawat")
        val quranGoal = intPreferencesKey("goal_quran")
        val sinsPin = stringPreferencesKey("sins_pin")
        val challengesSeen = intPreferencesKey("challenges_seen")
        fun remOn(t: String) = booleanPreferencesKey("rem_${t}_on")
        fun remH(t: String) = intPreferencesKey("rem_${t}_h")
        fun remM(t: String) = intPreferencesKey("rem_${t}_m")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { fromPrefs(it) }

    private fun fromPrefs(p: Preferences): Settings {
        val d = Settings()
        return Settings(
            onboarded = p[K.onboarded] ?: d.onboarded,
            arabic = p[K.arabic] ?: d.arabic,
            arabicDigits = p[K.digits] ?: d.arabicDigits,
            showHijri = p[K.hijri] ?: d.showHijri,
            theme = p[K.theme] ?: d.theme,
            duaGoal = p[K.duaGoal] ?: d.duaGoal,
            istighfarGoal = p[K.istGoal] ?: d.istighfarGoal,
            salawatGoal = p[K.salGoal] ?: d.salawatGoal,
            quranGoal = p[K.quranGoal] ?: d.quranGoal,
            sinsPin = p[K.sinsPin],
            challengesSeen = p[K.challengesSeen] ?: d.challengesSeen,
            reminders = Reminder.TYPES.associateWith { t ->
                val (h, m) = Reminder.defaultTime(t)
                ReminderSetting(p[K.remOn(t)] ?: false, p[K.remH(t)] ?: h, p[K.remM(t)] ?: m)
            },
        )
    }

    suspend fun chooseLanguage(arabic: Boolean) {
        context.dataStore.edit {
            it[K.arabic] = arabic
            it[K.onboarded] = true
            // Arabic users usually expect Arabic-Indic digits; they can change it in Settings.
            if (it[K.digits] == null) it[K.digits] = arabic
        }
    }

    suspend fun setArabic(v: Boolean) { context.dataStore.edit { it[K.arabic] = v } }
    suspend fun setArabicDigits(v: Boolean) { context.dataStore.edit { it[K.digits] = v } }
    suspend fun setShowHijri(v: Boolean) { context.dataStore.edit { it[K.hijri] = v } }
    suspend fun setTheme(v: Int) { context.dataStore.edit { it[K.theme] = v } }

    suspend fun setGoal(which: String, v: Int) {
        val key = when (which) {
            "dua" -> K.duaGoal
            "istighfar" -> K.istGoal
            "salawat" -> K.salGoal
            else -> K.quranGoal
        }
        context.dataStore.edit { it[key] = v }
    }

    suspend fun setSinsPin(hash: String?) {
        context.dataStore.edit { if (hash == null) it.remove(K.sinsPin) else it[K.sinsPin] = hash }
    }

    suspend fun setChallengesSeen(n: Int) { context.dataStore.edit { it[K.challengesSeen] = n } }

    suspend fun setReminder(type: String, enabled: Boolean) {
        context.dataStore.edit { it[K.remOn(type)] = enabled }
    }

    suspend fun setReminderTime(type: String, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[K.remH(type)] = hour
            it[K.remM(type)] = minute
        }
    }
}
