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
import java.time.LocalDate
import java.time.LocalTime

// Define the UI state (UPDATED)
data class DashboardUiState(
    val taskList: List<Task> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val newTaskTitleInput: String = "",
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val isDatePickerVisible: Boolean = false,
    val isTimePickerVisible: Boolean = false,
    // NEW: State for the task currently being long-pressed (Context Menu)
    val taskInContext: Task? = null
)

class DashboardViewModel(private val repository: TaskRepository) : ViewModel() {

    // Internal mutable state flows (CLEANED UP)
    private val _isAddDialogVisible = MutableStateFlow(false)
    private val _newTaskTitleInput = MutableStateFlow("")
    // Internal Date/Time flows
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedTime = MutableStateFlow<LocalTime?>(null)
    private val _isDatePickerVisible = MutableStateFlow(false)
    private val _isTimePickerVisible = MutableStateFlow(false)
    // NEW: Context Menu State
    private val _taskInContext = MutableStateFlow<Task?>(null)

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
        .combine(_selectedDate) { uiState, date ->
            uiState.copy(selectedDate = date)
        }
        .combine(_selectedTime) { uiState, time ->
            uiState.copy(selectedTime = time)
        }
        .combine(_isDatePickerVisible) { uiState, isVisible ->
            uiState.copy(isDatePickerVisible = isVisible)
        }
        .combine(_isTimePickerVisible) { uiState, isVisible ->
            uiState.copy(isTimePickerVisible = isVisible)
        }
        // NEW COMBINE: Context Menu State
        .combine(_taskInContext) { uiState, task ->
            uiState.copy(taskInContext = task)
        }
        // -----------------------------------------------------------------
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    // --- NEW: Context Menu Functions ---

    fun setTaskInContext(task: Task?) {
        _taskInContext.value = task
    }

    // --- Date/Time Update Functions ---

    fun setDatePickerVisibility(isVisible: Boolean) {
        _isDatePickerVisible.value = isVisible
    }

    fun setTimePickerVisibility(isVisible: Boolean) {
        _isTimePickerVisible.value = isVisible
    }

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        if (_selectedTime.value == null) {
            setTimePickerVisibility(true)
        }
    }

    fun updateSelectedTime(time: LocalTime) {
        _selectedTime.value = time
    }

    // --- State Update Functions ---

    fun setAddDialogVisibility(isVisible: Boolean) {
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
        val title = _newTaskTitleInput.value.trim()
        val date = _selectedDate.value
        val time = _selectedTime.value

        if (title.isNotEmpty()) {
            viewModelScope.launch {
                // val newTask = Task(title = title, dueDate = date, dueTime = time) // Placeholder until Task is updated
                val newTask = Task(title = title)
                repository.insertTask(newTask)

                setAddDialogVisibility(false)
                _selectedDate.value = null
                _selectedTime.value = null
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