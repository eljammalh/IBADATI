package com.ibadati.app.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ibadati.app.MainActivity
import com.ibadati.app.R
import com.ibadati.app.data.Reminder
import com.ibadati.app.data.SettingsRepository
import com.ibadati.app.ui.Lang
import com.ibadati.app.ui.reminderText
import com.ibadati.app.ui.fastingReminder
import com.ibadati.app.logic.Fasting
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

object Reminders {
    const val CHANNEL_ID = "ibadati_reminders"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Reminders · التذكيرات", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Athkar, Quran and Surah Al-Kahf reminders"
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Schedules (or cancels) a reminder. The Al-Kahf reminder repeats weekly on Friday; the rest daily. */
    fun schedule(context: Context, type: String, enabled: Boolean, hour: Int, minute: Int) {
        val wm = WorkManager.getInstance(context)
        val name = "reminder_$type"
        if (!enabled) {
            wm.cancelUniqueWork(name)
            return
        }
        val weekly = type == Reminder.KAHF
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(hour, minute)
        if (weekly) {
            next = now.toLocalDate().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)).atTime(hour, minute)
            if (!next.isAfter(now)) next = next.plusWeeks(1)
        } else if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        val delayMinutes = Duration.between(now, next).toMinutes()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(if (weekly) 7L else 1L, TimeUnit.DAYS)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf("type" to type))
            .build()
        wm.enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        val type = inputData.getString("type") ?: Reminder.MORNING
        val settings = SettingsRepository(ctx).settings.first()
        val lang = Lang(settings.arabic, settings.arabicDigits)
        val (title, text) = if (type == Reminder.FASTING) {
            // Only remind on the evening before a Monday, Thursday or White Day.
            val kinds = Fasting.kinds(LocalDate.now().plusDays(1).toEpochDay())
            if (kinds.isEmpty()) return Result.success()
            lang.fastingReminder(kinds)
        } else {
            lang.reminderText(type)
        }
        val id = 100 + Reminder.TYPES.indexOf(type).coerceAtLeast(0)
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            ctx, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(ctx, Reminders.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(id, notification)
        return Result.success()
    }
}
