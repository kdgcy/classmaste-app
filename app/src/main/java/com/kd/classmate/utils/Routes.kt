package com.kd.classmate.utils

object Routes {
    val dashboard = "dashboard"
    val calendar = "calendar"
    val pomodoro = "pomodoro"
    val appsettings = "appSettings"
    val splash = "splash"
    val taskDetails = "taskDetails/{taskId}"

    // Helper function to create the actual path with the task ID
    fun taskDetailsPath(taskId: Int) = "taskDetails/$taskId"
}