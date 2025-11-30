package com.kd.classmate.appsetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.PreferenceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine // NEW IMPORT
import kotlinx.coroutines.flow.map

// Data class to hold the necessary settings states (MODIFIED)
data class AppSettingsUiState(
    val isMasterNotificationEnabled: Boolean = true,
    val isDarkModeEnabled: Boolean = false // 🌟 NEW FIELD 🌟
)

class AppSettingsViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Expose settings state flow by mapping the flow from the preference manager
    val uiState: StateFlow<AppSettingsUiState> = preferenceManager.getMasterNotificationState()
        .combine(preferenceManager.getDarkModeState()) { isNotificationEnabled, isDarkMode ->
            AppSettingsUiState(
                isMasterNotificationEnabled = isNotificationEnabled,
                isDarkModeEnabled = isDarkMode // 🌟 Mapped here 🌟
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingsUiState()
        )

    // Public function to toggle the master notification setting
    fun toggleMasterNotification(enabled: Boolean) {
        preferenceManager.setMasterNotificationState(enabled)
    }

    // 🌟 NEW: Dark Mode Toggle Function 🌟
    fun toggleDarkMode(enabled: Boolean) {
        preferenceManager.setDarkModeState(enabled)
    }
}