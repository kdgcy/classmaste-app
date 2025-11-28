package com.kd.classmate.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.AddTaskDialog
import com.kd.classmate.utils.Routes
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


private fun formatDueDate(date: LocalDate?): String {
    return if (date != null) {
        // Use a standard formatter like "MMM d, yyyy" (e.g., Jul 27, 2025)
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } else {
        "Not set"
    }
}
private fun formatDueTime(time: LocalTime?): String {
    return if (time != null) {
        // Use a standard 12-hour formatter like "h:mma" (e.g., 5:00AM)
        time.format(DateTimeFormatter.ofPattern("h:mma"))
    } else {
        "Not set"
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Dashboard(navController: NavController) {

    // Initialize the ViewModel and collect state
    val viewModel: DashboardViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    // --- 1. Add Task Dialog ---
    if (uiState.isAddDialogVisible) {
        AddTaskDialog(
            taskTitle = uiState.newTaskTitleInput,
            onTitleChange = viewModel::setNewTaskTitleInput,
            onDismiss = { viewModel.setAddDialogVisibility(false) },
            onAddClick = viewModel::addTask,

            // PICKER PARAMETERS (remains the same)
            selectedDate = uiState.selectedDate,
            selectedTime = uiState.selectedTime,
            isDatePickerVisible = uiState.isDatePickerVisible,
            isTimePickerVisible = uiState.isTimePickerVisible,
            onDatePickerVisibilityChange = viewModel::setDatePickerVisibility,
            onTimePickerVisibilityChange = viewModel::setTimePickerVisibility,
            onDateSelected = viewModel::updateSelectedDate,
            onTimeSelected = viewModel::updateSelectedTime,
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
                Box {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    navController.navigate(Routes.taskDetailsPath(task.id))
                                },
                                onLongClick = {
                                    viewModel.setTaskInContext(task)
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 🌟 NEW: Column to stack Title and Due Date/Time 🌟
                            Column(modifier = Modifier.weight(1f)) {
                                // 1. Task Title
                                Text(
                                    text = task.title,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                // 2. Due Date/Time Info (Only display if not completed)
                                if (!task.isCompleted) {
                                    Text(
                                        text = "Due: ${formatDueDate(task.dueDate)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LocalContentColor.current.copy(alpha = 0.6f)
                                        )
                                    )
                                    Text(
                                        text = "Time: ${formatDueTime(task.dueTime)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LocalContentColor.current.copy(alpha = 0.6f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                    // Only show the menu if this specific task matches the one in context
                    if (uiState.taskInContext == task) {
                        TaskContextMenu(
                            task = task,
                            onDismiss = { viewModel.setTaskInContext(null) },
                            // 💥 FIX: The function is only passed to the Context Menu as a reference.
                            // The Context Menu (TaskContextMenu.kt) must be fixed to pass only one parameter.
                            onToggleCompletion = viewModel::updateTaskCompletion // Ensure this is the correct reference
                        )
                    }
                }
            }
        }
    }
}