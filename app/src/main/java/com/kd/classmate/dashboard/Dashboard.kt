package com.kd.classmate.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card // Import Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kd.classmate.components.AddTaskDialog
import com.kd.classmate.components.EditTaskDialog
import com.kd.classmate.dashboard.DashboardMenu

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Dashboard(navController: NavController, factory: ViewModelProvider.Factory) {

    // Initialize the ViewModel and collect state
    val viewModel: DashboardViewModel = viewModel(factory = factory)
    val uiState = viewModel.uiState.collectAsState().value

    // --- 1. Add Task Dialog ---
    if (uiState.isAddDialogVisible) {
        AddTaskDialog(
            taskTitle = uiState.newTaskTitleInput,
            onTitleChange = viewModel::setNewTaskTitleInput,
            onDismiss = { viewModel.setAddDialogVisibility(false) },
            onAddClick = viewModel::addTask
        )
    }

    // --- 2. Edit Task Dialog ---
    uiState.taskBeingEdited?.let { task ->
        EditTaskDialog(
            currentTitle = uiState.editTaskTitleInput,
            onTitleChange = viewModel::setEditTaskTitleInput,
            onCancel = viewModel::cancelEdit,
            onSaveClick = viewModel::saveEditedTask,
            onDeleteClick = {
                viewModel.deleteTask(task)
                viewModel.cancelEdit()
            }
        )
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    DashboardMenu(navController)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.setAddDialogVisibility(true)
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
            items(uiState.taskList, key = { it.id }) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Combined Clickable applied to the Card for the whole task row
                        .combinedClickable(
                            // Short click: Toggle completion status
                            onClick = {
                                viewModel.updateTaskCompletion(task, !task.isCompleted)
                            },
                            // Long Press: Open the Edit Dialog
                            onLongClick = {
                                viewModel.startEdit(task)
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp), // Increased padding for better look
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // REMOVED CHECKBOX HERE

                        // Spacer removed since Checkbox is gone

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
}