package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.data.local.dao.ActivitySessionDao
import com.example.myhealthtracker.data.local.dao.StepDao
import com.example.myhealthtracker.data.local.dao.WeightDao
import com.example.myhealthtracker.data.local.entity.ActivitySessionEntity
import com.example.myhealthtracker.data.local.entity.StepRecordEntity
import com.example.myhealthtracker.data.local.entity.WeightLogEntity
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class StatsUiState(
    val weeklySteps: List<StepRecordEntity> = emptyList(),
    val monthlySteps: List<StepRecordEntity> = emptyList(),
    val totalStepsEver: Int = 0,
    val totalDistanceEver: Double = 0.0,
    val totalCaloriesEver: Double = 0.0,
    val avgDailySteps: Int = 0,
    val bestDaySteps: Int = 0,
    val bestDayDate: String = "",
    val recentSessions: List<ActivitySessionEntity> = emptyList(),
    val weightLogs: List<WeightLogEntity> = emptyList(),
    val selectedTab: Int = 0,
    val currentStreak: Int = 0,
    val stepGoal: Int = 10_000
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val stepDao: StepDao,
    private val activitySessionDao: ActivitySessionDao,
    private val weightDao: WeightDao,
    private val userProfileDataStore: UserProfileDataStore,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        observeStats()
    }

    private fun observeStats() {
        viewModelScope.launch {
            // Weekly steps
            stepDao.getRecentStepRecords(7).collect { records ->
                val total = records.sumOf { it.steps }
                val avg = if (records.isNotEmpty()) total / records.size else 0
                val best = records.maxByOrNull { it.steps }
                val allTime = records.sumOf { it.steps }
                val allDist = records.sumOf { it.distanceMetres }
                val allCals = records.sumOf { it.caloriesBurned }
                val streak = calculateStreak(records)

                _uiState.update {
                    it.copy(
                        weeklySteps = records.reversed(),
                        totalStepsEver = allTime,
                        totalDistanceEver = allDist,
                        totalCaloriesEver = allCals,
                        avgDailySteps = avg,
                        bestDaySteps = best?.steps ?: 0,
                        bestDayDate = best?.date ?: "",
                        currentStreak = streak
                    )
                }
            }
        }

        viewModelScope.launch {
            // Monthly steps
            val cal = Calendar.getInstance()
            val to = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -29)
            val from = dateFormat.format(cal.time)
            stepDao.getStepRecordsBetween(from, to).collect { records ->
                _uiState.update { it.copy(monthlySteps = records) }
            }
        }

        viewModelScope.launch {
            activitySessionDao.getRecentSessions(10).collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }

        viewModelScope.launch {
            weightDao.getAllWeightLogs().collect { logs ->
                _uiState.update { it.copy(weightLogs = logs.take(10).reversed()) }
            }
        }

        viewModelScope.launch {
            userProfileDataStore.userProfile.collect { profile ->
                _uiState.update { it.copy(stepGoal = profile.dailyStepGoal) }
            }
        }
    }

    private fun calculateStreak(records: List<StepRecordEntity>): Int {
        if (records.isEmpty()) return 0
        var streak = 0
        val goal = _uiState.value.stepGoal
        val sorted = records.sortedByDescending { it.date }
        for (record in sorted) {
            if (record.steps >= goal) streak++
            else break
        }
        return streak
    }

    fun selectTab(index: Int) =
        _uiState.update { it.copy(selectedTab = index) }

    fun formatDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return dateStr
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        } catch (e: Exception) { dateStr }
    }

    fun getDayLabel(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return ""
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        } catch (e: Exception) { "" }
    }

    fun formatDistance(metres: Double) =
        calorieCalculator.formatDistance(metres)

    fun formatCalories(calories: Double) =
        calorieCalculator.formatCalories(calories)
}