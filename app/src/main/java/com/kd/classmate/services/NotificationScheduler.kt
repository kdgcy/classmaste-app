package com.kd.classmate.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kd.classmate.data.Task
import com.kd.classmate.services.NotificationReceiver // Import the receiver
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

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Key constants for Intent extras
    companion object {
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
    }

    /** Creates a PendingIntent specific to a task ID */
    private fun getPendingIntent(task: Task): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
        }
        // Use task.id as the requestCode to uniquely identify the intent
        return PendingIntent.getBroadcast(
            context,
            task.id,
            intent,
            // Use FLAG_IMMUTABLE for Android 12+ security, and FLAG_UPDATE_CURRENT to replace existing alarm
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun schedule(task: Task) {
        val dueDate = task.dueDate ?: return // Cannot schedule without a date
        val dueTime = task.dueTime ?: return // Cannot schedule without a time

        // 1. Combine date and time into a single point in time (milliseconds)
        val triggerTime = dueDate
            .atTime(dueTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // 2. Schedule the alarm
        val pendingIntent = getPendingIntent(task)

        // Use setExactAndAllowWhileIdle for reliable scheduling
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, // Wake up the device
            triggerTime,
            pendingIntent
        )
    }

    override fun cancel(taskId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        // Intent must match the one used for scheduling
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE // FLAG_NO_CREATE means don't create if it doesn't exist
        )
        // If the alarm exists, cancel it
        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }
}