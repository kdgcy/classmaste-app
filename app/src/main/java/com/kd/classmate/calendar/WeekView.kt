package com.kd.classmate.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kd.classmate.data.Task
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun WeekView(
    uiState: CalendarUiState,
    onDateSelected: (LocalDate) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    val startOfWeek = uiState.selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Horizontal Week Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDays.forEach { date ->
                val isSelected = date == uiState.selectedDate
                val dayName = date.dayOfWeek.name.take(1) // "M", "T", etc.

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onDateSelected(date) }
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = date.dayOfMonth.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

        // 2. Weekly Agenda List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.scheduledTasks.isEmpty()) {
                item {
                    Text(
                        "No appointments this week.",
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                items(uiState.scheduledTasks) { task ->
                    WeekTaskItem(task, timeFormatter, onTaskClick)
                }
            }
        }
    }
}

@Composable
fun WeekTaskItem(task: Task, formatter: DateTimeFormatter, onClick: (Task) -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE")
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(task) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(task.dueDate?.format(dateFormatter) ?: "", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(task.dueDate?.dayOfMonth.toString(), fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = task.dueTime?.format(formatter) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}