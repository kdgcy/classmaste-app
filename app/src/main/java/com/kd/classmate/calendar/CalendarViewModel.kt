package com.kd.classmate.calendar

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
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

// Define the UI state
data class CalendarUiState(
    val scheduledTasks: List<Task> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,

    // NEW: New Appointment Dialog State
    val isAppointmentDialogVisible: Boolean = false,
    val newAppointmentTitleInput: String = "",
    val newAppointmentTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
    val isTimePickerVisible: Boolean = false,
)

class CalendarViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _allTasksFlow = repository.getAllTasks()
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    // NEW: Appointment Dialog Flows
    private val _isAppointmentDialogVisible = MutableStateFlow(false)
    private val _newAppointmentTitleInput = MutableStateFlow("")
    private val _newAppointmentTime = MutableStateFlow(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))
    private val _isTimePickerVisible = MutableStateFlow(false)


    // Combine flows to provide filtered data to the UI (FIXED AMBIGUITY USING ARRAY)
    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<CalendarUiState> = combine(
        listOf( // List of flows being combined (Total 6 flows)
            _allTasksFlow,
            _selectedDate,
            _isAppointmentDialogVisible,
            _newAppointmentTitleInput,
            _newAppointmentTime,
            _isTimePickerVisible
        )
    ) { args -> // Lambda now accepts a single Array<Any?> argument
        val allTasks = args[0] as List<Task>
        val selectedDate = args[1] as LocalDate
        val isVisible = args[2] as Boolean
        val title = args[3] as String
        val time = args[4] as LocalTime
        val isTimeVisible = args[5] as Boolean

        val filteredTasks = allTasks
            .filter { !it.isCompleted && it.dueDate == selectedDate }
            .sortedWith(compareBy { it.dueTime })

        CalendarUiState(
            scheduledTasks = filteredTasks,
            selectedDate = selectedDate,
            isLoading = false,

            isAppointmentDialogVisible = isVisible,
            newAppointmentTitleInput = title,
            newAppointmentTime = time,
            isTimePickerVisible = isTimeVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // --- NEW: Appointment Functions ---

    fun setAppointmentDialogVisibility(isVisible: Boolean) {
        _isAppointmentDialogVisible.value = isVisible
        // Reset state when closing
        if (!isVisible) {
            _newAppointmentTitleInput.value = ""
            _newAppointmentTime.value = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
            _isTimePickerVisible.value = false
        }
    }

    fun setNewAppointmentTitle(title: String) {
        _newAppointmentTitleInput.value = title
    }

    fun setTimePickerVisibility(isVisible: Boolean) {
        _isTimePickerVisible.value = isVisible
    }

    fun setNewAppointmentTime(time: LocalTime) {
        _newAppointmentTime.value = time
    }

    fun saveNewAppointment() {
        val title = _newAppointmentTitleInput.value.trim()
        val date = _selectedDate.value
        val time = _newAppointmentTime.value

        if (title.isNotEmpty()) {
            viewModelScope.launch {
                // Assuming we would schedule a notification here if implemented
                val newTask = Task(
                    title = title,
                    dueDate = date,
                    dueTime = time,
                    isCompleted = false
                )
                repository.insertTask(newTask)

                // Close and reset dialog state
                setAppointmentDialogVisibility(false)
            }
        }
    }
}