package com.kd.classmate.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.kd.classmate.appsetting.FontSize // NEW IMPORT

interface PreferenceManager {
    // Master notification Switch
    fun getMasterNotificationState(): StateFlow<Boolean>
    fun setMasterNotificationState(enabled: Boolean)

    // Dark Mode Switch
    fun getDarkModeState(): StateFlow<Boolean>
    fun setDarkModeState(enabled: Boolean)

    // 🌟 NEW: Font Size Switch 🌟
    fun getFontSizeState(): StateFlow<FontSize>
    fun setFontSizeState(size: FontSize)
}

class PreferenceManagerImpl(context: Context) : PreferenceManager {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val MASTER_NOTIFICATION_KEY = "master_notification_enabled"
    private val DARK_MODE_KEY = "dark_mode_enabled"
    private val FONT_SIZE_KEY = "font_size_setting" // 🌟 NEW KEY 🌟

    private val _isMasterNotificationEnabled = MutableStateFlow(
        prefs.getBoolean(MASTER_NOTIFICATION_KEY, true)
    )
    private val _isDarkModeEnabled = MutableStateFlow(
        prefs.getBoolean(DARK_MODE_KEY, false)
    )
    private val _fontSize = MutableStateFlow( // 🌟 NEW FLOW 🌟
        try {
            FontSize.valueOf(prefs.getString(FONT_SIZE_KEY, FontSize.MEDIUM.name) ?: FontSize.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            FontSize.MEDIUM
        }
    )

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

    // 🌟 NEW: Font Size Logic 🌟
    override fun getFontSizeState(): StateFlow<FontSize> = _fontSize.asStateFlow()
    override fun setFontSizeState(size: FontSize) {
        prefs.edit().putString(FONT_SIZE_KEY, size.name).apply()
        _fontSize.value = size
    }
}