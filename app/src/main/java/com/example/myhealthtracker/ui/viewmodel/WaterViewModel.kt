package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.data.local.dao.WaterDao
import com.example.myhealthtracker.data.local.entity.WaterLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class WaterUiState(
    val todayLogs: List<WaterLogEntity> = emptyList(),
    val totalMl: Int = 0,
    val goalMl: Int = 2500,
    val progress: Float = 0f,
    val customAmount: String = ""
)

@HiltViewModel
class WaterViewModel @Inject constructor(
    private val waterDao: WaterDao,
    private val userProfileDataStore: UserProfileDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private fun today() = dateFormat.format(Date())

    init {
        observeWaterLogs()
        observeGoal()
    }

    private fun observeWaterLogs() {
        viewModelScope.launch {
            waterDao.getWaterLogsForDate(today()).collect { logs ->
                val total = logs.sumOf { it.amountMl }
                val goal = _uiState.value.goalMl
                _uiState.update {
                    it.copy(
                        todayLogs = logs,
                        totalMl = total,
                        progress = (total.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    private fun observeGoal() {
        viewModelScope.launch {
            userProfileDataStore.userProfile.collect { profile ->
                val goal = profile.dailyWaterGoalMl
                val total = _uiState.value.totalMl
                _uiState.update {
                    it.copy(
                        goalMl = goal,
                        progress = (total.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
                    )
                }
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val log = WaterLogEntity(
                date = today(),
                amountMl = amountMl
            )
            waterDao.insertWaterLog(log)
        }
    }

    fun deleteLog(log: WaterLogEntity) {
        viewModelScope.launch {
            waterDao.deleteWaterLog(log)
        }
    }

    fun updateCustomAmount(value: String) =
        _uiState.update { it.copy(customAmount = value) }

    fun addCustomAmount() {
        val amount = _uiState.value.customAmount.toIntOrNull() ?: return
        if (amount > 0) {
            addWater(amount)
            _uiState.update { it.copy(customAmount = "") }
        }
    }
}