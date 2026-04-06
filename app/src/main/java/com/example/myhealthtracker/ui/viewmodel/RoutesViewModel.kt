package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.local.dao.ActivitySessionDao
import com.example.myhealthtracker.data.local.entity.ActivitySessionEntity
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class RoutesUiState(
    val sessions: List<ActivitySessionEntity> = emptyList(),
    val filteredSessions: List<ActivitySessionEntity> = emptyList(),
    val selectedFilter: String = "All"
)

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val activitySessionDao: ActivitySessionDao,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesUiState())
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            activitySessionDao.getAllSessions().collect { sessions ->
                _uiState.update {
                    it.copy(
                        sessions = sessions,
                        filteredSessions = filterSessions(
                            sessions,
                            it.selectedFilter
                        )
                    )
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredSessions = filterSessions(it.sessions, filter)
            )
        }
    }

    private fun filterSessions(
        sessions: List<ActivitySessionEntity>,
        filter: String
    ): List<ActivitySessionEntity> {
        return if (filter == "All") sessions
        else sessions.filter {
            it.activityType.equals(filter.uppercase(), ignoreCase = true)
        }
    }

    fun formatDistance(metres: Double) =
        calorieCalculator.formatDistance(metres)

    fun formatCalories(calories: Double) =
        calorieCalculator.formatCalories(calories)

    fun formatDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return dateStr
            SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) { dateStr }
    }

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, secs)
        } else {
            "%d:%02d".format(minutes, secs)
        }
    }
}