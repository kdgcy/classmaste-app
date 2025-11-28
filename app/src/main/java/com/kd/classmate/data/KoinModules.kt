package com.kd.classmate.data

import com.kd.classmate.calendar.CalendarViewModel
import com.kd.classmate.data.AppDatabase.Companion.getDatabase
import com.kd.classmate.dashboard.DashboardViewModel
import com.kd.classmate.subtasks.TaskDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import com.kd.classmate.data.subtaskdata.SubtaskDao
import com.kd.classmate.data.subtaskdata.SubtaskRepository
import com.kd.classmate.services.NotificationScheduler
import com.kd.classmate.services.NotificationSchedulerImpl

val appModule = module {

    // --- Data Layer Dependencies ---

    // AppDatabase
    single { getDatabase(androidContext()) }

    // TaskDao
    single { get<AppDatabase>().taskDao() }

    // SubtaskDao
    single { get<AppDatabase>().subtaskDao() }

    // TaskRepository
    single { TaskRepository(get()) }

    // SubtaskRepository
    single { SubtaskRepository(get()) }

    // Notification Scheduler
    single<NotificationScheduler> { NotificationSchedulerImpl(androidContext()) }

    // --- ViewModel Dependencies ---

    // DashboardViewModel
    viewModel {
        DashboardViewModel(
            repository = get(), // TaskRepository
            notificationScheduler = get() // NotificationScheduler
        )
    }

    // 🌟 FIX: CalendarViewModel - Explicitly supplying two dependencies 🌟
    viewModel {
        CalendarViewModel(
            repository = get(), // TaskRepository (1st)
            notificationScheduler = get() // NotificationScheduler (2nd)
        )
    }

    viewModel { params ->
        TaskDetailsViewModel(
            repository = get(), // 1st
            subtaskRepository = get(), // 2nd
            taskId = params.get<Int>(), // 3rd
            notificationScheduler = get() // 4th
        )
    }
}