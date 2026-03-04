package com.kd.classmate.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.kd.classmate.components.DateTimePickerDialogs

import androidx.compose.material3.rememberDatePickerState

import androidx.compose.material3.rememberTimePickerState

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
    onCancel: () -> Unit,
    isDatePickerVisible: Boolean = false,
    onDatePickerVisibilityChange: (Boolean) -> Unit = {},
    onDateSelected: (LocalDate) -> Unit = {}
) {
    if (isTimePickerVisible) {
        // You likely have a helper for this, or use the standard Material3 TimePickerDialog
        // If you use your shared component:
        DateTimePickerDialogs(
            isDatePickerVisible = isDatePickerVisible,
            isTimePickerVisible = isTimePickerVisible,
            onDatePickerVisibilityChange = onDatePickerVisibilityChange,
            onTimePickerVisibilityChange = onTimePickerVisibilityChange,
            onDateSelected = onDateSelected,
            onTimeSelected = onTimeSelected,
            initialDate = selectedDate
        )
    }
    AlertDialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Allows custom width
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), // Modern extra-rounded corners
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New Appointment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // 1. STYLED TITLE INPUT
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Task or Event Name") },
                    placeholder = { Text("e.g., Capstone Meeting") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // 2. CONTEXTUAL INFO (DATE & TIME)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Display (Read-only)
                    AssistChip(
                        onClick = { onDatePickerVisibilityChange(true) },
                        label = { Text(selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))) },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )

                    // Time Selection Chip
                    AssistChip(
                        onClick = { onTimePickerVisibilityChange(true) },
                        label = { Text(currentTime.format(DateTimeFormatter.ofPattern("h:mma"))) },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Schedule")
                    }
                }
            }
        }
    }
}