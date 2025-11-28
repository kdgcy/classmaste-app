package com.kd.classmate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "taskTable")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,

    // NEW FIELDS for Date and Time
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null
)
