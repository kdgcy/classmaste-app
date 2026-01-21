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
import androidx.compose.material3.Button
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
fun WelcomeScreenA() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.todo),
            contentDescription = "Sample Image 1"
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Welcome Screen A",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun WelcomeScreenB() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.todo),
            contentDescription = "Sample Image 2"
        )

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Welcome Screen A",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                //implement boolean Later for the first time user
            }
        ) { Text("Get Started") }
    }
}

@Composable
fun OnboardingPage() {
    //Define the state for pages
    val pagerState = rememberPagerState(pageCount = {2})

    Column(modifier = Modifier.fillMaxSize()) {
        //The HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when(page) {
                0 -> WelcomeScreenA()
                1 -> WelcomeScreenB()
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