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
    val newSubtaskTitleInput: String = "",

    // NEW: Subtask Edit States
    val subtaskBeingEdited: Subtask? = null,
    val isSubtaskEditDialogVisible: Boolean = false,
    val editSubtaskTitleInput: String = ""
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

    // NEW: Internal flows for Subtask Edit
    private val _subtaskBeingEdited = MutableStateFlow<Subtask?>(null)
    private val _isSubtaskEditDialogVisible = MutableStateFlow(false)
    private val _editSubtaskTitleInput = MutableStateFlow("")


    // Combine flows into the public StateFlow
    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<TaskDetailsUiState> = combine(
        listOf(
            _taskFlow, _isEditDialogVisible, _editTaskTitleInput, _isDeleteConfirmationVisible,
            _subtaskListFlow, _isSubtaskAddDialogVisible, _newSubtaskTitleInput,
            _subtaskBeingEdited, _isSubtaskEditDialogVisible, _editSubtaskTitleInput // NEW FLOWS
        )
    ) { args ->
        TaskDetailsUiState(
            task = args[0] as Task?,
            title = (args[0] as Task?)?.title ?: "Loading...",
            isLoading = args[0] == null,
            isEditDialogVisible = args[1] as Boolean,
            editTaskTitleInput = args[2] as String,
            isDeleteConfirmationVisible = args[3] as Boolean,
            subtaskList = args[4] as List<Subtask>,
            isSubtaskAddDialogVisible = args[5] as Boolean,
            newSubtaskTitleInput = args[6] as String,

            // NEW Subtask Edit State Initialization
            subtaskBeingEdited = args[7] as Subtask?,
            isSubtaskEditDialogVisible = args[8] as Boolean,
            editSubtaskTitleInput = args[9] as String
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskDetailsUiState()
    )


    init {
        viewModelScope.launch {
            repository.getAllTasks().collect { allTasks -> _taskFlow.value = allTasks.find { it.id == taskId } }
        }

        viewModelScope.launch {
            subtaskRepository.getSubtasksForTask(taskId).collect { subtasks -> _subtaskListFlow.value = subtasks }
        }
    }

    // --- NEW SUBTASK EDIT FUNCTIONS ---

    fun startEditSubtask(subtask: Subtask) {
        _subtaskBeingEdited.value = subtask
        _editSubtaskTitleInput.value = subtask.title
        _isSubtaskEditDialogVisible.value = true
    }

    fun cancelEditSubtask() {
        _subtaskBeingEdited.value = null
        _editSubtaskTitleInput.value = ""
        _isSubtaskEditDialogVisible.value = false
    }

    fun setEditSubtaskTitleInput(input: String) {
        _editSubtaskTitleInput.value = input
    }

    fun saveEditedSubtask() {
        val subtask = _subtaskBeingEdited.value ?: return
        val newTitle = _editSubtaskTitleInput.value.trim()

        if (newTitle.isNotEmpty() && newTitle != subtask.title) {
            viewModelScope.launch {
                val updatedSubtask = subtask.copy(title = newTitle)
                subtaskRepository.updateSubtask(updatedSubtask)
                cancelEditSubtask()
            }
        } else if (newTitle.isNotEmpty()) {
            cancelEditSubtask()
        }
    }

    // --- NEW SUBTASK DELETE FUNCTION ---

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch {
            subtaskRepository.deleteSubtask(subtask)
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