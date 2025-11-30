package com.kd.classmate.subtasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.AddSubtaskDialog
import com.kd.classmate.components.DeleteConfirmationDialog
import com.kd.classmate.components.EditSubtaskDialog
import com.kd.classmate.components.EditTaskDialog
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.kd.classmate.components.DateTimePickerDialogs
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider

// --- HELPER FUNCTIONS FOR FORMATTING ---
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val timeFormatter = DateTimeFormatter.ofPattern("h:mma")

@Composable
private fun formatSchedule(date: LocalDate?, time: LocalTime?): Pair<String, String> {
    val dateText = if (date != null) date.format(dateFormatter) else "Not set"
    val timeText = if (time != null) time.format(timeFormatter) else "Not set"
    return Pair(dateText, timeText)
}

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

    var revealedSubtaskId by remember { mutableStateOf<Int?>(null) }
    var currentSwipedItemId by remember { mutableStateOf<Int?>(null) }
    val currentRevealedId by rememberUpdatedState(currentSwipedItemId)

    val (dateText, timeText) = formatSchedule(uiState.selectedDate, uiState.selectedTime)


    // NEW: Date/Time Picker Hosting
    DateTimePickerDialogs(
        isDatePickerVisible = uiState.isDatePickerVisible,
        isTimePickerVisible = uiState.isTimePickerVisible,
        onDatePickerVisibilityChange = viewModel::setDatePickerVisibility,
        onTimePickerVisibilityChange = viewModel::setTimePickerVisibility,
        onDateSelected = viewModel::updateSelectedDate,
        onTimeSelected = viewModel::updateSelectedTime,
        initialDate = uiState.selectedDate
    )

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
                        onDelete = viewModel::showDeleteConfirmation,
                        onSetReminder = viewModel::setDatePickerVisibility
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
        // Background Click Handler
        if (currentRevealedId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = { currentSwipedItemId = null })
            )
        }

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            // DETECT TAPS ON BACKGROUND/EMPTY SPACE TO CLOSE DELETE ICON
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        revealedSubtaskId = null
                    }
                )
            },
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // NEW SECTION: SCHEDULE STATUS DISPLAY
            item {
                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Due: $dateText", // Now resolved
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Time: $timeText", // Now resolved
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Subtask Header (Separation line)
            item {
                HorizontalDivider()
            }

            // Subtask List with SWIPE-TO-DELETE
            items(uiState.subtaskList, key = { it.id }) { subtask ->
                // The visibility toggle is still needed for the smooth exit animation
                var isVisible by remember { mutableStateOf(true) }

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { newValue ->
                        if (newValue == SwipeToDismissBoxValue.EndToStart) {
                            // If user swipes this row, mark it as the revealed one
                            revealedSubtaskId = subtask.id
                            true
                        } else {
                            // If they swipe back to close, clear the id
                            if (revealedSubtaskId == subtask.id) {
                                revealedSubtaskId = null
                            }
                            true
                        }
                    }
                )

                // THE MAGIC: CLOSE THIS ROW IF ANOTHER IS OPENED OR BACKGROUND TAPPED
                LaunchedEffect(revealedSubtaskId) {
                    if (revealedSubtaskId != subtask.id && dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    }
                }
                AnimatedVisibility(
                    visible = isVisible,
                    exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                ){
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val progress = dismissState.progress
                            val scale = Math.min(1f, progress)

                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.Transparent)
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(
                                    onClick = {
                                        isVisible = false
                                        viewModel.deleteSubtask(subtask)
                                        // Reset revealed ID after deletion
                                        if (revealedSubtaskId == subtask.id) revealedSubtaskId = null
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .scale(scale)
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
                        content = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            // IF USER TAPS THE ROW CONTENT, ALSO CLOSE THE DELETE ICON
                                            if (revealedSubtaskId != null) {
                                                revealedSubtaskId = null
                                            } else {
                                                viewModel.updateSubtaskCompletion(subtask, !subtask.isCompleted)
                                            }
                                        },
                                        onLongClick = {
                                            // Close delete icon before opening edit dialog
                                            revealedSubtaskId = null
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
                                        // Close delete icon if checking the box
                                        revealedSubtaskId = null
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