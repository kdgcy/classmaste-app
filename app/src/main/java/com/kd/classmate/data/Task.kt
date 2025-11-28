package com.kd.classmate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

enum class TaskType {
    TASK, // Created on the Dashboard (a To-Do item)
    APPOINTMENT // Created on the Calendar screen (a schedule item)
}

@Entity(tableName = "taskTable")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,

    // NEW FIELDS for Date and Time
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,

    // 🌟 NEW FIELD: To track the origin 🌟
    val type: TaskType = TaskType.TASK
)
