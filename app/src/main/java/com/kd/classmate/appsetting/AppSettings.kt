package com.kd.classmate.appsetting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive // NEW ICON
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
import org.koin.androidx.compose.koinViewModel // NEW IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettings(navController: NavController) {

    // 🌟 NEW: Initialize ViewModel and collect state 🌟
    val viewModel: AppSettingsViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value

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
            // --- 1. Productivity Settings ---
            item {
                SettingsGroup("Productivity") {
                    // 🌟 NEW: Pomodoro Sound Switch 🌟
                    SettingsSwitch(
                        title = "Pomodoro Timer Sound",
                        icon = Icons.Default.NotificationsActive,
                        checked = uiState.isPomodoroSoundEnabled,
                        onCheckedChange = viewModel::togglePomodoroSound
                    )
                }
            }

            // --- 2. About Section ---
            item {
                SettingsGroup("About") {
                    SettingsItem(
                        title = "ClassMate",
                        icon = Icons.Default.Info,
                        onClick = {  }
                    )
                }
            }
        }
    }
}