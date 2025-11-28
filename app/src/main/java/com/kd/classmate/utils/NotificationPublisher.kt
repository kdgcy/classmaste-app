package com.kd.classmate.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kd.classmate.services.NotificationSchedulerImpl
import com.kd.classmate.R // 🌟 ASSUMING: You have a resource file R 🌟

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

        // 🌟 FIX: Use a simple, reliable constant for the small icon 🌟
        // This constant is guaranteed to exist and is used if a custom drawable fails to resolve.
        val iconId = android.R.drawable.stat_notify_error

        // If you have a custom app icon (e.g., ic_stat_name) in your res/drawable:
        // val iconId = R.drawable.ic_notification_icon

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