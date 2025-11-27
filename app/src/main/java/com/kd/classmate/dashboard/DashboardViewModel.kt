package com.kd.classmate.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Define the UI state
data class DashboardUiState(
    val taskList: List<Task> = emptyList(),
    val isAddDialogVisible: Boolean = false, // Renamed for clarity
    val newTaskTitleInput: String = "",
    val taskBeingEdited: Task? = null, // NEW: The task currently being edited
    val editTaskTitleInput: String = "" // NEW: Input for the edit dialog
)

class DashboardViewModel(private val repository: TaskRepository) : ViewModel() {

    // Internal mutable state flows for UI-specific controls
    private val _isAddDialogVisible = MutableStateFlow(false)
    private val _newTaskTitleInput = MutableStateFlow("")
    private val _taskBeingEdited = MutableStateFlow<Task?>(null) // NEW
    private val _editTaskTitleInput = MutableStateFlow("") // NEW

    // Combine the task list flow with the UI control flows
    val uiState: StateFlow<DashboardUiState> = repository.getAllTasks()
        .map { taskList ->
            DashboardUiState(taskList = taskList)
        }
        .combine(_isAddDialogVisible) { uiState, isVisible ->
            uiState.copy(isAddDialogVisible = isVisible)
        }
        .combine(_newTaskTitleInput) { uiState, input ->
            uiState.copy(newTaskTitleInput = input)
        }
        .combine(_taskBeingEdited) { uiState, task -> // NEW Combine
            uiState.copy(taskBeingEdited = task)
        }
        .combine(_editTaskTitleInput) { uiState, input -> // NEW Combine
            uiState.copy(editTaskTitleInput = input)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    // --- State Update Functions ---

    fun setAddDialogVisibility(isVisible: Boolean) { // Renamed
        _isAddDialogVisible.value = isVisible
        if (!isVisible) {
            _newTaskTitleInput.value = ""
        }
    }

    fun setNewTaskTitleInput(input: String) {
        _newTaskTitleInput.value = input
    }

    // NEW: Functions to manage the Edit Dialog state
    fun startEdit(task: Task) {
        _taskBeingEdited.value = task
        _editTaskTitleInput.value = task.title
    }

    fun cancelEdit() {
        _taskBeingEdited.value = null
        _editTaskTitleInput.value = ""
    }

    fun setEditTaskTitleInput(input: String) {
        _editTaskTitleInput.value = input
    }

    // --- CRUD Functions ---

    // CREATE (C)
    fun addTask() {
        // ... (Logic remains the same)
        val title = _newTaskTitleInput.value.trim()
        if (title.isNotEmpty()) {
            viewModelScope.launch {
                val newTask = Task(title = title)
                repository.insertTask(newTask)
                setAddDialogVisibility(false) // Renamed function
            }
        }
    }

    // NEW: UPDATE (U) - Function to save the new title
    fun saveEditedTask() {
        val task = _taskBeingEdited.value ?: return
        val newTitle = _editTaskTitleInput.value.trim()

        if (newTitle.isNotEmpty() && newTitle != task.title) {
            viewModelScope.launch {
                // Create a copy of the task with the updated title
                val updatedTask = task.copy(title = newTitle)
                repository.updateTask(updatedTask)
                cancelEdit() // Close the dialog and reset state
            }
        } else if (newTitle.isNotEmpty()) {
            // If the title is the same but the user pressed save, just close the dialog
            cancelEdit()
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