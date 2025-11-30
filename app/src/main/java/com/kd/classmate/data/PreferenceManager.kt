package com.kd.classmate.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PreferenceManager {
    //  NEW: Master Switch
    fun getMasterNotificationState(): StateFlow<Boolean>
    fun setMasterNotificationState(enabled: Boolean)
}

class PreferenceManagerImpl(context: Context) : PreferenceManager {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Key for the master setting
    private val MASTER_NOTIFICATION_KEY = "master_notification_enabled"

    // Default state: ON (True)
    private val _isMasterNotificationEnabled = MutableStateFlow(
        prefs.getBoolean(MASTER_NOTIFICATION_KEY, true)
    )

    override fun getMasterNotificationState(): StateFlow<Boolean> = _isMasterNotificationEnabled.asStateFlow()

    override fun setMasterNotificationState(enabled: Boolean) {
        prefs.edit().putBoolean(MASTER_NOTIFICATION_KEY, enabled).apply()
        _isMasterNotificationEnabled.value = enabled
    }
}