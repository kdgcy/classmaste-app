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
    // 🌟 FIX: Change signature to accept only Task 🌟
    onToggleCompletion: (Task) -> Unit
) {
    val newCompletionStatus = !task.isCompleted
    val menuText = if (task.isCompleted) "Mark as incomplete" else "Mark as complete"

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(menuText) },
            onClick = {
                // The ViewModel handles the actual toggling logic using the task's current status.
                onToggleCompletion(task)
                onDismiss()
            }
        )
        // TODO: Add "Edit" and "Delete" actions here if needed later
    }
}