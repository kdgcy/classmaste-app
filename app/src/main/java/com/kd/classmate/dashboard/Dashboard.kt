package com.kd.classmate.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.AddTaskDialog
import com.kd.classmate.utils.Routes
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// --- HELPER FUNCTIONS FOR FORMATTING start---
private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d")

private fun formatDueDate(date: LocalDate?): String {
    return date?.format(shortDateFormatter) ?: "Not set"
}
private fun formatDueTime(time: LocalTime?): String {
    return time?.format(DateTimeFormatter.ofPattern("h:mma")) ?: "Not set"
}
// --- HELPER FUNCTIONS FOR FORMATTING end ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Dashboard(navController: NavController) {

    // Initialize the ViewModel and collect state
    val viewModel: DashboardViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    // --- Add Task Dialog Host ---
    if (uiState.isAddDialogVisible) {
        AddTaskDialog(
            taskTitle = uiState.newTaskTitleInput,
            onTitleChange = viewModel::setNewTaskTitleInput,
            onDismiss = { viewModel.setAddDialogVisibility(false) },
            onAddClick = viewModel::addTask,
            selectedDate = uiState.selectedDate,
            selectedTime = uiState.selectedTime,
            isDatePickerVisible = uiState.isDatePickerVisible,
            onDatePickerVisibilityChange = viewModel::setDatePickerVisibility,
            // This parameter now safely toggles the internal 'isSelectingTime' state
            onTimePickerVisibilityChange = viewModel::setTimePickerVisibility,
            onDateSelected = viewModel::updateSelectedDate,
            onTimeSelected = viewModel::updateSelectedTime,
        )
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") }
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
            if (uiState.taskList.isEmpty()) {
                item {
                    Column(
                        // Fill the remaining space to center the content
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ListAlt,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tasks yet!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Tap '+' to add your first to-do.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(uiState.taskList, key = { it.id }) { task ->
                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        // FIX: Build the string path manually
                                        navController.navigate("taskDetail/${task.id}")
                                    },
                                    onLongClick = { viewModel.setTaskInContext(task) }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Column to stack Title and Due Date/Time (Left Side)
                                Column(modifier = Modifier.weight(1f)) {
                                    //  Task Title (Larger and Bold)
                                    Text(
                                        text = task.title,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    //  Due Date/Time Info (Only display if not completed)
                                    if (!task.isCompleted) {
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Display Date and Time compacted
                                            Text(
                                                text = formatDueDate(task.dueDate),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = " | ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatDueTime(task.dueTime),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Context Menu Host
                        if (uiState.taskInContext == task) {
                            TaskContextMenu(
                                task = task,
                                onDismiss = { viewModel.setTaskInContext(null) },
                                onToggleCompletion = viewModel::updateTaskCompletion
                            )
                        }
                    }
                }
            }
        }
    }
}