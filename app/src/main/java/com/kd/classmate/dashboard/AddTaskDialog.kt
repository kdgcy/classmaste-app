package com.kd.classmate.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun AddTaskDialog(
    // State and event handlers from ViewModel
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss, // Dismiss when clicking outside
        title = {
            Text(text = "Add New Task")
        },
        text = {
            Column {
                // OutlinedTextField for task title input
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Task Title") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter a title for your new task.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAddClick,
                // Disable button if title is empty
                enabled = taskTitle.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}