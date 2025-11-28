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

    // 🌟 FIX: DashboardViewModel now passes both dependencies 🌟
    viewModel {
        DashboardViewModel(
            repository = get(), // TaskRepository
            notificationScheduler = get() // NotificationScheduler
        )
    }

    // NEW: CalendarViewModel injection
    viewModel { CalendarViewModel(repository = get()) }

    viewModel { params ->
        TaskDetailsViewModel(
            repository = get(), // TaskRepository
            subtaskRepository = get(), // SubtaskRepository
            notificationScheduler = get(), // NotificationScheduler
            taskId = params.get<Int>() // Runtime parameter
        )
    }
}