package com.kd.classmate.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kd.classmate.components.AddTaskDialog
import com.kd.classmate.components.EditTaskDialog

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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.taskList, key = { it.id }) { task ->
                // 1. Create a state for the swipe dismissal
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        when (value) {
                            // VVVV ACTION 1: Left-to-Right (StartToEnd) -> TOGGLE COMPLETION VVVV
                            SwipeToDismissBoxValue.StartToEnd -> {
                                viewModel.updateTaskCompletion(task, !task.isCompleted)
                                // Return false to snap back (toggle action)
                                return@rememberSwipeToDismissBoxState false
                            }
                            // VVVV ACTION 2: Right-to-Left (EndToStart) -> DELETE VVVV
                            SwipeToDismissBoxValue.EndToStart -> {
                                viewModel.deleteTask(task)
                                // Return true to allow the item to be dismissed (deleted)
                                return@rememberSwipeToDismissBoxState true
                            }
                            else -> true
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier,
                    // Enable both directions
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,

                    // Background shown during the swipe gesture
                    backgroundContent = {
                        val direction = dismissState.dismissDirection
                        val color = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> Color.Green.copy(alpha = 0.8f) // TOGGLE
                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)    // DELETE
                            else -> Color.Transparent
                        }
                        val icon = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> if (task.isCompleted) Icons.Filled.Close else Icons.Filled.Done
                            SwipeToDismissBoxValue.EndToStart -> Icons.Filled.Delete
                            else -> null
                        }
                        val alignment = when (direction) {
                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            else -> Alignment.Center
                        }

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color, RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = "Action",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    // The Card is the content being swiped
                    content = {
                        Card(
                            shape = RoundedCornerShape(12.dp),
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
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.title,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}