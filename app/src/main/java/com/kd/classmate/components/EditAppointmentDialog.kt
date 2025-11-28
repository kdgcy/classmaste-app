package com.kd.classmate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kd.classmate.data.Task // NEW IMPORT
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    currentAppointment: Task,
    currentTime: LocalTime, // Edited time from ViewModel
    title: String, // Edited title from ViewModel
    isTimePickerVisible: Boolean,
    onTitleChange: (String) -> Unit,
    onTimePickerVisibilityChange: (Boolean) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit, // Delete handler
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

    // --- New Appointment Dialog ---
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit Appointment") },
        text = {
            Column(horizontalAlignment = Alignment.Start) {
                // Display Date (NOT EDITABLE)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = currentAppointment.dueDate?.format(dateFormatter) ?: "Date Not Set",
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White)
                }
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    )
}