package com.kd.classmate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kd.classmate.services.NotificationSchedulerImpl // Required to get the extras

object NotificationPublisher {
    private const val CHANNEL_ID = "classmate_reminders_channel"
    private const val CHANNEL_NAME = "Task Reminders"

    /** Creates the notification channel if it doesn't exist (required for Android 8.0+) */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Reminders for scheduled tasks."
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Builds and shows the notification */
    fun showNotification(context: Context, taskId: Int, title: String) {
        // 1. Ensure channel exists before building the notification
        createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use a generic Android resource icon (standard practice when R is unavailable)
        val iconId = android.R.drawable.ic_lock_idle_alarm

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(iconId)
            .setContentTitle("Task Due Soon!")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismisses notification when tapped
            .build()

        // Use the task ID as the unique notification ID
        notificationManager.notify(taskId, notification)
    }
}