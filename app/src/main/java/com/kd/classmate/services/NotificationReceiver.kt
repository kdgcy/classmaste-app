package com.kd.classmate.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kd.classmate.utils.NotificationPublisher

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Verify the unique action of the Intent
        if (intent.action != NotificationSchedulerImpl.ACTION_REMINDER) {
            return
        }

        val taskId = intent.getIntExtra(NotificationSchedulerImpl.EXTRA_TASK_ID, -1)
        val taskTitle = intent.getStringExtra(NotificationSchedulerImpl.EXTRA_TASK_TITLE)

        if (taskId != -1 && taskTitle != null) {
            NotificationPublisher.showNotification(context, taskId, taskTitle)
        }
    }
}