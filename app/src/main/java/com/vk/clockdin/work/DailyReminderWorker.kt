// Location: app/src/main/java/com/vk/clockdin/work/DailyReminderWorker.kt
package com.vk.clockdin.work

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vk.clockdin.ClockdinApp
import com.vk.clockdin.MainActivity
import com.vk.clockdin.R
import com.vk.clockdin.domain.repository.MoodEntryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that fires once daily at the user's preferred time.
 * If the user has already painted an orb for today, the notification is skipped.
 */
class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderWorkerEntryPoint {
        fun moodEntryRepository(): MoodEntryRepository
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                ReminderWorkerEntryPoint::class.java
            )
            val repo = entryPoint.moodEntryRepository()
            val todayStr = LocalDate.now().toString()

            // Check if user already recorded an entry today
            val todayEntries = repo.getEntriesForDate(todayStr).firstOrNull() ?: emptyList()
            if (todayEntries.isNotEmpty()) {
                // Already recorded today, skip notification
                return Result.success()
            }

            // Send notification
            sendReminderNotification()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendReminderNotification() {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) {
            return
        }

        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, ClockdinApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(applicationContext.getString(R.string.reminder_notification_title))
            .setContentText(applicationContext.getString(R.string.reminder_notification_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Missing POST_NOTIFICATIONS permission
        }
    }

    companion object {
        const val WORK_NAME = "clockdin_daily_reminder_work"
        private const val NOTIFICATION_ID = 1001

        /**
         * Schedules a recurring 24-hour periodic work request with initial delay targeting hour:minute.
         */
        fun schedule(context: Context, hour: Int, minute: Int) {
            val now = LocalDateTime.now()
            var target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (target.isBefore(now) || target.isEqual(now)) {
                target = target.plusDays(1)
            }

            val initialDelayMs = Duration.between(now, target).toMillis()

            val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        /**
         * Cancels the scheduled daily reminder work request.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
