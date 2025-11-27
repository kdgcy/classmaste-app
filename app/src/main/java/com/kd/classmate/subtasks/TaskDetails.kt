package com.kd.classmate.subtasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.DeleteConfirmationDialog
import com.kd.classmate.components.EditTaskDialog
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetails(
    navController: NavController,
    taskId: Int
){
    val viewModel: TaskDetailsViewModel = koinViewModel(
        parameters = { parametersOf(taskId) }
    )
    val uiState = viewModel.uiState.collectAsState().value

    // --- EDIT TASK DIALOG ---
    if (uiState.isEditDialogVisible && uiState.task != null) {
        EditTaskDialog(
            currentTitle = uiState.editTaskTitleInput,
            onTitleChange = viewModel::setEditTaskTitleInput,
            onCancel = viewModel::cancelEdit,
            onSaveClick = viewModel::saveEditedTask,
            // onDeleteClick parameter REMOVED
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    if (uiState.isDeleteConfirmationVisible && uiState.task != null) {
        DeleteConfirmationDialog(
            taskTitle = uiState.task.title,
            onDismiss = viewModel::hideDeleteConfirmation,
            onConfirmDelete = {
                viewModel.deleteTask() // Triggers deletion in VM
                navController.navigateUp() // Navigate back to dashboard
            }
        )
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
                        // CHANGE: Action now shows the confirmation dialog
                        onDelete = viewModel::showDeleteConfirmation
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                if (uiState.isLoading) {
                    Text("Loading task data...")
                } else if (uiState.task == null) {
                    Text("Error: Task ID $taskId not found.")
                } else {
                    Text("Task ID: ${uiState.task.id}")
                    Text("Completed: ${uiState.task.isCompleted}")
                }
            }
        }
    }
}