package com.kd.classmate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.DialogProperties
import com.kd.classmate.data.Task
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    currentAppointment: Task,
    currentTime: LocalTime,
    title: String,
    isTimePickerVisible: Boolean,
    onTitleChange: (String) -> Unit,
    onTimePickerVisibilityChange: (Boolean) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("h:mma")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    // --- Reuse your Time Picker logic here ---
    if (isTimePickerVisible) {
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.hour,
            initialMinute = currentTime.minute
        )
        TimePickerDialog(
            title = { Text(text = "Update Appointment Time") },
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

    AlertDialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), // Matched to NewAppointmentDialog
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Appointment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // 1. STYLED TITLE INPUT
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Appointment Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 2. CONTEXTUAL INFO (DATE & TIME)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Display Chip
                    AssistChip(
                        onClick = { /* Fixed date for editing current item */ },
                        label = { Text(currentAppointment.dueDate?.format(dateFormatter) ?: "Date Not Set") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )

                    // Time Selection Chip - Matches NewAppointmentDialog style
                    AssistChip(
                        onClick = { onTimePickerVisibilityChange(true) },
                        label = { Text(currentTime.format(timeFormatter)) },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: Delete Action
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Right Side: Cancel & Save
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onCancel) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = onSave,
                            enabled = title.isNotBlank(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}