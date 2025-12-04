package com.kd.classmate.appsetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.data.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Data class to hold the necessary settings states
data class AppSettingsUiState(
    val isMasterNotificationEnabled: Boolean = true,
    val isDarkModeEnabled: Boolean = false,
    val selectedFontSize: FontSize = FontSize.MEDIUM,
    val isFontSizeDialogVisible: Boolean = false
)

class AppSettingsViewModel(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isFontSizeDialogVisible = MutableStateFlow(false) // Internal UI control state

    // Expose settings state flow by combining all necessary preferences
    val uiState: StateFlow<AppSettingsUiState> = preferenceManager.getMasterNotificationState()
        .combine(preferenceManager.getDarkModeState()) { isNotificationEnabled, isDarkMode ->
            isNotificationEnabled to isDarkMode
        }
        .combine(preferenceManager.getFontSizeState()) { (isNotificationEnabled, isDarkMode), fontSize ->
            AppSettingsUiState(
                isMasterNotificationEnabled = isNotificationEnabled,
                isDarkModeEnabled = isDarkMode,
                selectedFontSize = fontSize
            )
        }
        .combine(_isFontSizeDialogVisible) { uiState, isVisible ->
            uiState.copy(isFontSizeDialogVisible = isVisible)
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

    // Dark Mode Toggle Function
    fun toggleDarkMode(enabled: Boolean) {
        preferenceManager.setDarkModeState(enabled)
    }

    // 🌟 NEW: Font Size Logic 🌟
    fun setFontSizeDialogVisibility(isVisible: Boolean) {
        _isFontSizeDialogVisible.value = isVisible
    }

    fun setFontSize(size: FontSize) {
        preferenceManager.setFontSizeState(size)
        setFontSizeDialogVisibility(false)
    }
}