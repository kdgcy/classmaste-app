package com.kd.classmate.utils

object Routes {
    val dashboard = "dashboard"
    // NEW: Define the route with a required argument
    val taskDetails = "taskDetails/{taskId}"

    // Helper function to create the actual path with the task ID
    fun taskDetailsPath(taskId: Int) = "taskDetails/$taskId"
}