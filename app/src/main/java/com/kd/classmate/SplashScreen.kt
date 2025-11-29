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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kd.classmate.utils.Routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // 1. The Navigation Logic
    // Wait for 3 seconds (or until data loads) then go to Dashboard
    LaunchedEffect(Unit) {
        delay(3000) // Adjust delay as needed (3000ms = 3 seconds)
        navController.navigate(Routes.dashboard) {
            // Removes the Splash Screen from the back stack so pressing "Back" doesn't return to it
            popUpTo(Routes.splash) { inclusive = true }
        }
    }

    // 2. The UI Design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                // The Purple Gradient Background
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9C7DFF), // Light Purple (Top)
                        Color(0xFFF5F3FF)  // White/Light (Bottom)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // LOGO (White circle background)
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(120.dp),
                shadowElevation = 10.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Replace with your actual Icon drawable
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        tint = Color(0xFF6750A4), // Deep Purple Logo Tint
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // APP NAME
            Text(
                text = "ClassMate", // Or "ClassMate"
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // TAGLINE
            Text(
                text = "Your academic sidekick for tackling\ntasks, deadlines and everything in between!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // LOADING BAR
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Width of the bar
                    .height(4.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // LOADING TEXT
            Text(
                text = "Loading your workspace...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}