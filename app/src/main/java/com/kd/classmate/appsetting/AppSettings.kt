package com.kd.classmate.appsetting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.FontSizeDialog
import com.kd.classmate.utils.Routes
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettings(navController: NavController) {

    // Initialize ViewModel and collect state
    val viewModel: AppSettingsViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    // Font Size Dialog Host
    if (uiState.isFontSizeDialogVisible) {
        FontSizeDialog(
            currentSize = uiState.selectedFontSize,
            onSizeSelected = viewModel::setFontSize,
            onDismiss = { viewModel.setFontSizeDialogVisibility(false) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew,contentDescription = null)
                    }
                }
            )
        }
    ) {paddingValues ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item {
                SettingsGroup("General") {
                    // Font Size Item
                    SettingsItem(
                        title = "Font Size",
                        icon = Icons.Default.TextFields, // Use TextFields icon
                        onClick = { viewModel.setFontSizeDialogVisibility(true) } // Show dialog
                    )

                    // Dark Mode Toggle
                    SettingsSwitch(
                        title = "Dark Mode",
                        icon = Icons.Default.Palette,
                        checked = uiState.isDarkModeEnabled,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }
            // --- 1. Master Notification Control ---
            item {
                SettingsGroup("Notifications & Reminders") {
                    //  Master Switch
                    SettingsSwitch(
                        title = "Master Notifications Switch",
                        icon = Icons.Default.NotificationsActive,
                        checked = uiState.isMasterNotificationEnabled,
                        onCheckedChange = viewModel::toggleMasterNotification
                    )
                }
            }

            // --- 2. About Section ---
            item {
                SettingsGroup("About") {
                    SettingsItem(
                        title = "ClassMate",
                        icon = Icons.Default.Info,
                        onClick = { navController.navigate(Routes.about) }
                    )
                }
            }
        }
    }
}