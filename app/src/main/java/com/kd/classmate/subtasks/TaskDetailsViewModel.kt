// File: TaskDetailsViewModel.kt

package com.kd.classmate.subtasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Define the UI state for the TaskDetails screen
data class TaskDetailsUiState(
    val task: Task? = null,
    val title: String = "Loading...",
    val isLoading: Boolean = true
)

class TaskDetailsViewModel(
    private val repository: TaskRepository,
    private val taskId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState

    init {
        // Since TaskRepository doesn't have a getTaskById function,
        // we'll simulate fetching the correct task from the full list flow.
        // NOTE: In a complete application, TaskDao should have a getTaskById query.
        viewModelScope.launch {
            repository.getAllTasks()
                .collect { allTasks ->
                    val task = allTasks.find { it.id == taskId }

                    _uiState.update { currentState ->
                        currentState.copy(
                            task = task,
                            title = task?.title ?: "Task Not Found",
                            isLoading = false
                        )
                    }
                }
        }
    }
}