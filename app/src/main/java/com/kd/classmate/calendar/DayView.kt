package com.kd.classmate.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kd.classmate.data.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

@Composable
fun DayView(
    uiState: CalendarUiState,
    onDateChanged: (LocalDate) -> Unit, // New callback to sync with ViewModel
    onTaskClick: (Task) -> Unit
) {
    // 1. Create a large range for "infinite" swiping
    // We'll treat page 5000 as "Today"
    val initialPage = 5000
    val pagerState = rememberPagerState(initialPage = initialPage) { 10000 }

    // 2. Sync Pager State back to ViewModel
    LaunchedEffect(pagerState.currentPage) {
        val dateOffset = pagerState.currentPage - initialPage
        val targetDate = LocalDate.now().plusDays(dateOffset.toLong())
        if (targetDate != uiState.selectedDate) {
            onDateChanged(targetDate)
        }
    }

    // 3. Sync ViewModel back to Pager (if user clicks a date in the Month grid)
    LaunchedEffect(uiState.selectedDate) {
        val dateOffset = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), uiState.selectedDate).toInt()
        val targetPage = initialPage + dateOffset
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Date Header for the swiped page
        Text(
            text = uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // In a pager, each "page" needs its own content.
            // The logic inside CalendarViewModel already filters 'scheduledTasks'
            // based on 'selectedDate', so we just display that list.
            DayScheduleList(uiState.scheduledTasks, onTaskClick)
        }
    }
}

@Composable
private fun DayScheduleList(tasks: List<Task>, onTaskClick: (Task) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (tasks.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No appointments for this day.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        } else {
            items(tasks) { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTaskClick(task) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Time Indicator Column
                    Column(
                        modifier = Modifier.width(70.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = task.dueTime?.format(timeFormatter) ?: "00:00",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Vertical Timeline Line
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(2.dp)
                            .height(50.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // 3. Appointment Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "ClassMate Appointment",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayTaskRow(task: Task, onTaskClick: (Task) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onTaskClick(task) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(70.dp), horizontalAlignment = Alignment.End) {
            Text(
                text = task.dueTime?.format(timeFormatter) ?: "00:00",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.padding(horizontal = 12.dp).width(2.dp).height(50.dp).background(MaterialTheme.colorScheme.outlineVariant))
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("ClassMate Appointment", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}