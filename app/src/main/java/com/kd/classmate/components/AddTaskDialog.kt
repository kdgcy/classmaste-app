// File: AddTaskDialog.kt (UPDATED WITH COMBINED PICKER)

package com.kd.classmate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.animation.AnimatedContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit,
    selectedDate: LocalDate?,
    selectedTime: LocalTime?,
    isDatePickerVisible: Boolean,
    onDatePickerVisibilityChange: (Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onTimePickerVisibilityChange: (Boolean) -> Unit,
) {
    val isAddButtonEnabled = taskTitle.isNotBlank()
    var isSelectingTime by remember { mutableStateOf(false) }

    // 🌟 FIX 1: Move states here so they are stable across recompositions
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: Instant.now().toEpochMilli()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: LocalTime.now().hour,
        initialMinute = selectedTime?.minute ?: LocalTime.now().minute
    )

    if (isDatePickerVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                onDatePickerVisibilityChange(false)
                isSelectingTime = false
            },
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                AnimatedContent(targetState = isSelectingTime, label = "PickerTransition") { selectingTime ->
                    if (!selectingTime) {
                        Column {
                            DatePicker(
                                state = datePickerState,
                                showModeToggle = false,
                                title = null,
                                headline = null
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SuggestionChip(
                                    onClick = { onDateSelected(LocalDate.now()) },
                                    label = { Text("Today") }
                                )
                                SuggestionChip(
                                    onClick = { onDateSelected(LocalDate.now().plusDays(1)) },
                                    label = { Text("Tomorrow") }
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Select Time", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            TimePicker(state = timePickerState)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                ListItem(
                    headlineContent = { Text("Time") },
                    leadingContent = { Icon(Icons.Default.Schedule, null) },
                    trailingContent = {
                        Text(
                            text = if (isSelectingTime) "Confirm Time" else (selectedTime?.toString() ?: "No"),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        // 🌟 FIX 2: Safely update time when switching back
                        if (isSelectingTime) {
                            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        }
                        isSelectingTime = !isSelectingTime
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDatePickerVisibilityChange(false) }) { Text("CANCEL") }
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                            }
                            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))

                            // 🌟 FIX 3: Order matters to avoid layout collisions
                            onDatePickerVisibilityChange(false)
                            isSelectingTime = false
                        }
                    ) { Text("DONE") }
                }
            }
        }
    }

    // Main Dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onTitleChange,
                    label = { Text("What needs to be done?") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { onDatePickerVisibilityChange(true) }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Set Date/Time")
                        }
                    }
                )

                if (selectedDate != null) {
                    val formattedTime = selectedTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "No time"
                    Text(
                        text = "Due: ${selectedDate} at $formattedTime",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onAddClick, enabled = isAddButtonEnabled) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}