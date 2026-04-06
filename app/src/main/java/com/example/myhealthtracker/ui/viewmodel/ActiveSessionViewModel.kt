package com.example.myhealthtracker.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import com.example.myhealthtracker.service.RouteTrackingService
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveSessionUiState(
    val activityType: String = "WALK",
    val detectedActivity: String = "WALK",
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val durationSeconds: Long = 0L,
    val distanceMetres: Double = 0.0,
    val speedKmh: Double = 0.0,
    val calories: Double = 0.0,
    val currentLatLng: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val formattedDuration: String = "00:00",
    val formattedDistance: String = "0.00 km",
    val formattedSpeed: String = "0.0 km/h",
    val formattedCalories: String = "0 kcal"
)

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    application: Application,
    private val calorieCalculator: CalorieCalculator
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null
    private var elapsedSeconds = 0L

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != RouteTrackingService.ACTION_LOCATION_UPDATE) return

            val lat = intent.getDoubleExtra(RouteTrackingService.EXTRA_LATITUDE, 0.0)
            val lng = intent.getDoubleExtra(RouteTrackingService.EXTRA_LONGITUDE, 0.0)
            val distance = intent.getFloatExtra(
                RouteTrackingService.EXTRA_DISTANCE, 0f
            ).toDouble()
            val speed = intent.getFloatExtra(
                RouteTrackingService.EXTRA_SPEED_KMH, 0f
            ).toDouble()
            val duration = intent.getLongExtra(RouteTrackingService.EXTRA_DURATION, 0L)
            val detected = intent.getStringExtra(
                RouteTrackingService.EXTRA_DETECTED_ACTIVITY
            ) ?: "WALK"

            val latLng = LatLng(lat, lng)

            _uiState.update { state ->
                val newPoints = state.routePoints + latLng
                state.copy(
                    currentLatLng = latLng,
                    routePoints = newPoints,
                    distanceMetres = distance,
                    speedKmh = speed,
                    durationSeconds = duration,
                    detectedActivity = detected,
                    formattedDistance = calorieCalculator.formatDistance(distance),
                    formattedSpeed = "${"%.1f".format(speed)} km/h",
                    formattedDuration = formatDuration(duration)
                )
            }
        }
    }

    init {
        registerReceiver()
    }

    private fun registerReceiver() {
        val filter = IntentFilter(RouteTrackingService.ACTION_LOCATION_UPDATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(
                locationReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            getApplication<Application>().registerReceiver(locationReceiver, filter)
        }
    }

    fun startSession(activityType: String) {
        _uiState.update {
            it.copy(
                activityType = activityType,
                isTracking = true,
                isPaused = false,
                routePoints = emptyList(),
                durationSeconds = 0L
            )
        }
        startTimer()

        val intent = Intent(
            getApplication(),
            RouteTrackingService::class.java
        ).apply {
            action = RouteTrackingService.ACTION_START
            putExtra(RouteTrackingService.EXTRA_ACTIVITY_TYPE, activityType)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
    }

    fun pauseSession() {
        _uiState.update { it.copy(isPaused = true) }
        timerJob?.cancel()
        val intent = Intent(
            getApplication(),
            RouteTrackingService::class.java
        ).apply { action = RouteTrackingService.ACTION_PAUSE }
        getApplication<Application>().startService(intent)
    }

    fun resumeSession() {
        _uiState.update { it.copy(isPaused = false) }
        startTimer()
        val intent = Intent(
            getApplication(),
            RouteTrackingService::class.java
        ).apply { action = RouteTrackingService.ACTION_RESUME }
        getApplication<Application>().startService(intent)
    }

    fun stopSession() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isTracking = false,
                isFinished = true
            )
        }
        val intent = Intent(
            getApplication(),
            RouteTrackingService::class.java
        ).apply { action = RouteTrackingService.ACTION_STOP }
        getApplication<Application>().startService(intent)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedSeconds++
                _uiState.update {
                    it.copy(
                        durationSeconds = elapsedSeconds,
                        formattedDuration = formatDuration(elapsedSeconds)
                    )
                }
            }
        }
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, secs)
        } else {
            "%02d:%02d".format(minutes, secs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        try {
            getApplication<Application>().unregisterReceiver(locationReceiver)
        } catch (e: Exception) {
            // already unregistered
        }
    }
}