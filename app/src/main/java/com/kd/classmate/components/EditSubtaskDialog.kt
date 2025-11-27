// File: EditSubtaskDialog.kt
package com.kd.classmate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun EditSubtaskDialog(
    currentTitle: String,
    onTitleChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit // Include delete option for subtasks
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isSaveButtonEnabled = currentTitle.isNotBlank()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = "Edit Subtask") // Updated Title
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Subtask Title") }, // Updated Label
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isSaveButtonEnabled) {
                                onSaveClick()
                                keyboardController?.hide()
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Modify the title or delete the subtask.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveClick,
                enabled = isSaveButtonEnabled
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    )
}