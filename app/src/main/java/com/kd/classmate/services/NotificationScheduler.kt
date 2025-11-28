package com.kd.classmate.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kd.classmate.data.Task
import com.kd.classmate.services.NotificationReceiver
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

interface NotificationScheduler {
    /** Schedules an alarm to fire at the task's due date/time. */
    fun schedule(task: Task)

    /** Cancels a previously scheduled alarm for a given task ID. */
    fun cancel(taskId: Int)
}

class NotificationSchedulerImpl(private val context: Context) : NotificationScheduler {

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    // Key constants for Intent extras
    companion object {
        const val ACTION_REMINDER = "com.kd.classmate.ACTION_REMINDER" // 🌟 NEW: Custom Action 🌟
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
    }

    /** Creates a PendingIntent specific to a task ID */
    private fun getPendingIntent(task: Task): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            // 🌟 FIX: Add unique custom action 🌟
            action = ACTION_REMINDER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
        }
        // Use task.id as the requestCode to uniquely identify the intent
        return PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            // Ensure FLAG_IMMUTABLE is present for API 31+ security, and FLAG_UPDATE_CURRENT to replace existing alarm
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun schedule(task: Task) {
        val dueDate = task.dueDate ?: return
        val dueTime = task.dueTime ?: return

        // 1. Combine date and time into a single point in time (milliseconds)
        val triggerTime = dueDate
            .atTime(dueTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // 2. Schedule the alarm
        val pendingIntent = getPendingIntent(task)

        // Use try-catch for system stability
        try {
            // Check for API 31+ restrictions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // If permission is revoked at runtime, fall back to inexact
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Catches any runtime error during system interaction
            println("Exception scheduling alarm: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun cancel(taskId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            // 🌟 FIX: Intent must match the one used for scheduling 🌟
            action = ACTION_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}