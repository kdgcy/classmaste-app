package com.kd.classmate.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

// Constants (Default Pomodoro configuration)
private const val WORK_TIME_MINUTES = 25L
private const val SHORT_BREAK_MINUTES = 5L
private const val LONG_BREAK_MINUTES = 15L
private const val CYCLES_BEFORE_LONG_BREAK = 4

enum class TimerState {
    RUNNING, PAUSED, IDLE
}

enum class CycleState {
    WORK, SHORT_BREAK, LONG_BREAK
}

data class PomodoroUiState(
    val timeRemainingSeconds: Long = TimeUnit.MINUTES.toSeconds(WORK_TIME_MINUTES),
    val timerState: TimerState = TimerState.IDLE,
    val cycleState: CycleState = CycleState.WORK,
    val workCyclesCompleted: Int = 0
)

class PomodoroViewModel : ViewModel() {

    // Internal State Flows
    private val _timeRemainingSeconds = MutableStateFlow(TimeUnit.MINUTES.toSeconds(WORK_TIME_MINUTES))
    private val _timerState = MutableStateFlow(TimerState.IDLE)
    private val _cycleState = MutableStateFlow(CycleState.WORK)
    private val _workCyclesCompleted = MutableStateFlow(0)

    private var timerJob: Job? = null

    // Combine flows into the public StateFlow
    val uiState: StateFlow<PomodoroUiState> = combine(
        _timeRemainingSeconds, _timerState, _cycleState, _workCyclesCompleted
    ) { time, timerState, cycleState, cyclesCompleted ->
        PomodoroUiState(
            timeRemainingSeconds = time,
            timerState = timerState,
            cycleState = cycleState,
            workCyclesCompleted = cyclesCompleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PomodoroUiState()
    )

    // --- Core Timer Logic ---

    private fun startTimer() {
        // Cancel any existing job
        timerJob?.cancel()

        _timerState.value = TimerState.RUNNING

        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0) {
                delay(1000L) // Wait for 1 second
                _timeRemainingSeconds.update { it - 1 }
            }
            // Timer reached 0, auto-transition to the next cycle
            handleCycleEnd()
        }
    }

    private fun handleCycleEnd() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE

        val nextCycle = when (_cycleState.value) {
            CycleState.WORK -> {
                _workCyclesCompleted.update { it + 1 }
                if (_workCyclesCompleted.value % CYCLES_BEFORE_LONG_BREAK == 0) {
                    CycleState.LONG_BREAK
                } else {
                    CycleState.SHORT_BREAK
                }
            }
            CycleState.SHORT_BREAK, CycleState.LONG_BREAK -> CycleState.WORK
        }

        _cycleState.value = nextCycle
        resetTimer(shouldStart = false)
    }

    // --- Public Control Functions ---

    fun toggleTimer() {
        when (_timerState.value) {
            TimerState.RUNNING -> {
                timerJob?.cancel()
                _timerState.value = TimerState.PAUSED
            }
            TimerState.PAUSED, TimerState.IDLE -> {
                startTimer()
            }
        }
    }

    fun resetTimer(shouldStart: Boolean = true) {
        timerJob?.cancel()

        val newTime = when (_cycleState.value) {
            CycleState.WORK -> WORK_TIME_MINUTES
            CycleState.SHORT_BREAK -> SHORT_BREAK_MINUTES
            CycleState.LONG_BREAK -> LONG_BREAK_MINUTES
        }

        _timeRemainingSeconds.value = TimeUnit.MINUTES.toSeconds(newTime)
        _timerState.value = TimerState.IDLE

        if (shouldStart) {
            startTimer()
        }
    }

    // Reset the cycle count to zero
    fun resetCycleCount() {
        _workCyclesCompleted.value = 0
        _cycleState.value = CycleState.WORK
        resetTimer(shouldStart = false)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}