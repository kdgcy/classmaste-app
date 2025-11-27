package com.kd.classmate.dashboard

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kd.classmate.data.Task

@Composable
fun TaskContextMenu(
    task: Task,
    onDismiss: () -> Unit,
    onToggleCompletion: (Task, Boolean) -> Unit
) {
    val newCompletionStatus = !task.isCompleted
    val menuText = if (task.isCompleted) "Mark as incomplete" else "Mark as complete"

    DropdownMenu(
        expanded = true, // Always true when hosted, visibility controlled by parent
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(menuText) },
            onClick = {
                onToggleCompletion(task, newCompletionStatus)
                onDismiss() // Close the menu after action
            }
        )
        // TODO: Add "Edit" and "Delete" actions here if needed later
    }
}