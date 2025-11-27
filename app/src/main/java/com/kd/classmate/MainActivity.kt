package com.kd.classmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kd.classmate.data.AppDatabase
import com.kd.classmate.data.TaskRepository
import com.kd.classmate.dashboard.DashboardViewModel
import com.kd.classmate.dashboard.DashboardViewModelFactory // Import the new factory

class MainActivity : ComponentActivity() {

    // 1. Initialize the Database and DAO
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val taskDao by lazy { database.taskDao() }

    // 2. Initialize the Repository
    private val repository by lazy { TaskRepository(taskDao) }

    // 3. Initialize the Factory
    private val factory by lazy { DashboardViewModelFactory(repository) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // We pass the factory to the AppNavigation to handle ViewModel creation
            AppNavigation(factory = factory)
        }
    }
}