package com.kd.classmate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kd.classmate.pomodoro.PomodoroSettings

@Composable
fun PomodoroSettingsDialog(
    currentSettings: PomodoroSettings,
    onSave: (work: Long, shortBreak: Long, longBreak: Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Local mutable state for dialog inputs
    var workInput by remember { mutableStateOf(currentSettings.workDurationMinutes.toString()) }
    var shortBreakInput by remember { mutableStateOf(currentSettings.shortBreakMinutes.toString()) }
    var longBreakInput by remember { mutableStateOf(currentSettings.longBreakMinutes.toString()) }

    val isInputValid = workInput.toLongOrNull() != null && shortBreakInput.toLongOrNull() != null && longBreakInput.toLongOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Focus Times") },
        text = {
            Column {
                Text("Durations must be in minutes.", Modifier.padding(bottom = 8.dp))

                // Work Duration
                OutlinedTextField(
                    value = workInput,
                    onValueChange = { workInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Work Time (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Short Break Duration
                OutlinedTextField(
                    value = shortBreakInput,
                    onValueChange = { shortBreakInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Short Break (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Long Break Duration
                OutlinedTextField(
                    value = longBreakInput,
                    onValueChange = { longBreakInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Long Break (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isInputValid) {
                        onSave(
                            workInput.toLong(),
                            shortBreakInput.toLong(),
                            longBreakInput.toLong()
                        )
                    }
                },
                enabled = isInputValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}