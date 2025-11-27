// File: TaskDetailsViewModelFactory.kt

package com.kd.classmate.subtasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kd.classmate.data.TaskRepository

class TaskDetailsViewModelFactory(
    private val repository: TaskRepository,
    private val taskId: Int // NEW: Pass the required ID
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailsViewModel::class.java)) {
            // Instantiate TaskDetailsViewModel with both dependencies
            return TaskDetailsViewModel(repository, taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}