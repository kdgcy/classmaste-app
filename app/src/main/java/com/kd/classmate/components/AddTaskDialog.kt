package com.kd.classmate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect // NEW
import androidx.compose.ui.focus.FocusRequester // NEW
import androidx.compose.ui.focus.focusRequester // NEW
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions // NEW
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalSoftwareKeyboardController // NEW

@Composable
fun AddTaskDialog(
    // State and event handlers from ViewModel
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit
) {
    // 1. Setup Focus Requester and Keyboard Controller
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isAddButtonEnabled = taskTitle.isNotBlank()

    // 2. Request focus when the dialog first appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        // Optional: Ensure keyboard is explicitly shown if needed
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add New Task")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Task Title") },
                    singleLine = true,
                    // 3. Attach Focus Requester
                    modifier = Modifier.focusRequester(focusRequester),

                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),

                    // 4. Handle "Done" button press on the keyboard
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isAddButtonEnabled) {
                                onAddClick()
                                keyboardController?.hide() // Hide keyboard after submission
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter a title for your new task.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAddClick,
                enabled = isAddButtonEnabled // Use the local variable
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