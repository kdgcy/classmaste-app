// File: ClassmateApplication.kt
package com.kd.classmate

import android.app.Application
import com.kd.classmate.data.appModule // Import your module
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClassmateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Log Koin activity to Android Logger
            // androidLogger(Level.ERROR) // Optional for debugging
            // Reference the Android context
            androidContext(this@ClassmateApplication)
            // Load modules
            modules(appModule)
        }
    }
}