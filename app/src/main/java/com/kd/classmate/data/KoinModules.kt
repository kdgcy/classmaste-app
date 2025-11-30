package com.kd.classmate.data

import com.kd.classmate.calendar.CalendarViewModel
import com.kd.classmate.dashboard.DashboardViewModel
import com.kd.classmate.data.AppDatabase.Companion.getDatabase
import com.kd.classmate.data.subtaskdata.SubtaskRepository
import com.kd.classmate.pomodoro.PomodoroViewModel
import com.kd.classmate.services.NotificationScheduler
import com.kd.classmate.services.NotificationSchedulerImpl
import com.kd.classmate.services.WakeLockManager
import com.kd.classmate.services.WakeLockManagerImpl
import com.kd.classmate.subtasks.TaskDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.kd.classmate.appsetting.AppSettingsViewModel
import com.kd.classmate.services.SoundPlayer
import com.kd.classmate.services.SoundPlayerImpl

val appModule = module {

    // --- Data Layer Dependencies ---
    single { getDatabase(androidContext()) }
    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().subtaskDao() }
    single { TaskRepository(get()) }
    single { SubtaskRepository(get()) }
    single<NotificationScheduler> {
        NotificationSchedulerImpl(
            context = androidContext(),
            preferenceManager = get() // Inject the PreferenceManager here
        )
    }
    single<WakeLockManager> { WakeLockManagerImpl(androidContext()) }
    single<PreferenceManager> { PreferenceManagerImpl(androidContext()) }
    single<SoundPlayer> { SoundPlayerImpl(androidContext()) }


// --- ViewModel Dependencies ---

    // DashboardViewModel
    viewModel {
        DashboardViewModel(
            repository = get(),
            notificationScheduler = get()
        )
    }

    // CalendarViewModel
    viewModel {
        CalendarViewModel(
            repository = get(),
            notificationScheduler = get()
        )
    }


    // PomodoroViewModel
    viewModel {
        PomodoroViewModel(
            wakeLockManager = get(),
            context = androidContext() // Inject Application Context for service binding and to prevent memory leak
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

    // AppSettingsViewModel
    viewModel { AppSettingsViewModel(preferenceManager = get()) }
}