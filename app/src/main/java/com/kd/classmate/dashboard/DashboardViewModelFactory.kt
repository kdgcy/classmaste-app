package com.kd.classmate.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kd.classmate.data.TaskRepository

class DashboardViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is the DashboardViewModel
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            // If it is, instantiate it using the provided repository
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}