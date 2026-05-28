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
import androidx.compose.runtime.CompositionLocalProvider // EXISTING IMPORT
import com.kd.classmate.appsetting.AppSettingsViewModel
import com.kd.classmate.ui.theme.ClassMateTheme
import org.koin.android.ext.android.inject
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density


class MainActivity : ComponentActivity() {

    // Inject the ViewModel lazily to access the theme state
    private val appSettingsViewModel: AppSettingsViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        requestNotificationPermission()

        setContent {
            // Collect the theme preference state from the ViewModel.
            val settingsState by appSettingsViewModel.uiState.collectAsState()

            // Determine whether to use Dark Theme
            val useDarkTheme = settingsState.isDarkModeEnabled

            // Get the selected scale factor
            val customScaleFactor = settingsState.selectedFontSize.scaleFactor

            // Override LocalDensity to apply global text scaling
            CompositionLocalProvider(
                LocalDensity provides Density(
                    // Pass density as the first positional argument
                    density = LocalDensity.current.density,
                    // Pass fontScale as the second named argument
                    fontScale = customScaleFactor
                )
            ) {
                ClassMateTheme(darkTheme = useDarkTheme) {
                    AppNavigation()
                }
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "calendar_channel", // THIS ID MUST MATCH YOUR RECEIVER
                "Calendar Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for your scheduled appointments"
            }
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}