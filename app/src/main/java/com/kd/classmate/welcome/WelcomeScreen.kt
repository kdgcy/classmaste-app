package com.kd.classmate.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kd.classmate.R


@Composable
fun WelcomeImage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.welcomeimg),
            contentDescription = "First time user welcome image"
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Welcome to ClassMate",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun TaskManagement() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.taskmanagement),
            contentDescription = "Task Management"
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Master Your Workload with Tasks & Subtasks",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun SetDeadline(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.deadline),
            contentDescription = "Deadline"
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Never Miss a Deadline with Smart Reminders",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun TaskAppointment() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.taskapp),
            contentDescription = "Task Appointment"
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Organize Your Schedule & Appointments",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

//Onboarding Flow
@Composable
fun OnboardingPage() {
    //Define the state for pages
    val pagerState = rememberPagerState(pageCount = { 4 }) //set the page

    Column(modifier = Modifier.fillMaxSize()) {
        //The HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when(page) {
                0 -> WelcomeImage()
                1 -> TaskManagement()
                2 -> TaskAppointment()
                3 -> SetDeadline()
            }
        }

        //Page Indicator
        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Repeat based on TOTAL count
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(25.dp))
    }
}