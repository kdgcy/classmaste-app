package com.kd.classmate.subtasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Define the UI state for the TaskDetails screen
data class TaskDetailsUiState(
    val task: Task? = null,
    val title: String = "Loading...",
    val isLoading: Boolean = true,
    val isEditDialogVisible: Boolean = false,
    val editTaskTitleInput: String = "",
    // NEW: State for delete confirmation dialog
    val isDeleteConfirmationVisible: Boolean = false
)

class TaskDetailsViewModel(
    private val repository: TaskRepository,
    private val taskId: Int
) : ViewModel() {

    // Internal flows for current task and dialog states
    private val _taskFlow = MutableStateFlow<Task?>(null)
    private val _isEditDialogVisible = MutableStateFlow(false)
    private val _editTaskTitleInput = MutableStateFlow("")
    private val _isDeleteConfirmationVisible = MutableStateFlow(false)

    // Combine flows into the public StateFlow
    val uiState: StateFlow<TaskDetailsUiState> = combine(
        _taskFlow,
        _isEditDialogVisible,
        _editTaskTitleInput,
        _isDeleteConfirmationVisible
    ) { task, isEditVisible, editInput, isDeleteVisible ->
        TaskDetailsUiState(
            task = task,
            title = task?.title ?: "Loading...",
            isLoading = task == null,
            isEditDialogVisible = isEditVisible,
            editTaskTitleInput = editInput,
            isDeleteConfirmationVisible = isDeleteVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskDetailsUiState()
    )


    init {
        viewModelScope.launch {
            repository.getAllTasks()
                .collect { allTasks ->
                    _taskFlow.value = allTasks.find { it.id == taskId }
                }
        }
    }

    // --- DELETE DIALOG STATE MANAGEMENT ---
    fun showDeleteConfirmation() {
        _isDeleteConfirmationVisible.value = true
    }

    fun hideDeleteConfirmation() {
        _isDeleteConfirmationVisible.value = false
    }

    // --- EDIT STATE MANAGEMENT ---
    fun startEdit() {
        val task = _taskFlow.value ?: return
        _editTaskTitleInput.value = task.title
        _isEditDialogVisible.value = true
    }

    fun cancelEdit() {
        _isEditDialogVisible.value = false
        _editTaskTitleInput.value = ""
    }

    fun setEditTaskTitleInput(input: String) {
        _editTaskTitleInput.value = input
    }

    fun saveEditedTask() {
        val task = _taskFlow.value ?: return
        val newTitle = _editTaskTitleInput.value.trim()

        if (newTitle.isNotEmpty() && newTitle != task.title) {
            viewModelScope.launch {
                val updatedTask = task.copy(title = newTitle)
                repository.updateTask(updatedTask)
                cancelEdit()
            }
        } else if (newTitle.isNotEmpty()) {
            cancelEdit()
        }
    }

    // --- DELETE ACTION (Called after confirmation) ---
    fun deleteTask() {
        val task = _taskFlow.value ?: return
        viewModelScope.launch {
            repository.deleteTask(task)
            hideDeleteConfirmation()
        }
    }
}