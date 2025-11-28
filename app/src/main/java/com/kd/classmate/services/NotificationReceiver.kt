package com.kd.classmate.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kd.classmate.utils.NotificationPublisher

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Check for required extras from the Intent (scheduled by NotificationScheduler)
        val taskId = intent.getIntExtra(NotificationSchedulerImpl.EXTRA_TASK_ID, -1)
        val taskTitle = intent.getStringExtra(NotificationSchedulerImpl.EXTRA_TASK_TITLE)

        // Only proceed if we have a valid ID and Title
        if (taskId != -1 && taskTitle != null) {
            // Call the publisher to display the notification
            NotificationPublisher.showNotification(context, taskId, taskTitle)
        }
    }
}