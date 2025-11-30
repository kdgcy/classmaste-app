// File: AppSettingsViewModel.kt (MODIFIED)

package com.kd.classmate.appsetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.PreferenceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

// Data class to hold the necessary settings states
data class AppSettingsUiState(
    val isMasterNotificationEnabled: Boolean = true // NEW MASTER FIELD
)

class AppSettingsViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // Expose settings state flow by mapping the flow from the preference manager
    val uiState: StateFlow<AppSettingsUiState> = preferenceManager.getMasterNotificationState()
        .map { isEnabled ->
            AppSettingsUiState(isMasterNotificationEnabled = isEnabled)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettingsUiState()
        )

    // Public function to toggle the master setting and save it
    fun toggleMasterNotification(enabled: Boolean) {
        preferenceManager.setMasterNotificationState(enabled)
    }
}