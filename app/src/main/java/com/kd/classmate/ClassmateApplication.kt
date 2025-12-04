package com.kd.classmate

import android.app.Application
import com.kd.classmate.data.appModule // Import your module
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ClassmateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ClassmateApplication)
            modules(appModule)
        }
    }
}