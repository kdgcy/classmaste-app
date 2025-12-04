package com.kd.classmate.subtasks

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.kd.classmate.data.Task

@Composable
fun TaskDetailsMenu(
    navController: NavController,
    task: Task?,
    onStartEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetReminder: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    if(task==null) return
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    onStartEdit()
                }
            )

            // Set Reminder Action
            DropdownMenuItem(
                text = { Text("Set reminder") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.AddAlert,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    // FIX: Call the ViewModel handler to show the Date Picker
                    onSetReminder(true)
                }
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = {
                    Text("Delete task",
                        color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}