package com.kd.classmate.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Define the UI state
data class DashboardUiState(
    val taskList: List<Task> = emptyList(),
)

class DashboardViewModel(private val repository: TaskRepository) : ViewModel() {

    // Holds the UI state and collects the tasks from the repository
    val uiState: StateFlow<DashboardUiState> = repository.getAllTasks()
        .map { DashboardUiState(taskList = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    // --- CRUD Functions ---

    // CREATE (C)
    fun addTask(title: String) {
        viewModelScope.launch {
            val newTask = Task(title = title)
            repository.insertTask(newTask)
        }
    }

    // UPDATE (U) - for marking a task complete/incomplete
    fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            // Create a copy of the task with the updated completion status
            val updatedTask = task.copy(isCompleted = isCompleted)
            repository.updateTask(updatedTask)
        }
    }

    // DELETE (D)
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}