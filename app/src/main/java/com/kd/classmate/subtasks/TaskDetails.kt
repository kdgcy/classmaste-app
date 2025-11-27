package com.kd.classmate.subtasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row // NEW
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth // NEW
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // NEW
import androidx.compose.foundation.shape.RoundedCornerShape // NEW
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // NEW
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox // NEW
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton // NEW
import androidx.compose.material3.FloatingActionButtonDefaults // NEW
import androidx.compose.material3.HorizontalDivider // NEW
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme // NEW
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment // NEW
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration // NEW
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.AddSubtaskDialog // NEW IMPORT
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

    // --- DIALOGS HOSTING ---
    if (uiState.isEditDialogVisible && uiState.task != null) { /* ... EditTaskDialog remains the same ... */ }
    if (uiState.isDeleteConfirmationVisible && uiState.task != null) { /* ... DeleteConfirmationDialog remains the same ... */ }

    // --- NEW: ADD SUBTASK DIALOG ---
    if (uiState.isSubtaskAddDialogVisible) {
        AddSubtaskDialog(
            subtaskTitle = uiState.newSubtaskTitleInput,
            onTitleChange = viewModel::setNewSubtaskTitleInput,
            onDismiss = { viewModel.setSubtaskAddDialogVisibility(false) },
            onAddClick = viewModel::addSubtask
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setSubtaskAddDialogVisibility(true) },
                containerColor = MaterialTheme.colorScheme.tertiary, // Secondary color for subtask FAB
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

            // NEW: Subtask Section Header
            item {
                HorizontalDivider()
                Text("Subtasks:", style = MaterialTheme.typography.titleMedium)
            }

            // NEW: Subtask List (with Checkbox)
            items(uiState.subtaskList, key = { it.id }) { subtask ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
        }
    }
}