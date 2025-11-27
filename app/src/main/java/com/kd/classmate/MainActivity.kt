// File: MainActivity.kt (UPDATED)
package com.kd.classmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// REMOVE: Imports for AppDatabase, TaskRepository, DashboardViewModelFactory

class MainActivity : ComponentActivity() {

    // REMOVE: All lazy initializations for database, taskDao, repository, and factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // REMOVE: Passing the factory. Koin will handle dependency provision globally.
            AppNavigation()
        }
    }
}