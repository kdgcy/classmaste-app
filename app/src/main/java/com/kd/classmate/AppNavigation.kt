package com.kd.classmate

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kd.classmate.screens.Dashboard
import com.kd.classmate.utils.Routes

@Composable
fun AppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.dashboard) {
        composable(Routes.dashboard) { Dashboard(navController) }
    }
}