package com.kd.classmate

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kd.classmate.appsetting.AppSettings
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
        NavItem("Pomodoro", Icons.Default.Timer, Routes.pomodoro),
        NavItem("Settings", Icons.Default.Settings, Routes.settings)
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
        NavHost(
            navController = navController,
            startDestination = Routes.dashboard,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Helper function to determine slide direction based on tab index
            fun getTransitionDirection(initial: NavBackStackEntry, target: NavBackStackEntry): AnimatedContentTransitionScope.SlideDirection {
                val navItems = listOf(Routes.dashboard, Routes.calendar, Routes.pomodoro, Routes.settings)
                val initialRoute = initial.destination.route
                val targetRoute = target.destination.route

                val initialIndex = navItems.indexOf(initialRoute)
                val targetIndex = navItems.indexOf(targetRoute)

                // If moving to a higher index (e.g., Tasks -> Calendar), slide Left
                return if (targetIndex > initialIndex) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                }
            }

            // DASHBOARD
            composable(
                route = Routes.dashboard,
                enterTransition = {
                    slideIntoContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeIn()
                },
                exitTransition = {
                    slideOutOfContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeOut()
                }
            ) {
                Dashboard(navController = navController)
            }

            // CALENDAR
            composable(
                route = Routes.calendar,
                enterTransition = {
                    slideIntoContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeIn()
                },
                exitTransition = {
                    slideOutOfContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeOut()
                }
            ) {
                Calendar(navController = navController)
            }

            // POMODORO
            composable(
                route = Routes.pomodoro,
                enterTransition = {
                    slideIntoContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeIn()
                },
                exitTransition = {
                    slideOutOfContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeOut()
                }
            ) {
                Pomodoro(navController = navController)
            }

            composable(
                route = Routes.settings,
                enterTransition = {
                    slideIntoContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeIn()
                },
                exitTransition = {
                    slideOutOfContainer(getTransitionDirection(initialState, targetState), tween(400)) + fadeOut()
                }
            ) {
                AppSettings(navController = navController)
            }

            // --- TASK DETAIL WITH SLIDE ANIMATION ---
            composable(
                route = Routes.taskDetail,
                arguments = listOf(androidx.navigation.navArgument("taskId") {
                    type = androidx.navigation.NavType.IntType
                }),
                // Animations
                enterTransition = {
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = androidx.compose.animation.core.tween(300)
                    )
                },
                exitTransition = {
                    androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = androidx.compose.animation.core.tween(300)
                    )
                }
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
                com.kd.classmate.subtasks.TaskDetails(
                    navController = navController,
                    taskId = taskId
                )
            }
        }
    }
}