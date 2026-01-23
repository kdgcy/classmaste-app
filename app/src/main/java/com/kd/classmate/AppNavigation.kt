package com.kd.classmate

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kd.classmate.appsetting.AppSettings
import com.kd.classmate.calendar.Calendar
import com.kd.classmate.dashboard.Dashboard
import com.kd.classmate.pomodoro.Pomodoro
import com.kd.classmate.subtasks.TaskDetails
import com.kd.classmate.utils.Routes
import com.kd.classmate.welcome.OnboardingPage

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.splash) {

        //Splash Screen
        composable(
            Routes.splash,
            enterTransition = {fadeIn(tween(100)) },
            exitTransition = {fadeOut(tween(100)) }
        ){ SplashScreen(navController) }

        //Dashboard Screen
        composable(
            Routes.dashboard,
            enterTransition = {fadeIn(tween(100)) },
            exitTransition = {fadeOut(tween(100)) }
        ) { Dashboard(navController) }

        //Calendar Screen
        composable(
            Routes.calendar,
            enterTransition = {fadeIn(tween(100)) },
            exitTransition = {fadeOut(tween(100)) }
        ) { Calendar(navController) }

        //Pomodoro Screen
        composable(
            Routes.pomodoro,
            enterTransition = {fadeIn(tween(100)) },
            exitTransition = {fadeOut(tween(100)) }
        ) { Pomodoro(navController) }

        //Application Settings Screen
        composable(
            Routes.appsettings,
            enterTransition = {fadeIn(tween(100)) },
            exitTransition = {fadeOut(tween(100)) }
        ) { AppSettings(navController) }

        //About Screen
        composable(
            Routes.about,
            enterTransition = {fadeIn(tween(100)) },
            exitTransition = {fadeOut(tween(100)) }
        ){ AboutApp(navController) }

        composable(Routes.onboarding) {
            OnboardingPage(onFinished = {
                navController.navigate(Routes.dashboard) {
                    // Prevent user from going back to onboarding
                    popUpTo(Routes.onboarding) { inclusive = true }
                }
            })
        }

        // TaskDetails
        composable(
            route = Routes.taskDetails,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType }),
            enterTransition = {fadeIn(tween(100)) },
            exitTransition = {fadeOut(tween(100)) }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            if (taskId != null) {
                // TaskDetails only needs navController and taskId
                TaskDetails(
                    navController = navController,
                    taskId = taskId
                )
            }
        }
    }
}