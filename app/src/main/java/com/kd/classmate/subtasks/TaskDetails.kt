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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                var isVisible by remember { mutableStateOf(true) }

                // 1. SwipeToDismissBox State
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) { // Swipe Left
                            isVisible = false // Trigger the fade out animation
                            true // Allow dismissal
                        } else {
                            false
                        }
                    }
                )

                // 2. Perform deletion after the dismissal animation starts
                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue == SwipeToDismissBoxValue.Settled) return@LaunchedEffect

                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        // Small delay to let the animation start before deleting data
                        delay(300)
                        viewModel.deleteSubtask(subtask)
                    }
                }

                // 3. Animated Visibility Wrapper
                AnimatedVisibility(
                    visible = isVisible,
                    exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                ) {
                    // 4. SwipeToDismissBox Composable
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.Settled -> Color.Transparent
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error // Swipe left shows red
                            }
                            // Background icon and color
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onError
                                )
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
                                            // Simple click still toggles completion
                                            viewModel.updateSubtaskCompletion(subtask, !subtask.isCompleted)
                                        },
                                        onLongClick = {
                                            viewModel.startEditSubtask(subtask) // Long press starts edit
                                        }
                                    )
                                    .background(MaterialTheme.colorScheme.surface) // Ensures background is covered
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