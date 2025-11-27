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
fun AddSubtaskDialog(
    subtaskTitle: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onAddClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isAddButtonEnabled = subtaskTitle.isNotBlank()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add New Subtask")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = subtaskTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Subtask Title") },
                    singleLine = true,
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isAddButtonEnabled) {
                                onAddClick()
                                keyboardController?.hide()
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter a title for your new subtask.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAddClick,
                enabled = isAddButtonEnabled
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