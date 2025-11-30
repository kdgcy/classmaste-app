package com.kd.classmate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kd.classmate.appsetting.FontSize // 🌟 NEW IMPORT 🌟

@Composable
fun FontSizeDialog(
    currentSize: FontSize,
    onSizeSelected: (FontSize) -> Unit,
    onDismiss: () -> Unit
) {
    // Local state to track the user's temporary selection in the dialog
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(currentSize) }

    val allSizes = FontSize.entries.toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Font Size") },
        text = {
            Column {
                allSizes.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(size) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (size == selectedOption),
                            onClick = { onOptionSelected(size) }
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = size.displayName,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSizeSelected(selectedOption)
                    onDismiss()
                },
                enabled = selectedOption != currentSize // Only enable if selection changed
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}