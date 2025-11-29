package com.kd.classmate.services

// 🌟 FIX: Add missing imports for LOCALE and PowerManager 🌟
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.kd.classmate.MainActivity
import com.kd.classmate.pomodoro.CycleState
import com.kd.classmate.pomodoro.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.kd.classmate.pomodoro.PomodoroSettings

// These constants must be defined outside the class body if they are top-level constants.
private var WORK_TIME_MINUTES = 25L
private var SHORT_BREAK_MINUTES = 5L
private var LONG_BREAK_MINUTES = 15L
private const val CYCLES_BEFORE_LONG_BREAK = 4
private const val NOTIFICATION_ID = 101

// Data class reflecting the timer's state
data class ServiceTimerState(
    val timeRemainingSeconds: Long = TimeUnit.MINUTES.toSeconds(WORK_TIME_MINUTES),
    val timerState: TimerState = TimerState.IDLE,
    val cycleState: CycleState = CycleState.WORK,
    val workCyclesCompleted: Int = 0
)

class TimerService : LifecycleService() {

    private val _timerState = MutableStateFlow(ServiceTimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private lateinit var wakeLockManager: WakeLockManager

    //  NEW: Internal mutable settings state
    private val _currentSettings = MutableStateFlow(PomodoroSettings())

    // Binder implementation for connecting to the ViewModel
    inner class TimerServiceBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    // --- 🌟 NEW: Settings Update Function 🌟
    fun updateSettings(work: Long, shortBreak: Long, longBreak: Long) {
        _currentSettings.update {
            it.copy(
                workDurationMinutes = work,
                shortBreakMinutes = shortBreak,
                longBreakMinutes = longBreak
            )
        }

        // Force a timer reset to apply the new duration immediately if the timer is not running
        if (_timerState.value.timerState != TimerState.RUNNING) {
            resetTimer(shouldStart = false)
        }
    }

    // --- Lifecycle Overrides ---

    override fun onCreate() {
        super.onCreate()
        // 🌟 FIX: Assuming WakeLockManagerImpl takes the Application Context 🌟
        wakeLockManager = WakeLockManagerImpl(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return TimerServiceBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        wakeLockManager.releaseWakeLock()
    }

    // --- Core Timer Logic ---
    private fun startTimer() {
        timerJob?.cancel()
        wakeLockManager.acquireWakeLock()

        _timerState.update { it.copy(timerState = TimerState.RUNNING) }

        timerJob = lifecycleScope.launch {
            while (_timerState.value.timeRemainingSeconds > 0) {
                delay(1000L)
                _timerState.update {
                    it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1)
                }
                if (_timerState.value.timeRemainingSeconds % 60 == 0L) {
                    updateForegroundNotification()
                }
            }
            handleCycleEnd()
        }
    }

    fun toggleTimer() {
        when (_timerState.value.timerState) {
            TimerState.RUNNING -> {
                timerJob?.cancel()
                _timerState.update { it.copy(timerState = TimerState.PAUSED) }
                wakeLockManager.releaseWakeLock()
            }
            TimerState.PAUSED, TimerState.IDLE -> startTimer()
        }
    }

    // --- Cycle Logic ---

    private fun handleCycleEnd() {
        timerJob?.cancel()
        wakeLockManager.releaseWakeLock()

        _timerState.update { currentState ->
            val settings = _currentSettings.value // Get current settings

            val nextCycle = when (currentState.cycleState) {
                CycleState.WORK -> {
                    val newCycles = currentState.workCyclesCompleted + 1
                    if (newCycles % CYCLES_BEFORE_LONG_BREAK == 0) CycleState.LONG_BREAK else CycleState.SHORT_BREAK
                }
                CycleState.SHORT_BREAK, CycleState.LONG_BREAK -> CycleState.WORK
            }

            // Get the duration from the current settings
            val newTime = when (nextCycle) {
                CycleState.WORK -> settings.workDurationMinutes
                CycleState.SHORT_BREAK -> settings.shortBreakMinutes
                CycleState.LONG_BREAK -> settings.longBreakMinutes
            }

            currentState.copy(
                timerState = TimerState.IDLE,
                cycleState = nextCycle,
                workCyclesCompleted = if (nextCycle == CycleState.WORK) currentState.workCyclesCompleted + 1 else currentState.workCyclesCompleted,
                timeRemainingSeconds = TimeUnit.MINUTES.toSeconds(newTime)
            )
        }
    }

    // --- Public Utility Functions ---

    fun resetTimer(shouldStart: Boolean = true) {
        timerJob?.cancel()
        wakeLockManager.releaseWakeLock()

        val settings = _currentSettings.value // Get current settings

        _timerState.update { currentState ->
            val newTime = when (currentState.cycleState) {
                CycleState.WORK -> settings.workDurationMinutes
                CycleState.SHORT_BREAK -> settings.shortBreakMinutes
                CycleState.LONG_BREAK -> settings.longBreakMinutes
            }
            currentState.copy(
                timerState = TimerState.IDLE,
                timeRemainingSeconds = TimeUnit.MINUTES.toSeconds(newTime)
            )
        }
        if (shouldStart) toggleTimer()
    }

    // --- Notification Handling ---

    private fun buildForegroundNotification(): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val channelId = "POMODORO_TIMER_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 🌟 FIX: Use Locale for String.format 🌟
        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", TimeUnit.SECONDS.toMinutes(_timerState.value.timeRemainingSeconds), _timerState.value.timeRemainingSeconds % 60)

        val iconId = android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Session: ${formatCycleText(_timerState.value.cycleState)}")
            .setContentText("Time remaining: $formattedTime")
            .setSmallIcon(iconId)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateForegroundNotification() {
        val notification = buildForegroundNotification()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatCycleText(cycle: CycleState): String {
        return when(cycle) {
            CycleState.WORK -> "FOCUS"
            CycleState.SHORT_BREAK -> "SHORT BREAK"
            CycleState.LONG_BREAK -> "LONG BREAK"
        }
    }
}