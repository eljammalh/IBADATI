# Ibadati · عبادتي

An offline Android app for tracking daily worship, in English and Arabic.

Kotlin · Jetpack Compose (Material 3) · Room · DataStore · WorkManager · min Android 8.0 (API 26)

## Get the APK

Every push to GitHub builds the app automatically:

1. Open the repository on GitHub and go to the **Actions** tab.
2. Click the latest **Build APK** run (green check).
3. Under **Artifacts**, download **Ibadati-apk**, unzip it, and copy `app-debug.apk` to your phone.
4. Open it on the phone to install (allow "install unknown apps" when asked).

You can also open the folder in **Android Studio** and use **Build → Build APK(s)**.

## Features

- **Prayers:** Fajr, Dhuhr, Asr, Maghrib, Isha with Fardh and Sunnah circles (Dhuhr Sunnah 2 or 4 by long-press), status per prayer (Jama'a = gold, on time = green, missed = red), Witr (1–13 odd) and Qiyam (2–20 even).
- **Athkar:** Morning, after each prayer, Night, and Dua minutes.
- **Quran:** start/stop page per day (604-page Madani Mushaf), daily average, khatma progress and history, Surah Al-Kahf on Fridays.
- **Istighfar & Salawat:** type a count or use +10 / +33 / +100.
- **Reports:** week / month / year / all time, prayer status breakdown, Sunnah, Witr & Qiyam, athkar, Quran, dhikr charts, streaks and a 16-week heatmap.
- **Settings:** English / العربية (full right-to-left), Arabic-Indic or Western numerals, Hijri date, light/dark theme, daily goals, reminders, backup and restore to a file.

## Project layout

```
app/src/main/java/com/ibadati/app/
  data/Database.kt           Room: days, prayers, khatmas
  data/Worship.kt            Prayer, athkar and Quran definitions
  data/SettingsRepository.kt Language, numerals, theme, goals, reminders
  data/Backup.kt             JSON backup format
  logic/Stats.kt             Khatma progress, streaks, daily score, reports
  MainViewModel.kt           App state and actions
  MainActivity.kt            Tabs, settings, language & direction
  ui/Strings.kt              ALL app text in English and Arabic (edit wording here)
  ui/Lang.kt                 Numerals, Gregorian & Hijri dates
  ui/*Screen.kt              The screens
  notify/Reminders.kt        Daily / Friday reminder notifications
```

## Fonts

IBM Plex Sans Arabic and Amiri are bundled under the SIL Open Font License (see `licenses/`).
