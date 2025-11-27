package com.kd.classmate

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider // Import for the factory type
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kd.classmate.dashboard.Dashboard
import com.kd.classmate.subtasks.TaskDetails
import com.kd.classmate.utils.Routes

@Composable
fun AppNavigation(factory: ViewModelProvider.Factory){ // Accept the factory here
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.dashboard,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        }
    ) {
        // Pass the factory down to the Dashboard composable
        composable(Routes.dashboard) { Dashboard(navController, factory) }
        // NEW: TaskDetails Composable
        composable(
            route = Routes.taskDetails,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Extract the taskId argument
            val taskId = backStackEntry.arguments?.getInt("taskId")
            if (taskId != null) {
                // Pass the taskId and factory to the TaskDetails screen
                TaskDetails(
                    navController = navController,
                    taskId = taskId,
                    factory = factory
                )
            }
        }
    }
}