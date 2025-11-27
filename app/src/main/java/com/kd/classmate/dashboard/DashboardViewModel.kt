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
    val isAddDialogVisible: Boolean = false,
    val newTaskTitleInput: String = ""
)

class DashboardViewModel(private val repository: TaskRepository) : ViewModel() {

    // Internal mutable state flows (CLEANED UP)
    private val _isAddDialogVisible = MutableStateFlow(false)
    private val _newTaskTitleInput = MutableStateFlow("")
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
        // REMOVED: combines for _taskBeingEdited and _editTaskTitleInput
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


    // UPDATE (U) - for marking a task complete/incomplete (REMAINS)
    fun updateTaskCompletion(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = isCompleted)
            repository.updateTask(updatedTask)
        }
    }

    // DELETE (D) (REMAINS, as it's a generic repository method)
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}