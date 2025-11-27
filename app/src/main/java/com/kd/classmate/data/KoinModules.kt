package com.kd.classmate.data

import com.kd.classmate.dashboard.DashboardViewModel
import com.kd.classmate.data.AppDatabase.Companion.getDatabase
import com.kd.classmate.subtasks.TaskDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // --- Data Layer Dependencies ---

    // AppDatabase: Singleton, gets context from Koin
    single { getDatabase(androidContext()) }

    // TaskDao: Gets the database from Koin
    single { get<AppDatabase>().taskDao() }

    // TaskRepository: Singleton, gets the DAO from Koin
    single { TaskRepository(get()) }

    // --- ViewModel Dependencies ---

    // DashboardViewModel: Koin automatically injects TaskRepository (get())
    viewModel { DashboardViewModel(get()) }

    // TaskDetailsViewModel: Koin needs parameters for the taskId (get() means inject the dependency)
    viewModel { params ->
        TaskDetailsViewModel(
            repository = get(),
            taskId = params.get<Int>() // Get the taskId parameter passed at runtime
        )
    }
}