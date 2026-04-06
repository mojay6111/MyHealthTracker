package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.local.dao.ActivitySessionDao
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import com.example.myhealthtracker.service.RouteTrackingService
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class RouteDetailUiState(
    val activityType: String = "WALK",
    val formattedDate: String = "",
    val formattedDuration: String = "--:--",
    val formattedDistance: String = "0 m",
    val formattedCalories: String = "0 kcal",
    val formattedAvgSpeed: String = "0.0 km/h",
    val formattedMaxSpeed: String = "0.0 km/h",
    val formattedPace: String = "--:-- /km",
    val avgPaceMinPerKm: Double = 0.0,
    val steps: Int = 0,
    val notes: String? = null,
    val routePoints: List<LatLng> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RouteDetailViewModel @Inject constructor(
    private val activitySessionDao: ActivitySessionDao,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteDetailUiState())
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = activitySessionDao.getSessionById(sessionId) ?: return@launch

            // Parse route points from JSON
            val routePoints = try {
                session.routePointsJson?.let { json ->
                    val points = Json.decodeFromString<List<RouteTrackingService.RoutePoint>>(json)
                    points.map { LatLng(it.lat, it.lng) }
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            // Format duration
            val hours = session.durationSeconds / 3600
            val minutes = (session.durationSeconds % 3600) / 60
            val secs = session.durationSeconds % 60
            val formattedDuration = if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, secs)
            } else {
                "%02d:%02d".format(minutes, secs)
            }

            // Format date
            val formattedDate = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(session.date) ?: Date()
                SimpleDateFormat(
                    "EEEE, MMM dd yyyy",
                    Locale.getDefault()
                ).format(date)
            } catch (e: Exception) {
                session.date
            }

            // Format pace
            val formattedPace = calorieCalculator.formatPace(session.avgSpeedKmh)

            _uiState.update {
                it.copy(
                    activityType = session.activityType,
                    formattedDate = formattedDate,
                    formattedDuration = formattedDuration,
                    formattedDistance = calorieCalculator.formatDistance(
                        session.distanceMetres
                    ),
                    formattedCalories = calorieCalculator.formatCalories(
                        session.caloriesBurned
                    ),
                    formattedAvgSpeed = "${"%.1f".format(session.avgSpeedKmh)} km/h",
                    formattedMaxSpeed = "${"%.1f".format(session.maxSpeedKmh)} km/h",
                    formattedPace = formattedPace,
                    avgPaceMinPerKm = session.avgPaceMinPerKm,
                    steps = session.steps,
                    notes = session.notes,
                    routePoints = routePoints,
                    isLoading = false
                )
            }
        }
    }
}