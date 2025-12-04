package com.kd.classmate.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.Task
import com.kd.classmate.data.TaskRepository
import com.kd.classmate.data.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import com.kd.classmate.services.NotificationScheduler

// Define the UI state
data class CalendarUiState(
    val scheduledTasks: List<Task> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,

    // New Appointment Dialog State
    val isAppointmentDialogVisible: Boolean = false,
    val newAppointmentTitleInput: String = "",
    val newAppointmentTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
    val isTimePickerVisible: Boolean = false,

    //  Edit Appointment State
    val appointmentBeingEdited: Task? = null,
    val isEditAppointmentDialogVisible: Boolean = false,
    val editAppointmentTitleInput: String = "",
    val editAppointmentTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
)

class CalendarViewModel(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler // Inject Scheduler
) : ViewModel() {

    private val _allTasksFlow = repository.getAllTasks()
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    // NEW: Appointment Dialog Flows
    private val _isAppointmentDialogVisible = MutableStateFlow(false)
    private val _newAppointmentTitleInput = MutableStateFlow("")
    private val _newAppointmentTime = MutableStateFlow(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))
    private val _isTimePickerVisible = MutableStateFlow(false)

    //  Edit Appointment Flows
    private val _appointmentBeingEdited = MutableStateFlow<Task?>(null)
    private val _isEditAppointmentDialogVisible = MutableStateFlow(false)
    private val _editAppointmentTitleInput = MutableStateFlow("")
    private val _editAppointmentTime = MutableStateFlow(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))

    // Combine flows to provide filtered data to the UI
    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<CalendarUiState> = combine(
        listOf(
            _allTasksFlow, _selectedDate, _isAppointmentDialogVisible, _newAppointmentTitleInput,
            _newAppointmentTime, _isTimePickerVisible,
            _appointmentBeingEdited, _isEditAppointmentDialogVisible, _editAppointmentTitleInput, _editAppointmentTime // NEW FLOWS
        )
    ) { args -> // Lambda now accepts a single Array<Any?> argument
        val allTasks = args[0] as List<Task>
        val selectedDate = args[1] as LocalDate
        val isVisible = args[2] as Boolean
        val title = args[3] as String
        val time = args[4] as LocalTime
        val isTimeVisible = args[5] as Boolean

        val filteredTasks = allTasks
            .filter { it.type == TaskType.APPOINTMENT }
            .filter { !it.isCompleted && it.dueDate == selectedDate }
            .sortedWith(compareBy { it.dueTime })

        CalendarUiState(
            scheduledTasks = filteredTasks,
            selectedDate = selectedDate,
            isLoading = false,
            isAppointmentDialogVisible = args[2] as Boolean,
            newAppointmentTitleInput = args[3] as String,
            newAppointmentTime = args[4] as LocalTime,
            isTimePickerVisible = args[5] as Boolean,

            // Edit State
            appointmentBeingEdited = args[6] as Task?,
            isEditAppointmentDialogVisible = args[7] as Boolean,
            editAppointmentTitleInput = args[8] as String,
            editAppointmentTime = args[9] as LocalTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // --- Appointment Functions ---

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
                val newTask = Task(
                    title = title,
                    dueDate = date,
                    dueTime = time,
                    isCompleted = false,
                    type = TaskType.APPOINTMENT
                )
                val newId = repository.insertTask(newTask)
                notificationScheduler.schedule(newTask.copy(id = newId.toInt()))
                setAppointmentDialogVisibility(false)
            }
        }
    }

    // --- Update/Delete Functions  ---

    fun startEditAppointment(appointment: Task) {
        _appointmentBeingEdited.value = appointment
        _editAppointmentTitleInput.value = appointment.title
        _editAppointmentTime.value = appointment.dueTime ?: LocalTime.now().truncatedTo(ChronoUnit.MINUTES) // Use existing time or current time
        _isEditAppointmentDialogVisible.value = true
    }

    fun cancelEditAppointment() {
        _appointmentBeingEdited.value = null
        _editAppointmentTitleInput.value = ""
        _isEditAppointmentDialogVisible.value = false
    }

    fun setEditAppointmentTitle(title: String) {
        _editAppointmentTitleInput.value = title
    }

    fun setEditAppointmentTime(time: LocalTime) {
        _editAppointmentTime.value = time
    }

    // U - Update Operation
    fun saveEditedAppointment() {
        val appointment = _appointmentBeingEdited.value ?: return
        val newTitle = _editAppointmentTitleInput.value.trim()
        val newTime = _editAppointmentTime.value

        if (newTitle.isNotEmpty()) {
            viewModelScope.launch {
                val updatedAppointment = appointment.copy(
                    title = newTitle,
                    dueTime = newTime
                )
                repository.updateTask(updatedAppointment)
                notificationScheduler.schedule(updatedAppointment) // Reschedule alarm
                cancelEditAppointment()
            }
        }
    }

    // D - Delete Operation
    fun deleteAppointment() {
        val appointment = _appointmentBeingEdited.value ?: return
        viewModelScope.launch {
            notificationScheduler.cancel(appointment.id)
            repository.deleteTask(appointment)
            cancelEditAppointment()
        }
    }
}