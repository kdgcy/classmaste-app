// File: AppSettingsViewModel.kt (REVISED)

package com.kd.classmate.appsetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.PreferenceManager // NEW IMPORT
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

// Data class to hold the necessary settings states
data class AppSettingsUiState(
    val isPomodoroSoundEnabled: Boolean = true
)

class AppSettingsViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Expose settings state flow by mapping the flow from the preference manager
    val uiState: StateFlow<AppSettingsUiState> = preferenceManager.getPomodoroSoundState()
        .map { isEnabled ->
            AppSettingsUiState(isPomodoroSoundEnabled = isEnabled)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingsUiState()
        )

    // Public function to toggle the setting and save it via the PreferenceManager
    fun togglePomodoroSound(enabled: Boolean) {
        preferenceManager.setPomodoroSoundState(enabled)
    }
}