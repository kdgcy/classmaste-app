package com.kd.classmate.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import com.kd.classmate.data.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay
import com.kd.classmate.services.NotificationScheduler

// Define the UI state
data class DashboardUiState(
    val taskList: List<Task> = emptyList(),
    val isAddDialogVisible: Boolean = false,
    val newTaskTitleInput: String = "",
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val isDatePickerVisible: Boolean = false,
    val isTimePickerVisible: Boolean = false,
    val taskInContext: Task? = null
)

class DashboardViewModel(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    // Internal mutable state flows
    private val _isAddDialogVisible = MutableStateFlow(false)
    private val _newTaskTitleInput = MutableStateFlow("")
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _selectedTime = MutableStateFlow<LocalTime?>(null)
    private val _isDatePickerVisible = MutableStateFlow(false)
    private val _isTimePickerVisible = MutableStateFlow(false)
    private val _taskInContext = MutableStateFlow<Task?>(null)



    val uiState: StateFlow<DashboardUiState> = repository.getAllTasks()
        .map { allTasks ->
            val dashboardTasks = allTasks.filter { task ->
                task.type == TaskType.TASK
            }
            DashboardUiState(taskList = dashboardTasks)
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
        .combine(_taskInContext) { uiState, task ->
            uiState.copy(taskInContext = task)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    // ---  Context Menu Functions ---

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
            viewModelScope.launch {
                delay(300L)
                setTimePickerVisibility(true)
            }
        }
    }

    fun updateSelectedTime(time: LocalTime) {
        _selectedTime.value = time.withSecond(0).withNano(0)
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
                // 🌟 FIX 1: Ensure TaskType.TASK is set when constructing the Task 🌟
                val newTask = Task(
                    title = title,
                    dueDate = date,
                    dueTime = time,
                    type = TaskType.TASK // CRITICAL: Identify this task as belonging to the Dashboard
                )

                val newId = repository.insertTask(newTask)

                // Schedule the notification if date and time were set
                if (date != null && time != null) {
                    val scheduledTask = newTask.copy(id = newId.toInt())
                    notificationScheduler.schedule(scheduledTask)
                }

                setAddDialogVisibility(false)
                _selectedDate.value = null
                _selectedTime.value = null
            }
        }
    }


    // UPDATE (U) - for marking a task complete/incomplete (REMAINS)
    // UPDATE (U) - for marking a task complete/incomplete (REMAINS)
    fun updateTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updatedTask)
        }
    }

    // DELETE (D) (REMAINS)
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            // Cancel the notification before deleting the task
            notificationScheduler.cancel(task.id)
            repository.deleteTask(task)
        }
    }
}