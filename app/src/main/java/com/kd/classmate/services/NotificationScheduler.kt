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

        // 🌟 FIX: Wrap the system call in try-catch for stability 🌟
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, // Wake up the device
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Catches permission issues (like SCHEDULE_EXACT_ALARM)
            println("Security Exception scheduling alarm: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            // Catches any other runtime error during system interaction
            println("Runtime Exception scheduling alarm: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun cancel(taskId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
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