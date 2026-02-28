package com.kd.classmate

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kd.classmate.utils.Routes
import com.kd.classmate.welcome.OnboardingPage

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = Routes.splash
    ) {
        composable(Routes.splash) {
            // Ensure SplashScreen matches these parameter names
            SplashScreen(
                navController = rootNavController
            )
        }

        composable(Routes.onboarding) {
            OnboardingPage(onFinished = {
                rootNavController.navigate("main_home") {
                    popUpTo(Routes.onboarding) { inclusive = true }
                }
            })
        }

        composable("main_home") {
            // FIXED: Passed rootNavController to Home
            Home(rootNavController = rootNavController)
        }
    }
}