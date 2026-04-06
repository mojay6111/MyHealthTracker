package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.datastore.UserProfile
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val age: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val sex: String = "MALE",
    val stepGoal: String = "10000",
    val waterGoalMl: String = "2500"
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileDataStore: UserProfileDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        _uiState.update {
            it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3))
        }
    }

    fun previousStep() {
        _uiState.update {
            it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0))
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }

    fun updateAge(age: String) = _uiState.update { it.copy(age = age) }

    fun updateHeight(height: String) = _uiState.update { it.copy(heightCm = height) }

    fun updateWeight(weight: String) = _uiState.update { it.copy(weightKg = weight) }

    fun updateSex(sex: String) = _uiState.update { it.copy(sex = sex) }

    fun updateStepGoal(goal: String) = _uiState.update { it.copy(stepGoal = goal) }

    fun updateWaterGoal(goal: String) = _uiState.update { it.copy(waterGoalMl = goal) }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val profile = UserProfile(
                name = state.name.trim(),
                age = state.age.toIntOrNull() ?: 0,
                heightCm = state.heightCm.toFloatOrNull() ?: 0f,
                weightKg = state.weightKg.toFloatOrNull() ?: 0f,
                sex = state.sex,
                dailyStepGoal = state.stepGoal.toIntOrNull() ?: 10_000,
                dailyWaterGoalMl = state.waterGoalMl.toIntOrNull() ?: 2_500,
                onboardingComplete = true
            )
            userProfileDataStore.updateProfile(profile)
            onComplete()
        }
    }
}