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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import com.kd.classmate.services.NotificationScheduler

enum class CalendarView { DAY, WEEK, MONTH }

data class CalendarUiState(
    val currentView: CalendarView = CalendarView.MONTH,
    val scheduledTasks: List<Task> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val allAppointmentDates: Set<LocalDate> = emptySet(),
    val isAppointmentDialogVisible: Boolean = false,
    val newAppointmentTitleInput: String = "",
    val newAppointmentTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
    val isTimePickerVisible: Boolean = false,
    val appointmentBeingEdited: Task? = null,
    val isEditAppointmentDialogVisible: Boolean = false,
    val editAppointmentTitleInput: String = "",
    val editAppointmentTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
)

class CalendarViewModel(
    private val repository: TaskRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _currentView = MutableStateFlow(CalendarView.MONTH)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _isAppointmentDialogVisible = MutableStateFlow(false)
    private val _newAppointmentTitleInput = MutableStateFlow("")
    private val _newAppointmentTime = MutableStateFlow(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))
    private val _isTimePickerVisible = MutableStateFlow(false)
    private val _appointmentBeingEdited = MutableStateFlow<Task?>(null)
    private val _isEditAppointmentDialogVisible = MutableStateFlow(false)
    private val _editAppointmentTitleInput = MutableStateFlow("")
    private val _editAppointmentTime = MutableStateFlow(LocalTime.now().truncatedTo(ChronoUnit.MINUTES))

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<CalendarUiState> = combine(
        repository.getAllTasks(),
        _selectedDate,
        _currentView,
        _isAppointmentDialogVisible,
        _newAppointmentTitleInput,
        _newAppointmentTime,
        _isTimePickerVisible,
        _appointmentBeingEdited,
        _isEditAppointmentDialogVisible,
        _editAppointmentTitleInput,
        _editAppointmentTime
    ) { args ->
        val allTasks = args[0] as List<Task>
        val selectedDate = args[1] as LocalDate
        val currentView = args[2] as CalendarView

        // 1. Get dots for the month grid
        val allAppointmentDates = allTasks
            .filter { it.type == TaskType.APPOINTMENT && !it.isCompleted }
            .mapNotNull { it.dueDate }
            .toSet()

        // 2. Filter tasks based on the ACTIVE VIEW
        val filteredTasks = allTasks
            .filter { it.type == TaskType.APPOINTMENT && !it.isCompleted }
            .filter { task ->
                when (currentView) {
                    CalendarView.DAY -> task.dueDate == selectedDate
                    CalendarView.WEEK -> {
                        val start = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                        val end = start.plusDays(6)
                        task.dueDate != null && !task.dueDate.isBefore(start) && !task.dueDate.isAfter(end)
                    }
                    CalendarView.MONTH -> task.dueDate == selectedDate // Month view shows list for selected day
                }
            }
            .sortedBy { it.dueTime }

        CalendarUiState(
            currentView = currentView,
            scheduledTasks = filteredTasks,
            selectedDate = selectedDate,
            allAppointmentDates = allAppointmentDates,
            isLoading = false,
            isAppointmentDialogVisible = args[3] as Boolean,
            newAppointmentTitleInput = args[4] as String,
            newAppointmentTime = args[5] as LocalTime,
            isTimePickerVisible = args[6] as Boolean,
            appointmentBeingEdited = args[7] as Task?,
            isEditAppointmentDialogVisible = args[8] as Boolean,
            editAppointmentTitleInput = args[9] as String,
            editAppointmentTime = args[10] as LocalTime
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun setCalendarView(view: CalendarView) { _currentView.value = view }
    fun setSelectedDate(date: LocalDate) { _selectedDate.value = date }

    fun setAppointmentDialogVisibility(isVisible: Boolean) {
        _isAppointmentDialogVisible.value = isVisible
        if (!isVisible) {
            _newAppointmentTitleInput.value = ""
            _newAppointmentTime.value = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
            _isTimePickerVisible.value = false
        }
    }

    fun setNewAppointmentTitle(title: String) { _newAppointmentTitleInput.value = title }
    fun setTimePickerVisibility(isVisible: Boolean) { _isTimePickerVisible.value = isVisible }
    fun setNewAppointmentTime(time: LocalTime) { _newAppointmentTime.value = time }

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

    fun startEditAppointment(appointment: Task) {
        _appointmentBeingEdited.value = appointment
        _editAppointmentTitleInput.value = appointment.title
        _editAppointmentTime.value = appointment.dueTime ?: LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
        _isEditAppointmentDialogVisible.value = true
    }

    fun cancelEditAppointment() {
        _appointmentBeingEdited.value = null
        _editAppointmentTitleInput.value = ""
        _isEditAppointmentDialogVisible.value = false
    }

    fun setEditAppointmentTitle(title: String) { _editAppointmentTitleInput.value = title }
    fun setEditAppointmentTime(time: LocalTime) { _editAppointmentTime.value = time }

    fun saveEditedAppointment() {
        val appointment = _appointmentBeingEdited.value ?: return
        val newTitle = _editAppointmentTitleInput.value.trim()
        val newTime = _editAppointmentTime.value

        if (newTitle.isNotEmpty()) {
            viewModelScope.launch {
                val updatedAppointment = appointment.copy(title = newTitle, dueTime = newTime)
                repository.updateTask(updatedAppointment)
                notificationScheduler.schedule(updatedAppointment)
                cancelEditAppointment()
            }
        }
    }

    fun deleteAppointment() {
        val appointment = _appointmentBeingEdited.value ?: return
        viewModelScope.launch {
            notificationScheduler.cancel(appointment.id)
            repository.deleteTask(appointment)
            cancelEditAppointment()
        }
    }
}