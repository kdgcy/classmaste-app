// File: AddTaskDialog.kt (UPDATED WITH COMBINED PICKER)

package com.kd.classmate.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

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

    // 🌟 FIX 1: Use rememberDatePickerState and rememberTimePickerState with stable initial values
    // We only initialize these once. Updates from the ViewModel won't reset them mid-scroll.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = remember(isDatePickerVisible) {
            selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: Instant.now().toEpochMilli()
        }
    )

    val timePickerState = rememberTimePickerState(
        initialHour = remember(isDatePickerVisible) { selectedTime?.hour ?: LocalTime.now().hour },
        initialMinute = remember(isDatePickerVisible) { selectedTime?.minute ?: LocalTime.now().minute }
    )

    if (isDatePickerVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                onDatePickerVisibilityChange(false)
                isSelectingTime = false
            }
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                // 🌟 FIX 2: Use a key with AnimatedContent to prevent layout thrashing during transition
                AnimatedContent(
                    targetState = isSelectingTime,
                    label = "PickerTransition"
                ) { selectingTime ->
                    if (!selectingTime) {
                        // Date View
                        DatePicker(
                            state = datePickerState,
                            title = null,
                            headline = null,
                            showModeToggle = false // Reduces UI complexity
                        )
                    } else {
                        // Time View
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
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
                        // 🌟 FIX 3: Local display logic to avoid flickering
                        val displayTime = if (isSelectingTime) {
                            String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        } else {
                            selectedTime?.toString() ?: "No"
                        }
                        Text(text = if (isSelectingTime) "Confirm" else displayTime)
                    },
                    modifier = Modifier.clickable {
                        if (isSelectingTime) {
                            // Only update ViewModel when transitioning away from clock
                            onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        }
                        isSelectingTime = !isSelectingTime
                    }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onDatePickerVisibilityChange(false) }) { Text("CANCEL") }
                    Button(onClick = {
                        // 🌟 FINAL SYNC: Push all data to ViewModel only once
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                        }
                        onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))

                        onDatePickerVisibilityChange(false)
                        isSelectingTime = false
                    }) { Text("DONE") }
                }
            }
        }
    }

    // MAIN ALERT DIALOG
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
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    }
                )
                if (selectedDate != null) {
                    Text(
                        text = "Due: $selectedDate ${selectedTime ?: ""}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = { Button(onClick = onAddClick, enabled = isAddButtonEnabled) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}