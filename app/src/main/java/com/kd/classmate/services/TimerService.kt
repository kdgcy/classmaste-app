package com.kd.classmate.services

import android.app.Notification
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
import com.kd.classmate.data.PreferenceManager
import com.kd.classmate.pomodoro.CycleState
import com.kd.classmate.pomodoro.PomodoroSettings
import com.kd.classmate.pomodoro.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import java.util.Locale
import java.util.concurrent.TimeUnit


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

private const val ACTION_STOP_TIMER = "com.kd.classmate.ACTION_STOP_TIMER" // Stop timer
private const val ACTION_TOGGLE_TIMER = "com.kd.classmate.ACTION_TOGGLE_TIMER"
class TimerService : LifecycleService(), KoinComponent {

    private val _timerState = MutableStateFlow(ServiceTimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private lateinit var wakeLockManager: WakeLockManager
    private lateinit var soundPlayer: SoundPlayer
    private lateinit var preferenceManager: PreferenceManager

    private val _currentSettings = MutableStateFlow(PomodoroSettings())

    // Binder implementation for connecting to the ViewModel
    inner class TimerServiceBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    // Settings Update Function
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
        wakeLockManager = get()
        soundPlayer = get()
        preferenceManager = get() // 🌟 FIX: Retrieve PreferenceManager here 🌟
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return TimerServiceBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_STOP_TIMER -> {
                handleStopAction() // New helper function to clean up
            }
            ACTION_TOGGLE_TIMER -> {
                toggleTimer()
                // If toggling results in the timer running, update notification
                startForeground(NOTIFICATION_ID, buildForegroundNotification())
            }
        }
        return START_STICKY
    }

    private fun handleStopAction() {
        // 1. Reset the logic state
        resetTimer(shouldStart = false)

        // 2. Stop the foreground status and remove notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        // 3. Optional: Stop the service entirely if you want it to disappear from RAM
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        wakeLockManager.releaseWakeLock()
    }

    // --- Core Timer Logic ---
    private fun startTimer() {
        timerJob?.cancel()
        wakeLockManager.acquireWakeLock() // Keep CPU awake

        _timerState.update { it.copy(timerState = TimerState.RUNNING) }

        timerJob = lifecycleScope.launch {
            while (_timerState.value.timeRemainingSeconds > 0) {
                delay(1000L)
                _timerState.update {
                    it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1)
                }

                // Push notification update every second for a real-time feel
                updateForegroundNotification()
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

    // In TimerService.kt

    private fun handleCycleEnd() {
        timerJob?.cancel()
        wakeLockManager.releaseWakeLock()

        val preferenceManager: PreferenceManager = get()

        // Master state
        val isMasterEnabled = preferenceManager.getMasterNotificationState().value

        // Check if the user has the sound preference enabled AND the master is enabled
        if (isMasterEnabled) {
            soundPlayer.playCycleEndSound() // Sound logic assumes only the master switch controls it now
        }

        if (isMasterEnabled) {
            updateForegroundNotification() // Banner logic
        }


        _timerState.update { currentState ->
            val settings = _currentSettings.value

            val nextCycle = when (currentState.cycleState) {
                CycleState.WORK -> {
                    val newCycles = currentState.workCyclesCompleted + 1
                    if (newCycles % CYCLES_BEFORE_LONG_BREAK == 0) CycleState.LONG_BREAK else CycleState.SHORT_BREAK
                }
                CycleState.SHORT_BREAK, CycleState.LONG_BREAK -> CycleState.WORK
            }

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
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        // Action 0: Toggle (Pause/Resume)
        val togglePendingIntent = PendingIntent.getService(
            this, 2,
            Intent(this, TimerService::class.java).apply { action = ACTION_TOGGLE_TIMER },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action 1: Stop
        val stopPendingIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TimerService::class.java).apply { action = ACTION_STOP_TIMER },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val state = _timerState.value
        val isRunning = state.timerState == TimerState.RUNNING

        val formattedTime = String.format(
            Locale.getDefault(), "%02d:%02d",
            state.timeRemainingSeconds / 60,
            state.timeRemainingSeconds % 60
        )

        return NotificationCompat.Builder(this, "POMODORO_TIMER_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${formatCycleText(state.cycleState)} Mode")
            .setContentText("Time remaining: $formattedTime")
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)

            // ADD ACTIONS
            .addAction(
                if (isRunning) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isRunning) "Pause" else "Resume",
                togglePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )

            //  APPLY MEDIA STYLE
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
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