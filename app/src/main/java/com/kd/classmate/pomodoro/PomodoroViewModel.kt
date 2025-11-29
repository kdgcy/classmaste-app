package com.kd.classmate.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.kd.classmate.services.TimerService.TimerServiceBinder
import com.kd.classmate.services.ServiceTimerState
import com.kd.classmate.services.TimerService
import com.kd.classmate.services.WakeLockManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import android.os.Build
import java.util.concurrent.TimeUnit
import android.os.PowerManager // Added back for completeness

// Constants (moved to service, but defined here for local state consistency)
private const val WORK_TIME_MINUTES = 25L

enum class TimerState {
    RUNNING, PAUSED, IDLE
}
enum class CycleState {
    WORK, SHORT_BREAK, LONG_BREAK
}

// UI State remains the same, but data comes from the service
data class PomodoroUiState(
    val timeRemainingSeconds: Long = TimeUnit.MINUTES.toSeconds(WORK_TIME_MINUTES),
    val timerState: TimerState = TimerState.IDLE,
    val cycleState: CycleState = CycleState.WORK,
    val workCyclesCompleted: Int = 0
)

class PomodoroViewModel(
    private val wakeLockManager: WakeLockManager,
    private val context: Context // Context is required for binding the service
) : ViewModel() {

    // 1. Service Connection State
    private val _isBound = MutableStateFlow(false)
    private var timerService: TimerService? = null

    // 2. Service Timer State Flow (default/initial flow)
    private val serviceTimerStateFlow = MutableStateFlow(ServiceTimerState())

    // 3. Service Connection Object
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
        .map { serviceState ->
            PomodoroUiState(
                timeRemainingSeconds = serviceState.timeRemainingSeconds,
                timerState = serviceState.timerState,
                cycleState = serviceState.cycleState,
                workCyclesCompleted = serviceState.workCyclesCompleted
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PomodoroUiState()
        )

    // --- Public Control Functions (Delegate to Service) ---

    fun toggleTimer() {
        timerService?.toggleTimer()
    }

    fun resetTimer(shouldStart: Boolean = true) {
        timerService?.resetTimer(shouldStart)
    }

    fun resetCycleCount() {
        // Needs logic in TimerService.kt if required
        // timerService?.resetCycleCount()
    }

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