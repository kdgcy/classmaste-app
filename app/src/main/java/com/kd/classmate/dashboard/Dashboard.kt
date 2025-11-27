package com.kd.classmate.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues // NEW: For padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn // NEW: For displaying a list
import androidx.compose.foundation.lazy.items // NEW: To iterate through the list
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // NEW: To observe the Flow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier // NEW: For modifiers
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(navController: NavController, factory: ViewModelProvider.Factory) {

    // Initialize the ViewModel using the factory
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    // Collect the UI state as Compose state
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = null
                        )
                        DashboardMenu(navController)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // C - Create: Add a new task on FAB click
                    viewModel.addTask("Task #${uiState.taskList.size + 1}")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->

        // R - Read: Display the list of tasks
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Apply padding from the Scaffold
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Inner padding for the list items
        ) {
            items(uiState.taskList) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // U - Update: Toggle completion status when the row is clicked
                            viewModel.updateTaskCompletion(task, !task.isCompleted)
                        }
                        // D - DELETE: Trigger deletion on a Long Press
                        .combinedClickable( // Import and use combinedClickable
                            onClick = { viewModel.updateTaskCompletion(task, !task.isCompleted) },
                            onLongClick = {
                                viewModel.deleteTask(task) // Call the delete function
                            }
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox that triggers the update function
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { isChecked ->
                            // U - Update: Call ViewModel function to update completion status
                            viewModel.updateTaskCompletion(task, isChecked)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        // Add visual feedback: strike-through if completed
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    // Optional: Display ID for debugging
                    Text(
                        text = "(ID: ${task.id})",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}