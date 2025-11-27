package com.kd.classmate.data.subtaskdata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subtaskTable")
data class Subtask(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val parentTaskId: Int, // Foreign key linking to the parent Task
    val title: String,
    val isCompleted: Boolean = false,
)