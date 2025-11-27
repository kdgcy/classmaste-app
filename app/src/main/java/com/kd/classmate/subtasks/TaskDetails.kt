package com.kd.classmate.subtasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.AddSubtaskDialog
import com.kd.classmate.components.DeleteConfirmationDialog
import com.kd.classmate.components.EditSubtaskDialog
import com.kd.classmate.components.EditTaskDialog
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskDetails(
    navController: NavController,
    taskId: Int
){
    val viewModel: TaskDetailsViewModel = koinViewModel(
        parameters = { parametersOf(taskId) }
    )
    val uiState = viewModel.uiState.collectAsState().value

    // --- DIALOGS HOSTING ---
    if (uiState.isEditDialogVisible && uiState.task != null) {
        EditTaskDialog(
            currentTitle = uiState.editTaskTitleInput,
            onTitleChange = viewModel::setEditTaskTitleInput,
            onCancel = viewModel::cancelEdit,
            onSaveClick = viewModel::saveEditedTask,
        )
    }
    if (uiState.isDeleteConfirmationVisible && uiState.task != null) {
        DeleteConfirmationDialog(
            taskTitle = uiState.task.title,
            onDismiss = viewModel::hideDeleteConfirmation,
            onConfirmDelete = {
                viewModel.deleteTask()
                navController.navigateUp()
            }
        )
    }

    // --- ADD SUBTASK DIALOG ---
    if (uiState.isSubtaskAddDialogVisible) {
        AddSubtaskDialog(
            subtaskTitle = uiState.newSubtaskTitleInput,
            onTitleChange = viewModel::setNewSubtaskTitleInput,
            onDismiss = { viewModel.setSubtaskAddDialogVisibility(false) },
            onAddClick = viewModel::addSubtask
        )
    }

    // --- EDIT SUBTASK DIALOG ---
    uiState.subtaskBeingEdited?.let { subtask ->
        if (uiState.isSubtaskEditDialogVisible) {
            EditSubtaskDialog(
                currentTitle = uiState.editSubtaskTitleInput,
                onTitleChange = viewModel::setEditSubtaskTitleInput,
                onCancel = viewModel::cancelEditSubtask,
                onSaveClick = viewModel::saveEditedSubtask,
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.title,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton( onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                },
                actions = {
                    TaskDetailsMenu(
                        navController = navController,
                        task = uiState.task,
                        onStartEdit = viewModel::startEdit,
                        onDelete = viewModel::showDeleteConfirmation
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setSubtaskAddDialogVisibility(true) },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Subtask")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Subtask List with SWIPE-TO-DELETE
            items(uiState.subtaskList, key = { it.id }) { subtask ->
                // The visibility toggle is still needed for the smooth exit animation
                var isVisible by remember { mutableStateOf(true) }

                // 1. SwipeToDismissBox State
                val dismissState = rememberSwipeToDismissBoxState(
                    // FIX: Confirm value change must be removed or always return false
                    // We remove it entirely to prevent automatic dismissal.
                )

                // LaunchedEffect is removed, as deletion happens on button click.

                // 3. Animated Visibility Wrapper
                AnimatedVisibility(
                    visible = isVisible,
                    exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                ){
                    // 4. SwipeToDismissBox Composable
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false, // Only allow swipe from EndToStart
                        backgroundContent = {
                            // Calculate scale based on swipe progress (from 0 to 1)
                            val progress = dismissState.progress
                            val scale = Math.min(1f, progress)

                            // FIX: Background is now a clickable icon button
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.Transparent)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(
                                    onClick = {
                                        // 💥 NEW LOGIC: Click the button to perform delete 💥
                                        isVisible = false // Trigger smooth exit animation
                                        viewModel.deleteSubtask(subtask) // Delete from database
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .scale(scale) // Scale the icon/circle based on swipe progress
                                        .background(
                                            color = MaterialTheme.colorScheme.error,
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onError,
                                    )
                                }
                            }
                        },
                        // The actual content to be swiped
                        content = {
                            // Subtask Row Content
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            viewModel.updateSubtaskCompletion(subtask, !subtask.isCompleted)
                                        },
                                        onLongClick = {
                                            viewModel.startEditSubtask(subtask)
                                        }
                                    )
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subtask.isCompleted,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateSubtaskCompletion(subtask, isChecked)
                                    }
                                )
                                Text(
                                    text = subtask.title,
                                    modifier = Modifier.padding(start = 8.dp),
                                    textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}