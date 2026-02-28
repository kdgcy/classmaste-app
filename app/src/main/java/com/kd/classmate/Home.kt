package com.kd.classmate

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kd.classmate.calendar.Calendar
import com.kd.classmate.dashboard.Dashboard
import com.kd.classmate.pomodoro.Pomodoro
import com.kd.classmate.utils.Routes

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun Home(rootNavController: NavController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navItemList = listOf(
        NavItem("Tasks", Icons.Default.Task, Routes.dashboard),
        NavItem("Calendar", Icons.Default.CalendarMonth, Routes.calendar),
        NavItem("Pomodoro", Icons.Default.Timer, Routes.pomodoro)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItemList.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(text = item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // FIXED: Added required navController and modifier
        NavHost(
            navController = navController,
            startDestination = Routes.dashboard,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.dashboard) {
                Dashboard(navController = navController)
            }
            composable(Routes.calendar) {
                Calendar(navController = navController)
            }
            composable(Routes.pomodoro) {
                Pomodoro(navController = navController) // Only if Pomodoro doesn't need a controller
            }

            composable(
                route = Routes.taskDetail, // This is "taskDetail/{taskId}"
                arguments = listOf(
                    androidx.navigation.navArgument("taskId") {
                        type = androidx.navigation.NavType.IntType
                    }
                )
            ) { backStackEntry ->
                // Extract the ID from the navigation arguments
                val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0

                // Call your TaskDetails screen with the extracted ID
                com.kd.classmate.subtasks.TaskDetails(
                    navController = navController,
                    taskId = taskId
                )
            }
        }
    }
}