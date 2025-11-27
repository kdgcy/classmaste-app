package com.kd.classmate.subtasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import com.kd.classmate.data.subtaskdata.Subtask
import com.kd.classmate.data.subtaskdata.SubtaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Define the UI state
data class TaskDetailsUiState(
    val task: Task? = null,
    val title: String = "Loading...",
    val isLoading: Boolean = true,
    val isEditDialogVisible: Boolean = false,
    val editTaskTitleInput: String = "",
    val isDeleteConfirmationVisible: Boolean = false,
    // Subtask States
    val subtaskList: List<Subtask> = emptyList(),
    val isSubtaskAddDialogVisible: Boolean = false,
    val newSubtaskTitleInput: String = ""
)

class TaskDetailsViewModel(
    private val repository: TaskRepository,
    private val subtaskRepository: SubtaskRepository,
    private val taskId: Int
) : ViewModel() {

    // Internal flows for task, edit, and delete state
    private val _taskFlow = MutableStateFlow<Task?>(null)
    private val _isEditDialogVisible = MutableStateFlow(false)
    private val _editTaskTitleInput = MutableStateFlow("")
    private val _isDeleteConfirmationVisible = MutableStateFlow(false)

    // Internal flows for Subtasks
    private val _subtaskListFlow = MutableStateFlow<List<Subtask>>(emptyList())
    private val _isSubtaskAddDialogVisible = MutableStateFlow(false)
    private val _newSubtaskTitleInput = MutableStateFlow("")

    // Combine flows into the public StateFlow
    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<TaskDetailsUiState> = combine(
        // FIX: Combine using a list of flows, which forces the compiler to use
        // the single-argument lambda (Array<Any?>) overload.
        listOf(
            _taskFlow,
            _isEditDialogVisible,
            _editTaskTitleInput,
            _isDeleteConfirmationVisible,
            _subtaskListFlow,
            _isSubtaskAddDialogVisible,
            _newSubtaskTitleInput
        )
    ) { args ->
        // Manually retrieve and cast each argument from the array
        TaskDetailsUiState(
            task = args[0] as Task?,
            title = (args[0] as Task?)?.title ?: "Loading...",
            isLoading = args[0] == null,
            isEditDialogVisible = args[1] as Boolean,
            editTaskTitleInput = args[2] as String,
            isDeleteConfirmationVisible = args[3] as Boolean,
            subtaskList = args[4] as List<Subtask>,
            isSubtaskAddDialogVisible = args[5] as Boolean,
            newSubtaskTitleInput = args[6] as String
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

        viewModelScope.launch {
            subtaskRepository.getSubtasksForTask(taskId)
                .collect { subtasks ->
                    _subtaskListFlow.value = subtasks
                }
        }
    }

    // --- SUBTASK STATE/CRUD FUNCTIONS ---
    fun setSubtaskAddDialogVisibility(isVisible: Boolean) {
        _isSubtaskAddDialogVisible.value = isVisible
        if (!isVisible) _newSubtaskTitleInput.value = ""
    }

    fun setNewSubtaskTitleInput(input: String) {
        _newSubtaskTitleInput.value = input
    }

    fun addSubtask() {
        val title = _newSubtaskTitleInput.value.trim()
        if (title.isNotEmpty()) {
            viewModelScope.launch {
                val newSubtask = Subtask(parentTaskId = taskId, title = title)
                subtaskRepository.insertSubtask(newSubtask)
                setSubtaskAddDialogVisibility(false)
            }
        }
    }

    fun updateSubtaskCompletion(subtask: Subtask, isCompleted: Boolean) {
        viewModelScope.launch {
            val updatedSubtask = subtask.copy(isCompleted = isCompleted)
            subtaskRepository.updateSubtask(updatedSubtask)
        }
    }

    // --- (Edit and Delete functions remain the same) ---
    fun showDeleteConfirmation() { _isDeleteConfirmationVisible.value = true }
    fun hideDeleteConfirmation() { _isDeleteConfirmationVisible.value = false }
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
    fun deleteTask() {
        val task = _taskFlow.value ?: return
        viewModelScope.launch {
            repository.deleteTask(task)
            hideDeleteConfirmation()
        }
    }
}