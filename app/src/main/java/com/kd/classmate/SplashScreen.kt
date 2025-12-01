package com.kd.classmate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School // 🌟 NEW ICON: Represents the cap/graduation 🌟
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kd.classmate.utils.Routes
import kotlinx.coroutines.delay
// NOTE: We assume the background gradient is no longer required based on the final logo image
// which uses a solid white background and a colored circle.

@Composable
fun SplashScreen(navController: NavController) {
    // 1. The Navigation Logic
    LaunchedEffect(Unit) {
        delay(3000) // Adjust delay as needed (3000ms = 3 seconds)
        navController.navigate(Routes.dashboard) {
            popUpTo(Routes.splash) { inclusive = true }
        }
    }

    // 2. The UI Design (Matching the provided logo image aesthetic)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // 🌟 FIX: Solid White Background (as per logo image) 🌟
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // LOGO (Teal circle background, White icon)
            Surface(
                shape = CircleShape,
                color = Color(0xFF1FAB89), // 🌟 FIX: Teal/Green background color 🌟
                modifier = Modifier.size(120.dp),
                shadowElevation = 10.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.School, // 🌟 FIX: Using a generic academic icon 🌟
                        contentDescription = "ClassMate Logo",
                        tint = Color.White, // White icon
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Increased spacing

            // APP NAME
            Text(
                text = "ClassMate",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground // Use dynamic color
            )

            Spacer(modifier = Modifier.height(48.dp))

            // LOADING BAR (Simplified for the final look)
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary, // Use theme primary color
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}