package com.kd.classmate.pomodoro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kd.classmate.services.ServiceTimerState
import com.kd.classmate.services.TimerService
import com.kd.classmate.services.WakeLockManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class TimerState {
    RUNNING, PAUSED, IDLE
}
enum class CycleState {
    WORK, SHORT_BREAK, LONG_BREAK
}

// UI State remains the same, but data comes from the service
data class ServiceTimerState(
    val timeRemainingSeconds: Long = TimeUnit.MINUTES.toSeconds(25L), // Default value
    val timerState: TimerState = TimerState.IDLE,
    val cycleState: CycleState = CycleState.WORK,
    val workCyclesCompleted: Int = 0
)
// Data class to hold the persistent user settings
data class PomodoroSettings(
    val workDurationMinutes: Long = 25L,
    val shortBreakMinutes: Long = 5L,
    val longBreakMinutes: Long = 15L
)
data class PomodoroUiState(
    val timeRemainingSeconds: Long = TimeUnit.MINUTES.toSeconds(25L),
    val timerState: TimerState = TimerState.IDLE,
    val cycleState: CycleState = CycleState.WORK,
    val workCyclesCompleted: Int = 0,
    // Settings State
    val settings: PomodoroSettings = PomodoroSettings(),
    val isSettingsDialogVisible: Boolean = false
)

class PomodoroViewModel(
    private val wakeLockManager: WakeLockManager,
    private val context: Context
) : ViewModel() {

    // --- Settings and UI State Flows ---
    private val _settings = MutableStateFlow(PomodoroSettings())
    private val _isSettingsDialogVisible = MutableStateFlow(false)

    // Service Connection State
    private val _isBound = MutableStateFlow(false)
    private var timerService: TimerService? = null

    // Service Timer State Flow (default/initial flow)
    private val serviceTimerStateFlow = MutableStateFlow(ServiceTimerState())

    // Service Connection Object
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerServiceBinder
            timerService = binder.getService()

            // Collect the service's state flow
            viewModelScope.launch {
                timerService!!.timerState.collect { state ->
                    serviceTimerStateFlow.value = state
                }
            }
            _isBound.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _isBound.value = false
            timerService = null
        }
    }

    init {
        // Start and bind to the service when the ViewModel is created
        bindService(context)
        startService(context)
    }

    private fun bindService(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startService(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        // Use startForegroundService for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    // Combine flows into the public StateFlow (Maps service state to UI state)
    val uiState: StateFlow<PomodoroUiState> = serviceTimerStateFlow
        .combine(_settings) { serviceState, settings ->
            serviceState to settings
        }
        .combine(_isSettingsDialogVisible) { (serviceState, settings), isVisible ->
            PomodoroUiState(
                timeRemainingSeconds = serviceState.timeRemainingSeconds,
                timerState = serviceState.timerState,
                cycleState = serviceState.cycleState,
                workCyclesCompleted = serviceState.workCyclesCompleted,
                settings = settings,
                isSettingsDialogVisible = isVisible
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PomodoroUiState()
        )

    // --- Settings Management Functions ---

    fun setSettingsDialogVisibility(isVisible: Boolean) {
        _isSettingsDialogVisible.value = isVisible
    }

    fun updateSettings(work: Long, shortBreak: Long, longBreak: Long) {
        _settings.update {
            it.copy(
                workDurationMinutes = work,
                shortBreakMinutes = shortBreak,
                longBreakMinutes = longBreak
            )
        }
        // Call the service function to update settings
        // The service function updateSettings must be defined in TimerService.kt
        timerService?.updateSettings(work, shortBreak, longBreak)
        setSettingsDialogVisibility(false)
    }

    // --- Public Control Functions (Delegate to Service) ---

    fun toggleTimer() {
        timerService?.toggleTimer()
    }

    fun resetTimer(shouldStart: Boolean = true) {
        timerService?.resetTimer(shouldStart)
    }

    /*
    fun resetCycleCount() {
        // Needs logic in TimerService.kt if required
        // timerService?.resetCycleCount()
    }
    */

    override fun onCleared() {
        super.onCleared()
        // Unbind service when ViewModel is destroyed
        if (_isBound.value) {
            // FIX: Use the injected context to unbind the service
            context.unbindService(serviceConnection)
        }
        timerService = null
    }
}