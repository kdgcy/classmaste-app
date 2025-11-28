package com.kd.classmate.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.kd.classmate.utils.Routes

@Composable
fun DashboardMenu(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
            // Removed hardcoded width for natural Material 3 sizing
        ) {
            // 1. Calendar OPTION
            DropdownMenuItem(
                text = { Text("Calendar") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                    navController.navigate(Routes.calendar)
                }
            )

            // 2. FOCUS MODE (Updated Icon)
            DropdownMenuItem(
                text = { Text("Focus Mode") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Timer, // Better context than 'Alarm'
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                }
            )

            // VISUAL SEPARATOR
            HorizontalDivider()

            // 3. SETTINGS
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null
                    )
                },
                onClick = {
                    expanded = false
                }
            )
        }
    }
}