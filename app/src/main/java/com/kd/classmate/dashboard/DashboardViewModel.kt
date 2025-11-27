package com.kd.classmate.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow // NEW
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // NEW
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Define the UI state
data class DashboardUiState(
    val taskList: List<Task> = emptyList(),
    val isDialogVisible: Boolean = false, // NEW: State for dialog visibility
    val newTaskTitleInput: String = "" // NEW: State for text field input
)

class DashboardViewModel(private val repository: TaskRepository) : ViewModel() {

    // Internal mutable state flows for UI-specific controls
    private val _isDialogVisible = MutableStateFlow(false)
    private val _newTaskTitleInput = MutableStateFlow("")

    // Combine the task list flow with the UI control flows
    val uiState: StateFlow<DashboardUiState> = repository.getAllTasks()
        .map { taskList ->
            // Initial map to just the task list
            DashboardUiState(taskList = taskList)
        }
        .combine(_isDialogVisible) { uiState, isVisible ->
            // Combine with dialog visibility
            uiState.copy(isDialogVisible = isVisible)
        }
        .combine(_newTaskTitleInput) { uiState, input ->
            // Combine with text input
            uiState.copy(newTaskTitleInput = input)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    // --- State Update Functions ---

    fun setDialogVisibility(isVisible: Boolean) {
        _isDialogVisible.value = isVisible
        // Reset input when dialog is closed
        if (!isVisible) {
            _newTaskTitleInput.value = ""
        }
    }

    fun setNewTaskTitleInput(input: String) {
        _newTaskTitleInput.value = input
    }

    // --- CRUD Functions ---

    // CREATE (C) - Updated to use the input state and close dialog
    fun addTask() {
        // Use trim to ensure title is not just whitespace
        val title = _newTaskTitleInput.value.trim()

        if (title.isNotEmpty()) {
            viewModelScope.launch {
                val newTask = Task(title = title)
                repository.insertTask(newTask)
                // Close the dialog and reset the state after successful insertion
                setDialogVisibility(false)
            }
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