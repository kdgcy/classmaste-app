package com.kd.classmate.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kd.classmate.data.Task
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

@Composable
fun DayView(
    uiState: CalendarUiState,
    onTaskClick: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.scheduledTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No appointments for this day.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(uiState.scheduledTasks) { task ->
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