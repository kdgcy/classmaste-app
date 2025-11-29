package com.kd.classmate.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kd.classmate.data.Task
import java.time.ZoneId

interface NotificationScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: Int)
}

class NotificationSchedulerImpl(private val context: Context) : NotificationScheduler {

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    // Key constants for Intent extras
    companion object {
        const val ACTION_REMINDER = "com.kd.classmate.ACTION_REMINDER"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
    }

    /** Creates a PendingIntent specific to a task ID */
    private fun getPendingIntent(task: Task): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)

            // 🌟 CRITICAL FIX: Explicitly set the component for reliable delivery 🌟
            component = ComponentName(context, NotificationReceiver::class.java)
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
        // ... (Existing scheduling logic remains the same) ...
        val dueDate = task.dueDate ?: return
        val dueTime = task.dueTime ?: return
        val triggerTime = dueDate.atTime(dueTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = getPendingIntent(task)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            println("Exception scheduling alarm: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun cancel(taskId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            // 🌟 FIX: Apply explicit component to cancellation Intent too 🌟
            action = ACTION_REMINDER
            component = ComponentName(context, NotificationReceiver::class.java)
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