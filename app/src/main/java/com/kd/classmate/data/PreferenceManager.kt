package com.kd.classmate.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PreferenceManager {
    fun getPomodoroSoundState(): StateFlow<Boolean>
    fun setPomodoroSoundState(enabled: Boolean)
}

class PreferenceManagerImpl(context: Context) : PreferenceManager {
    // We use the application context to prevent memory leaks
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Key for the setting
    private val POMODORO_SOUND_KEY = "pomodoro_sound_enabled"

    // Default state: ON (True)
    private val _isPomodoroSoundEnabled = MutableStateFlow(
        prefs.getBoolean(POMODORO_SOUND_KEY, true)
    )

    override fun getPomodoroSoundState(): StateFlow<Boolean> = _isPomodoroSoundEnabled.asStateFlow()

    override fun setPomodoroSoundState(enabled: Boolean) {
        prefs.edit().putBoolean(POMODORO_SOUND_KEY, enabled).apply()
        _isPomodoroSoundEnabled.value = enabled
    }
}