package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.data.local.dao.WeightDao
import com.example.myhealthtracker.data.local.entity.WeightLogEntity
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class WeightUiState(
    val logs: List<WeightLogEntity> = emptyList(),
    val latestWeight: Double = 0.0,
    val latestBmi: Double = 0.0,
    val bmiCategory: String = "",
    val heightCm: Float = 0f,
    val newWeight: String = "",
    val newNote: String = "",
    val savedSuccess: Boolean = false,
    val weightChange: Double = 0.0,
    val isGaining: Boolean = false
)

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val weightDao: WeightDao,
    private val userProfileDataStore: UserProfileDataStore,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private fun today() = dateFormat.format(Date())

    init {
        observeWeightLogs()
        observeProfile()
    }

    private fun observeWeightLogs() {
        viewModelScope.launch {
            weightDao.getAllWeightLogs().collect { logs ->
                val latest = logs.firstOrNull()?.weightKg ?: 0.0
                val previous = logs.getOrNull(1)?.weightKg ?: latest
                val change = latest - previous
                val height = _uiState.value.heightCm
                val bmi = if (height > 0)
                    calorieCalculator.calculateBMI(latest.toFloat(), height)
                else 0.0

                _uiState.update {
                    it.copy(
                        logs = logs,
                        latestWeight = latest,
                        latestBmi = bmi,
                        bmiCategory = calorieCalculator.getBMICategory(bmi),
                        weightChange = change,
                        isGaining = change >= 0
                    )
                }
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            userProfileDataStore.userProfile.collect { profile ->
                _uiState.update { it.copy(heightCm = profile.heightCm) }
            }
        }
    }

    fun updateNewWeight(value: String) =
        _uiState.update { it.copy(newWeight = value) }

    fun updateNewNote(value: String) =
        _uiState.update { it.copy(newNote = value) }

    fun logWeight() {
        val weight = _uiState.value.newWeight.toDoubleOrNull() ?: return
        if (weight <= 0) return

        viewModelScope.launch {
            val height = _uiState.value.heightCm
            val bmi = if (height > 0)
                calorieCalculator.calculateBMI(weight.toFloat(), height)
            else null

            val log = WeightLogEntity(
                date = today(),
                weightKg = weight,
                bmi = bmi,
                note = _uiState.value.newNote.ifEmpty { null }
            )
            weightDao.insertWeightLog(log)
            _uiState.update {
                it.copy(
                    newWeight = "",
                    newNote = "",
                    savedSuccess = true
                )
            }
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(savedSuccess = false) }
        }
    }

    fun deleteLog(log: WeightLogEntity) {
        viewModelScope.launch {
            weightDao.deleteWeightLog(log)
        }
    }

    fun formatDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return dateStr
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            dateStr
        }
    }
}