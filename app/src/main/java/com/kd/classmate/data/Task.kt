package com.kd.classmate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

enum class TaskType {
    TASK, // For Dashboard.kt
    APPOINTMENT // For Calendar.kt
}

@Entity(tableName = "taskTable")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,

    // To track the origin, its either Task or Appointment
    val type: TaskType = TaskType.TASK
)
