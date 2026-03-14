package com.kd.classmate.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kd.classmate.data.Task
import com.kd.classmate.data.subtaskdata.Subtask
import java.time.ZoneId
import com.kd.classmate.data.PreferenceManager

interface NotificationScheduler {
    fun schedule(task: Task)
    fun schedule(subtask: Subtask) // NEW: Support for subtasks
    fun cancel(taskId: Int, isSubtask: Boolean = false) // UPDATED: Toggle for subtasks
}

class NotificationSchedulerImpl(
    private val context: Context,
    private val preferenceManager: PreferenceManager
) : NotificationScheduler {

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    companion object {
        const val ACTION_REMINDER = "com.kd.classmate.ACTION_REMINDER"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"
        // Offset to prevent ID collisions between Tasks and Subtasks
        private const val SUBTASK_ID_OFFSET = 100000
    }

    private fun getPendingIntent(id: Int, title: String, isSubtask: Boolean): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_TASK_ID, id)
            putExtra(EXTRA_TASK_TITLE, if (isSubtask) "$title" else title)
            component = ComponentName(context, NotificationReceiver::class.java)
        }

        val requestCode = if (isSubtask) id + SUBTASK_ID_OFFSET else id

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun schedule(task: Task) {
        if (!preferenceManager.getMasterNotificationState().value) return
        val triggerTime = task.dueDate?.atTime(task.dueTime ?: return)?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: return
        performSchedule(triggerTime, getPendingIntent(task.id, task.title, false))
    }

    override fun schedule(subtask: Subtask) {
        if (!preferenceManager.getMasterNotificationState().value) return
        val triggerTime = subtask.dueDate?.atTime(subtask.dueTime ?: return)?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: return
        performSchedule(triggerTime, getPendingIntent(subtask.id, subtask.title, true))
    }

    private fun performSchedule(triggerTime: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun cancel(taskId: Int, isSubtask: Boolean) {
        val requestCode = if (isSubtask) taskId + SUBTASK_ID_OFFSET else taskId
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = ACTION_REMINDER
            component = ComponentName(context, NotificationReceiver::class.java)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}