package com.kd.classmate.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.NewAppointmentDialog
import com.kd.classmate.components.EditAppointmentDialog
import org.koin.androidx.compose.koinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(navController: NavController) {

    val viewModel: CalendarViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val today = LocalDate.now()

    // --- Dialogs (Edit/New) ---
    uiState.appointmentBeingEdited?.let { appointment ->
        if (uiState.isEditAppointmentDialogVisible) {
            EditAppointmentDialog(
                currentAppointment = appointment,
                currentTime = uiState.editAppointmentTime,
                title = uiState.editAppointmentTitleInput,
                isTimePickerVisible = uiState.isTimePickerVisible,
                onTitleChange = viewModel::setEditAppointmentTitle,
                onTimePickerVisibilityChange = viewModel::setTimePickerVisibility,
                onTimeSelected = viewModel::setEditAppointmentTime,
                onSave = viewModel::saveEditedAppointment,
                onDelete = viewModel::deleteAppointment,
                onCancel = viewModel::cancelEditAppointment
            )
        }
    }

    if (uiState.isAppointmentDialogVisible) {
        NewAppointmentDialog(
            selectedDate = uiState.selectedDate,
            currentTime = uiState.newAppointmentTime,
            title = uiState.newAppointmentTitleInput,
            isTimePickerVisible = uiState.isTimePickerVisible,
            onTitleChange = viewModel::setNewAppointmentTitle,
            onTimePickerVisibilityChange = viewModel::setTimePickerVisibility,
            onTimeSelected = viewModel::setNewAppointmentTime,
            onSave = viewModel::saveNewAppointment,
            onCancel = { viewModel.setAppointmentDialogVisibility(false) }
        )
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(text = "Schedule") },
                    actions = {
                        IconButton(onClick = { viewModel.setAppointmentDialogVisibility(true) }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                        }
                    }
                )
                // THE VIEW SWITCHER TABS
                TabRow(selectedTabIndex = uiState.currentView.ordinal) {
                    CalendarView.values().forEach { view ->
                        Tab(
                            selected = uiState.currentView == view,
                            onClick = { viewModel.setCalendarView(view) },
                            text = { Text(view.name) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // DYNAMIC CONTENT SWITCHING
            when (uiState.currentView) {
                CalendarView.MONTH -> {
                    MonthViewContent(uiState, viewModel, today)
                }
                CalendarView.WEEK -> {
                    WeekView(
                        uiState = uiState,
                        onDateSelected = { viewModel.setSelectedDate(it) },
                        onTaskClick = { viewModel.startEditAppointment(it) }
                    )
                }
                CalendarView.DAY -> {
                    DayView(
                        uiState = uiState,
                        onDateChanged = { newDate -> viewModel.setSelectedDate(newDate) }, // Update ViewModel on swipe
                        onTaskClick = { viewModel.startEditAppointment(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthViewContent(uiState: CalendarUiState, viewModel: CalendarViewModel, today: LocalDate) {
    val firstDayOfMonth = uiState.selectedDate.with(TemporalAdjusters.firstDayOfMonth())
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val startDayOffset = if (firstDayOfMonth.dayOfWeek == DayOfWeek.SUNDAY) 0 else firstDayOfMonth.dayOfWeek.value
    val calendarDays = mutableListOf<LocalDate?>().apply {
        repeat(startDayOffset) { add(null) }
        for (day in 1..daysInMonth) { add(firstDayOfMonth.withDayOfMonth(day)) }
    }
    val numRows = (calendarDays.size + 6) / 7

    Column {
        // Month Navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.setSelectedDate(uiState.selectedDate.minusMonths(1)) }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = null)
            }
            Text(uiState.selectedDate.format(monthYearFormatter), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { viewModel.setSelectedDate(uiState.selectedDate.plusMonths(1)) }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
        }

        // Grid
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(day, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                }
            }
            repeat(numRows) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { colIndex ->
                        val cellIndex = rowIndex * 7 + colIndex
                        val date = calendarDays.getOrNull(cellIndex)
                        Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            date?.let { dayDate ->
                                CalendarDayItem(dayDate, uiState, today, viewModel)
                            }
                        }
                    }
                }
            }
        }

        // Daily Schedule Header and List
        Text("Schedule for ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}", Modifier.padding(16.dp))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (uiState.scheduledTasks.isEmpty()) {
                item { Text("No appointments scheduled.") }
            } else {
                items(uiState.scheduledTasks) { appointment ->
                    Card(Modifier.fillMaxWidth().clickable { viewModel.startEditAppointment(appointment) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(appointment.title, style = MaterialTheme.typography.titleMedium)
                            Text(appointment.dueTime?.format(timeFormatter) ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(date: LocalDate, uiState: CalendarUiState, today: LocalDate, viewModel: CalendarViewModel) {
    val isSelected = date == uiState.selectedDate
    val isPastDate = date.isBefore(today)
    val hasAppointment = uiState.allAppointmentDates.contains(date)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(36.dp).clickable(enabled = !isPastDate) { viewModel.setSelectedDate(date) },
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isPastDate) Color.LightGray else MaterialTheme.colorScheme.onSurface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (hasAppointment && !isPastDate) {
            Box(Modifier.padding(top = 2.dp).size(4.dp).background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary, CircleShape))
        } else {
            Spacer(Modifier.height(6.dp))
        }
    }
}