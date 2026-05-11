package com.example.workoutlevelup.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.workoutlevelup.MainActivity
import java.util.concurrent.TimeUnit
import java.util.Calendar

const val CHANNEL_ID   = "workout_reminder_channel"
const val CHANNEL_NAME = "Workout Reminders"
const val NOTIFICATION_ID = 1001

class WorkoutReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        sendReminderNotification()
        return Result.success()
    }

    private fun sendReminderNotification() {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messages = listOf(
            "⚔️ The system is calling. Time to level up, Hunter.",
            "💪 No excuses. Your training awaits.",
            "🔥 Don't break your streak! Time to work out.",
            "🏋️ Jinwoo didn't get to S-Rank by skipping. Neither will you.",
            "⚡ Rise. Train. Level Up. Do it now."
        )
        val message = messages.random()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Workout Level Up 🏆")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily workout reminder to keep your streak alive"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val WORK_TAG = "workout_daily_reminder"

        /**
         * Schedules a daily reminder at [hourOfDay] (24h format).
         * Call from Application.onCreate or from a settings screen.
         */
        fun scheduleDailyReminder(context: Context, hourOfDay: Int = 18, minute: Int = 0) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            // If the time today has already passed, schedule for tomorrow
            if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancelReminders(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        }
    }
}
