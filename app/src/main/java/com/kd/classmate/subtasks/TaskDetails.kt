package com.kd.classmate.subtasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // NEW: Import viewModel
import androidx.compose.runtime.collectAsState // NEW: Import collectAsState
import com.kd.classmate.data.TaskRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetails(
    navController: NavController,
    taskId: Int,
    // Note: The factory parameter is the DashboardViewModelFactory from MainActivity
    factory: ViewModelProvider.Factory // This factory holds the TaskRepository
){
    // 1. Get the TaskRepository from the factory (assumes the factory is DashboardViewModelFactory)
    // This is a common but slightly impure way to reuse the repository dependency.
    // In a real app, you would typically use an injection framework like Hilt/Koin.
    val repository = (factory as? com.kd.classmate.dashboard.DashboardViewModelFactory)?.repository
        ?: throw IllegalStateException("Factory must be DashboardViewModelFactory to extract repository")


    // 2. Create the specialized factory for this ViewModel
    val detailsFactory = TaskDetailsViewModelFactory(repository, taskId)

    // 3. Initialize the ViewModel
    val viewModel: TaskDetailsViewModel = viewModel(factory = detailsFactory)
    val uiState = viewModel.uiState.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                // 4. Use the title from the ViewModel's state
                title = {
                    Text(
                        text = uiState.title, // DISPLAY THE DYNAMIC TITLE
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                }
            )
        }
    ) {paddingValues ->
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