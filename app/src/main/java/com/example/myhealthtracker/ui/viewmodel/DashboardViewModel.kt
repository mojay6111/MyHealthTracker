package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.data.local.entity.StepRecordEntity
import com.example.myhealthtracker.data.repository.StepRepository
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val steps: Int = 0,
    val stepGoal: Int = 10_000,
    val calories: Double = 0.0,
    val distanceMetres: Double = 0.0,
    val waterMl: Int = 0,
    val waterGoalMl: Int = 2_500,
    val userName: String = "",
    val weeklyRecords: List<StepRecordEntity> = emptyList(),
    val progressFraction: Float = 0f,
    val greeting: String = "Good Morning"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val stepRepository: StepRepository,
    private val userProfileDataStore: UserProfileDataStore,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeTodaySteps()
        observeUserProfile()
        observeWeeklySteps()
        updateGreeting()
    }

    private fun observeTodaySteps() {
        viewModelScope.launch {
            stepRepository.getTodayStepRecord().collect { record ->
                _uiState.update { state ->
                    val steps = record?.steps ?: 0
                    val goal = record?.goalSteps ?: state.stepGoal
                    state.copy(
                        steps = steps,
                        calories = record?.caloriesBurned ?: 0.0,
                        distanceMetres = record?.distanceMetres ?: 0.0,
                        progressFraction = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            userProfileDataStore.userProfile.collect { profile ->
                _uiState.update { state ->
                    state.copy(
                        userName = profile.name,
                        stepGoal = profile.dailyStepGoal,
                        waterGoalMl = profile.dailyWaterGoalMl
                    )
                }
            }
        }
    }

    private fun observeWeeklySteps() {
        viewModelScope.launch {
            stepRepository.getWeeklyRecords().collect { records ->
                _uiState.update { it.copy(weeklyRecords = records) }
            }
        }
    }

    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        _uiState.update { it.copy(greeting = greeting) }
    }

    fun formatSteps(steps: Int): String = calorieCalculator.formatSteps(steps)

    fun formatDistance(metres: Double): String = calorieCalculator.formatDistance(metres)

    fun formatCalories(calories: Double): String = calorieCalculator.formatCalories(calories)

    fun getDayLabel(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return ""
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            ""
        }
    }
}