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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Dashboard(navController: NavController, factory: ViewModelProvider.Factory) {

    // Initialize the ViewModel using the factory
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    // Collect the UI state as Compose state
    val uiState = viewModel.uiState.collectAsState().value

    if (uiState.isDialogVisible) {
        AddTaskDialog(
            taskTitle = uiState.newTaskTitleInput,
            onTitleChange = viewModel::setNewTaskTitleInput,
            onDismiss = { viewModel.setDialogVisibility(false) },
            onAddClick = viewModel::addTask
        )
    }

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
                    viewModel.setDialogVisibility(true)
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.taskList) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            //Toggle completion status when the row is clicked
                            viewModel.updateTaskCompletion(task, !task.isCompleted)
                        }
                        //Trigger deletion on a Long Press
                        .combinedClickable(
                            onClick = { viewModel.updateTaskCompletion(task, !task.isCompleted) },
                            onLongClick = {
                                viewModel.deleteTask(task)
                            }
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox that triggers the update function
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { isChecked ->
                            viewModel.updateTaskCompletion(task, isChecked)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}