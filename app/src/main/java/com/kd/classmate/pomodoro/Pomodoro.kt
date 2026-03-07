package com.kd.classmate.pomodoro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kd.classmate.components.PomodoroSettingsDialog
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pomodoro(navController: NavController) {

    val viewModel: PomodoroViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    // --- Time Formatting and Logic ---
    val minutes = TimeUnit.SECONDS.toMinutes(uiState.timeRemainingSeconds)
    val seconds = uiState.timeRemainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val totalTimeSeconds = when (uiState.cycleState) {
        CycleState.WORK -> TimeUnit.MINUTES.toSeconds(uiState.settings.workDurationMinutes)
        CycleState.SHORT_BREAK -> TimeUnit.MINUTES.toSeconds(uiState.settings.shortBreakMinutes)
        CycleState.LONG_BREAK -> TimeUnit.MINUTES.toSeconds(uiState.settings.longBreakMinutes)
    }

    val progress = uiState.timeRemainingSeconds.toFloat() / totalTimeSeconds.toFloat()

    val currentCycleText = when (uiState.cycleState) {
        CycleState.WORK -> "FOCUS"
        CycleState.SHORT_BREAK -> "SHORT BREAK"
        CycleState.LONG_BREAK -> "LONG BREAK"
    }

    val targetColor = when (uiState.cycleState) {
        CycleState.WORK -> MaterialTheme.colorScheme.primary
        CycleState.SHORT_BREAK, CycleState.LONG_BREAK -> MaterialTheme.colorScheme.secondary
    }

    val ringColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500)
    )

    val isTimerActive = uiState.timerState != TimerState.IDLE

    // --- Settings Dialog Hosting ---
    if (uiState.isSettingsDialogVisible) {
        PomodoroSettingsDialog(
            currentSettings = uiState.settings,
            onSave = viewModel::updateSettings,
            onDismiss = { viewModel.setSettingsDialogVisibility(false) }
        )
    }

    // --- MAIN LAYOUT (TimerPill removed for Image 2 behavior) ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Focus Session") },
                actions = {
                    IconButton(onClick = { viewModel.setSettingsDialogVisibility(true) }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CYCLE ${uiState.workCyclesCompleted + 1} / 4",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                        CircularProgressIndicator(
                            progress = 1f,
                            modifier = Modifier.size(280.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            strokeWidth = 10.dp
                        )
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(280.dp),
                            color = ringColor,
                            strokeWidth = 15.dp,
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentCycleText,
                                style = MaterialTheme.typography.titleMedium,
                                color = ringColor
                            )
                        }
                    }
                }
            }

            // Controls Row
            Row(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = { viewModel.resetTimer(shouldStart = false) },
                    modifier = Modifier.size(56.dp),
                    enabled = isTimerActive
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset",
                        tint = if (isTimerActive) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.size(32.dp))
                Button(
                    onClick = viewModel::toggleTimer,
                    modifier = Modifier.size(96.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ringColor)
                ) {
                    Icon(
                        imageVector = if (uiState.timerState == TimerState.RUNNING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Toggle",
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.size(56.dp))
            }
        }
    }
}