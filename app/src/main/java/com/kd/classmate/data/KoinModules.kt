package com.kd.classmate.data

import com.kd.classmate.data.AppDatabase.Companion.getDatabase
import com.kd.classmate.dashboard.DashboardViewModel
import com.kd.classmate.subtasks.TaskDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import com.kd.classmate.data.subtaskdata.SubtaskDao // NEW IMPORT
import com.kd.classmate.data.subtaskdata.SubtaskRepository // NEW IMPORT

val appModule = module {

    // --- Data Layer Dependencies ---

    // AppDatabase: Singleton, gets context from Koin
    single { getDatabase(androidContext()) }

    // TaskDao
    single { get<AppDatabase>().taskDao() }

    // NEW: SubtaskDao
    single { get<AppDatabase>().subtaskDao() }

    // TaskRepository
    single { TaskRepository(get()) }

    // NEW: SubtaskRepository
    single { SubtaskRepository(get()) }

    // --- ViewModel Dependencies ---

    // DashboardViewModel
    viewModel { DashboardViewModel(get()) }

    // TaskDetailsViewModel: NOW REQUIRES SubtaskRepository
    viewModel { params ->
        TaskDetailsViewModel(
            repository = get(), // TaskRepository
            subtaskRepository = get(), // NEW: SubtaskRepository
            taskId = params.get<Int>()
        )
    }
}