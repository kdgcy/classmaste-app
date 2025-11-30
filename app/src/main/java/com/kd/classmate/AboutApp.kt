package com.kd.classmate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star // App Icon Placeholder
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.appsetting.SettingsGroup // Reused component
import com.kd.classmate.appsetting.SettingsItem // Reused component
import com.kd.classmate.utils.Routes // Used for navigation
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


// Define App Metadata (Typically read from build config, but hardcoded here for UI demo)
private const val APP_VERSION = "1.0"
private const val CONTACT_EMAIL = "developer@classmate.com" // Placeholder email

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutApp(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("About") }, // Title simplified to 'About'
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Filled.ArrowBackIosNew, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. App Header/Logo ---
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Placeholder Logo Icon
                    Icon(
                        imageVector = Icons.Filled.Star, // Placeholder for app's primary icon
                        contentDescription = "ClassMate Logo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ClassMate",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Version $APP_VERSION",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- 2. About this App Card (Design Focus) ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About this App",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "ClassMate is your all-in-one academic sidekick. It combines a powerful Task Dashboard, a Smart Calendar for appointments, and a Pomodoro Timer to help you stay focused.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Contact Developer Button
                        Button(
                            onClick = { /* TODO: Implement Intent to open email app */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Filled.MailOutline, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Contact Developer")
                        }
                    }
                }
            }

            // --- 3. Key Features Group ---
            item {
                SettingsGroup(title = "Key Features") {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        // Task Management Feature
                        FeatureItem(
                            title = "Tasks with Subtasks & Progress",
                            icon = Icons.Filled.Task
                        )
                        // Calendar Feature
                        FeatureItem(
                            title = "Smart Calendar for Appointments",
                            icon = Icons.Filled.CalendarMonth
                        )
                        // Pomodoro Feature
                        FeatureItem(
                            title = "Focus Timer with Persistence",
                            icon = Icons.Filled.Timer
                        )
                        // Dynamic Theme
                        FeatureItem(
                            title = "Dynamic Dark Mode Support",
                            icon = Icons.Filled.NightsStay
                        )
                    }
                }
            }

            // --- 4. Pomodoro Logic Group ---
            item {
                SettingsGroup(title = "Pomodoro Logic") {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "• 25 min Work / 5 min Short Break (Customizable)",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "• After 4 Cycles: 15 min Long Break",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // --- 5. Legal Group (Simplified Footer) ---
            item {
                Text(
                    text = "© 2025 ClassMate. All rights reserved.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// 🌟 NEW: Helper Composable for Feature List (to simplify the UI) 🌟
@Composable
private fun FeatureItem(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}