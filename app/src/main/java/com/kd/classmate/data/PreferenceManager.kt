package com.kd.classmate.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.kd.classmate.appsetting.FontSize

interface PreferenceManager {
    // Master notification Switch
    fun getMasterNotificationState(): StateFlow<Boolean>
    fun setMasterNotificationState(enabled: Boolean)

    // Dark Mode Switch
    fun getDarkModeState(): StateFlow<Boolean>
    fun setDarkModeState(enabled: Boolean)

    // Font Size Switch
    fun getFontSizeState(): StateFlow<FontSize>
    fun setFontSizeState(size: FontSize)

    //Onboarding Flow for first time user
    fun getIsFirstLaunch(): StateFlow<Boolean>
    fun setFirstLaunchCompleted()
}

class PreferenceManagerImpl(context: Context) : PreferenceManager {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)


    //DEFINE KEYS HERE
    private val FIRST_LAUNCH_KEY = "is_first_launch"
    private val _isFirstLaunch = MutableStateFlow(
        prefs.getBoolean(FIRST_LAUNCH_KEY, true) // Default is true
    )
    private val MASTER_NOTIFICATION_KEY = "master_notification_enabled"
    private val DARK_MODE_KEY = "dark_mode_enabled"
    private val FONT_SIZE_KEY = "font_size_setting"

    private val _isMasterNotificationEnabled = MutableStateFlow(
        prefs.getBoolean(MASTER_NOTIFICATION_KEY, true)
    )
    private val _isDarkModeEnabled = MutableStateFlow(
        prefs.getBoolean(DARK_MODE_KEY, false)
    )
    private val _fontSize = MutableStateFlow(
        try {
            FontSize.valueOf(prefs.getString(FONT_SIZE_KEY, FontSize.MEDIUM.name) ?: FontSize.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            FontSize.MEDIUM
        }
    )


    //
    override fun getIsFirstLaunch(): StateFlow<Boolean> = _isFirstLaunch.asStateFlow() //

    override fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(FIRST_LAUNCH_KEY, false).apply() // Set to false permanently
        _isFirstLaunch.value = false //
    }

    override fun getMasterNotificationState(): StateFlow<Boolean> = _isMasterNotificationEnabled.asStateFlow()
    override fun setMasterNotificationState(enabled: Boolean) {
        prefs.edit().putBoolean(MASTER_NOTIFICATION_KEY, enabled).apply()
        _isMasterNotificationEnabled.value = enabled
    }

    // Dark Mode Logic (remains the same)
    override fun getDarkModeState(): StateFlow<Boolean> = _isDarkModeEnabled.asStateFlow()
    override fun setDarkModeState(enabled: Boolean) {
        prefs.edit().putBoolean(DARK_MODE_KEY, enabled).apply()
        _isDarkModeEnabled.value = enabled
    }

    //  Font Size Logic
    override fun getFontSizeState(): StateFlow<FontSize> = _fontSize.asStateFlow()
    override fun setFontSizeState(size: FontSize) {
        prefs.edit().putString(FONT_SIZE_KEY, size.name).apply()
        _fontSize.value = size
    }
}