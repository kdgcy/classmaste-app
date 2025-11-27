package com.kd.classmate.subtasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import org.koin.androidx.compose.koinViewModel // Required for koinViewModel()
import org.koin.core.parameter.parametersOf
import androidx.compose.foundation.lazy.LazyColumn // Required for LazyColumn
import com.kd.classmate.components.EditTaskDialog // Required for the dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetails(
    navController: NavController,
    taskId: Int
){
    // 1. Initialize ViewModel using KOIN, passing the required taskId
    val viewModel: TaskDetailsViewModel = koinViewModel(
        parameters = { parametersOf(taskId) }
    )
    val uiState = viewModel.uiState.collectAsState().value

    // --- EDIT TASK DIALOG ---
    // The dialog is only shown if the state indicates it and the task is loaded.
    if (uiState.isEditDialogVisible && uiState.task != null) {
        EditTaskDialog(
            // Use the state and functions from the local TaskDetailsViewModel
            currentTitle = uiState.editTaskTitleInput,
            onTitleChange = viewModel::setEditTaskTitleInput,
            onCancel = viewModel::cancelEdit,
            onSaveClick = viewModel::saveEditedTask,
            onDeleteClick = {
                viewModel.deleteTask() // Delete action
                viewModel.cancelEdit() // Close dialog
                navController.navigateUp() // Navigate back to dashboard after deletion
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
                    // Pass the task and action handlers to the menu
                    TaskDetailsMenu(
                        navController = navController,
                        task = uiState.task,
                        onStartEdit = viewModel::startEdit, // Trigger local VM function
                        onDelete = {
                            viewModel.deleteTask()
                            navController.navigateUp() // Navigate back after deletion
                        }
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
            // Placeholder content to show it's working
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