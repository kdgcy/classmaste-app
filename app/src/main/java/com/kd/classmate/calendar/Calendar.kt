package com.kd.classmate.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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


// Helper functions for formatting (omitted for brevity)
private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
private val today = LocalDate.now() // Define today here for reuse


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(navController: NavController){

    val viewModel: CalendarViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val today = LocalDate.now()

    // Determine the first day of the month currently displayed
    val firstDayOfMonth = uiState.selectedDate.with(TemporalAdjusters.firstDayOfMonth())
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val startDayOffset = if (firstDayOfMonth.dayOfWeek == DayOfWeek.SUNDAY) 0 else firstDayOfMonth.dayOfWeek.value
    val totalCells = daysInMonth + startDayOffset
    val numRows = (totalCells + 6) / 7

    // --- Appointment Dialog Host  ---
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

    // --- Appointment Dialog Host ---
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

    // Create a list of days, including leading nulls
    val calendarDays = mutableListOf<LocalDate?>()

    // Add leading empty slots
    for (i in 0 until startDayOffset) {
        calendarDays.add(null)
    }
    // Add days of the month
    for (day in 1..daysInMonth) {
        calendarDays.add(firstDayOfMonth.withDayOfMonth(day))
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew,contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setAppointmentDialogVisibility(true) }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- Monthly Navigation and Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.setSelectedDate(uiState.selectedDate.minusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Month")
                }
                Text(
                    text = uiState.selectedDate.format(monthYearFormatter),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = { viewModel.setSelectedDate(uiState.selectedDate.plusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month")
                }
            }

            // --- Calendar Grid ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Day of Week Header (S, M, T, W, T, F, S)
                Row(modifier = Modifier.fillMaxWidth()) {
                    // FIX: Render days starting from Sunday
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEach { dayLetter ->
                        Text(
                            text = dayLetter,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Days Grid
                repeat(numRows) { rowIndex ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { dayOfWeekIndex ->
                            val cellIndex = rowIndex * 7 + dayOfWeekIndex
                            val dayDate = calendarDays.getOrNull(cellIndex)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f), // Make cells square
                                contentAlignment = Alignment.Center
                            ) {
                                dayDate?.let { date ->
                                    val isSelected = date == uiState.selectedDate
                                    // Check if the date is in the past
                                    val isPastDate = date.isBefore(today)

                                    val clickEnabled = !isPastDate

                                    // Determine colors
                                    val surfaceColor = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isPastDate -> Color.Transparent
                                        else -> Color.Transparent
                                    }

                                    val contentColor = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isPastDate -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) // Gray out text
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .size(36.dp)
                                            // Click only if NOT a past date
                                            .clickable(enabled = clickEnabled) { viewModel.setSelectedDate(date) },
                                        shape = CircleShape,
                                        color = surfaceColor,
                                        contentColor = contentColor
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Schedule List Header ---
            Text(
                text = "Schedule for ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )

            // --- Task List for the Selected Date ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isLoading) {
                    item { Text("Loading schedule...") }
                } else if (uiState.scheduledTasks.isEmpty()) {
                    item { Text("No appointments scheduled for this date.") }
                } else {
                    items(uiState.scheduledTasks) { appointment ->
                        Card(
                            // Make card clickable to start edit process
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.startEditAppointment(appointment) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = appointment.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                appointment.dueTime?.let { time ->
                                    Text(
                                        text = time.format(timeFormatter),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}