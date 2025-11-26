package com.kd.classmate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taskTable")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    val title: String,
    val isCompleted: Boolean = false,
)
