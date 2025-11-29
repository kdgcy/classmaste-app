package com.kd.classmate.services

import android.content.Context
import android.os.PowerManager

interface WakeLockManager {
    fun acquireWakeLock()
    fun releaseWakeLock()
}

class WakeLockManagerImpl(context: Context) : WakeLockManager {
    // Get Application Context to prevent memory leaks
    private val appContext = context.applicationContext
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    // Create the WakeLock instance (WAKE_LOCK ensures CPU stays on)
    private val wakeLock: PowerManager.WakeLock by lazy {
        powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "com.kd.classmate:PomodoroWakeLockTag"
        )
    }

    override fun acquireWakeLock() {
        if (!wakeLock.isHeld) {
            wakeLock.acquire()
        }
    }

    override fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}