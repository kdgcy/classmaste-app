package com.kd.classmate

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import com.kd.classmate.appsetting.AppSettingsViewModel // NEW IMPORT
import com.kd.classmate.ui.theme.ClassMateTheme // Assume theme file is here
import org.koin.android.ext.android.inject // NEW IMPORT
import androidx.compose.foundation.isSystemInDarkTheme // NEW IMPORT

class MainActivity : ComponentActivity() {

    // 🌟 NEW: Inject the ViewModel lazily to access the theme state 🌟
    private val appSettingsViewModel: AppSettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        setContent {
            // Collect the Dark Mode preference state from the ViewModel
            val settingsState by appSettingsViewModel.uiState.collectAsState()

            // Determine whether to use Dark Theme
            val useDarkTheme = settingsState.isDarkModeEnabled

            // 🌟 FIX: Apply the theme based on the preference state 🌟
            ClassMateTheme(darkTheme = useDarkTheme) {
                AppNavigation()
            }
        }
    }

    private fun requestNotificationPermission() {
        // Check only on Android 13 (API 33) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            // If permission is not granted, request it
            if (permissionStatus != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001 // Request code
                )
            }
        }
    }
}