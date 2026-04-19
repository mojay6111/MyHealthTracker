package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.local.dao.AchievementDao
import com.example.myhealthtracker.data.local.dao.StepDao
import com.example.myhealthtracker.data.local.entity.AchievementEntity
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementsUiState(
    val allAchievements: List<AchievementEntity> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val recentlyUnlocked: List<AchievementEntity> = emptyList(),
    val stepAchievements: List<AchievementEntity> = emptyList(),
    val streakAchievements: List<AchievementEntity> = emptyList(),
    val distanceAchievements: List<AchievementEntity> = emptyList(),
    val hydrationAchievements: List<AchievementEntity> = emptyList(),
    val currentStreak: Int = 0
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementDao: AchievementDao,
    private val stepDao: StepDao,
    private val userProfileDataStore: UserProfileDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        observeAchievements()
        observeStreak()
    }

    private fun observeAchievements() {
        viewModelScope.launch {
            achievementDao.getAllAchievements().collect { achievements ->
                val unlocked = achievements.filter { it.unlockedAt != null }
                val recent = unlocked
                    .sortedByDescending { it.unlockedAt }
                    .take(3)

                _uiState.update {
                    it.copy(
                        allAchievements = achievements,
                        unlockedCount = unlocked.size,
                        totalCount = achievements.size,
                        recentlyUnlocked = recent,
                        stepAchievements = achievements.filter {
                                a -> a.category == "STEPS"
                        },
                        streakAchievements = achievements.filter {
                                a -> a.category == "STREAK"
                        },
                        distanceAchievements = achievements.filter {
                                a -> a.category == "DISTANCE"
                        },
                        hydrationAchievements = achievements.filter {
                                a -> a.category == "HYDRATION"
                        }
                    )
                }
            }
        }
    }

    private fun observeStreak() {
        viewModelScope.launch {
            combine(
                stepDao.getRecentStepRecords(30),
                userProfileDataStore.userProfile
            ) { records, profile ->
                var streak = 0
                val sorted = records.sortedByDescending { it.date }
                for (record in sorted) {
                    if (record.steps >= profile.dailyStepGoal) streak++
                    else break
                }
                streak
            }.collect { streak ->
                _uiState.update { it.copy(currentStreak = streak) }
            }
        }
    }
}