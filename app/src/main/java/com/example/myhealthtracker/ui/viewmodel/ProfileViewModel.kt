package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val age: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val sex: String = "MALE",
    val stepGoal: String = "10000",
    val waterGoalMl: String = "2500",
    val unitSystem: String = "METRIC",
    val notificationsEnabled: Boolean = true,
    val sedentaryAlertEnabled: Boolean = true,
    val hydrationReminders: Boolean = true,
    val bmi: Double = 0.0,
    val bmiCategory: String = "",
    val savedSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileDataStore: UserProfileDataStore,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userProfileDataStore.userProfile.collect { profile ->
                val bmi = calorieCalculator.calculateBMI(
                    profile.weightKg,
                    profile.heightCm
                )
                _uiState.update {
                    it.copy(
                        name = profile.name,
                        age = if (profile.age > 0) profile.age.toString() else "",
                        heightCm = if (profile.heightCm > 0) profile.heightCm.toString() else "",
                        weightKg = if (profile.weightKg > 0) profile.weightKg.toString() else "",
                        sex = profile.sex,
                        stepGoal = profile.dailyStepGoal.toString(),
                        waterGoalMl = profile.dailyWaterGoalMl.toString(),
                        unitSystem = profile.unitSystem,
                        notificationsEnabled = profile.notificationsEnabled,
                        sedentaryAlertEnabled = profile.sedentaryAlertEnabled,
                        hydrationReminders = profile.hydrationRemindersEnabled,
                        bmi = bmi,
                        bmiCategory = calorieCalculator.getBMICategory(bmi)
                    )
                }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateAge(value: String) = _uiState.update { it.copy(age = value) }
    fun updateHeight(value: String) {
        _uiState.update { it.copy(heightCm = value) }
        recalculateBMI()
    }
    fun updateWeight(value: String) {
        _uiState.update { it.copy(weightKg = value) }
        recalculateBMI()
    }
    fun updateSex(value: String) = _uiState.update { it.copy(sex = value) }
    fun updateStepGoal(value: String) = _uiState.update { it.copy(stepGoal = value) }
    fun updateWaterGoal(value: String) = _uiState.update { it.copy(waterGoalMl = value) }
    fun updateUnitSystem(value: String) = _uiState.update { it.copy(unitSystem = value) }
    fun updateNotifications(value: Boolean) =
        _uiState.update { it.copy(notificationsEnabled = value) }
    fun updateSedentaryAlert(value: Boolean) =
        _uiState.update { it.copy(sedentaryAlertEnabled = value) }
    fun updateHydrationReminders(value: Boolean) =
        _uiState.update { it.copy(hydrationReminders = value) }

    private fun recalculateBMI() {
        val state = _uiState.value
        val weight = state.weightKg.toFloatOrNull() ?: return
        val height = state.heightCm.toFloatOrNull() ?: return
        val bmi = calorieCalculator.calculateBMI(weight, height)
        _uiState.update {
            it.copy(
                bmi = bmi,
                bmiCategory = calorieCalculator.getBMICategory(bmi)
            )
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentProfile = userProfileDataStore.userProfile.first()
            val updated = currentProfile.copy(
                name = state.name.trim(),
                age = state.age.toIntOrNull() ?: 0,
                heightCm = state.heightCm.toFloatOrNull() ?: 0f,
                weightKg = state.weightKg.toFloatOrNull() ?: 0f,
                sex = state.sex,
                dailyStepGoal = state.stepGoal.toIntOrNull() ?: 10_000,
                dailyWaterGoalMl = state.waterGoalMl.toIntOrNull() ?: 2_500,
                unitSystem = state.unitSystem,
                notificationsEnabled = state.notificationsEnabled,
                sedentaryAlertEnabled = state.sedentaryAlertEnabled,
                hydrationRemindersEnabled = state.hydrationReminders
            )
            userProfileDataStore.updateProfile(updated)
            _uiState.update { it.copy(savedSuccess = true) }
            // Hide success message after 3 seconds
            delay(3000)
            _uiState.update { it.copy(savedSuccess = false) }
        }
    }
}