package com.kd.classmate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    selectedDate: LocalDate,
    currentTime: LocalTime,
    title: String,
    isTimePickerVisible: Boolean,
    onTitleChange: (String) -> Unit,
    onTimePickerVisibilityChange: (Boolean) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val isSaveButtonEnabled = title.isNotBlank()
    val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    // --- Time Picker Dialog ---
    if (isTimePickerVisible) {
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute
        )
        TimePickerDialog(
            title = { Text(text = "Select Appointment Time") },
            onDismissRequest = { onTimePickerVisibilityChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        onTimeSelected(selectedTime)
                        onTimePickerVisibilityChange(false)
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onTimePickerVisibilityChange(false) }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    // --- Appointment Dialog ---
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("New Appointment") },
        text = {
            Column(horizontalAlignment = Alignment.Start) {
                // Display Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = selectedDate.format(dateFormatter),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Time Input Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTimePickerVisibilityChange(true) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                        Text(
                            text = currentTime.format(timeFormatter),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = isSaveButtonEnabled
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}