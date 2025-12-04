// File: AddTaskDialog.kt (REVISION)

package com.kd.classmate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


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
    isTimePickerVisible: Boolean,
    onDatePickerVisibilityChange: (Boolean) -> Unit,
    onTimePickerVisibilityChange: (Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isAddButtonEnabled = taskTitle.isNotBlank()

    // -------------------------------------------------------------
    // 1. PICKER DIALOGS
    // -------------------------------------------------------------

    // Date Picker
    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            // Set initial date to today
            initialSelectedDateMillis = Instant.now().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { onDatePickerVisibilityChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            onDateSelected(selectedDate)
                        }
                        onDatePickerVisibilityChange(false)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDatePickerVisibilityChange(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker
    if (isTimePickerVisible) {
        val timePickerState = rememberTimePickerState(
            initialHour = LocalTime.now().hour,
            initialMinute = LocalTime.now().minute
        )
        TimePickerDialog(
            // Add the required 'title' parameter
            title = { Text(text = "Select Time") },
            onDismissRequest = { onTimePickerVisibilityChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        onTimeSelected(selectedTime)
                        onTimePickerVisibilityChange(false)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { onTimePickerVisibilityChange(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }


    // Main Alert Dialog
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),

                    // TRAILING ICONS
                    trailingIcon = {
                        Row {
                            // Calendar Icon (Due Date)
                            IconButton(onClick = { onDatePickerVisibilityChange(true) }) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "Set Due Date"
                                )
                            }
                            // Alarm Icon (Reminder)
                            IconButton(onClick = { onTimePickerVisibilityChange(true) }) {
                                Icon(
                                    imageVector = Icons.Filled.Alarm,
                                    contentDescription = "Set Reminder"
                                )
                            }
                        }
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isAddButtonEnabled) {
                                onAddClick()
                                keyboardController?.hide()
                            }
                        }
                    )
                )

                // Display selected Date/Time
                if (selectedDate != null || selectedTime != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Due: ${selectedDate?.toString() ?: "N/A"} @ ${selectedTime?.truncatedTo(ChronoUnit.MINUTES) ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter a title for your new task.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onAddClick, enabled = isAddButtonEnabled) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}