// File: MainActivity.kt (UPDATED)
package com.kd.classmate

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import android.Manifest // NEW IMPORT
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    // ... (Existing variables remain the same) ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🌟 NEW: Request POST_NOTIFICATIONS permission at runtime 🌟
        requestNotificationPermission()

        setContent {
            AppNavigation()
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
        // For older versions, the permission is automatically granted by the manifest.
    }
}