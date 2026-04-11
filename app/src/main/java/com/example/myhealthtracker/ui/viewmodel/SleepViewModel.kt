package com.example.myhealthtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.local.dao.SleepDao
import com.example.myhealthtracker.data.local.entity.SleepLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SleepUiState(
    val logs: List<SleepLogEntity> = emptyList(),
    val bedTime: String = "22:00",
    val wakeTime: String = "06:00",
    val qualityRating: Int = 3,
    val notes: String = "",
    val savedSuccess: Boolean = false,
    val avgDuration: Int = 0,
    val avgQuality: Float = 0f
)

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val sleepDao: SleepDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    init {
        observeSleepLogs()
    }

    private fun observeSleepLogs() {
        viewModelScope.launch {
            sleepDao.getRecentSleepLogs(14).collect { logs ->
                val avgDuration = if (logs.isNotEmpty())
                    logs.sumOf { it.durationMinutes } / logs.size
                else 0
                val avgQuality = if (logs.isNotEmpty())
                    logs.sumOf { it.qualityRating }.toFloat() / logs.size
                else 0f
                _uiState.update {
                    it.copy(
                        logs = logs,
                        avgDuration = avgDuration,
                        avgQuality = avgQuality
                    )
                }
            }
        }
    }

    fun updateBedTime(value: String) =
        _uiState.update { it.copy(bedTime = value) }

    fun updateWakeTime(value: String) =
        _uiState.update { it.copy(wakeTime = value) }

    fun updateQuality(value: Int) =
        _uiState.update { it.copy(qualityRating = value) }

    fun updateNotes(value: String) =
        _uiState.update { it.copy(notes = value) }

    fun logSleep() {
        viewModelScope.launch {
            val state = _uiState.value
            val today = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())

            // Parse bed and wake times
            val bedMillis = parseTimeToMillis(state.bedTime)
            val wakeMillis = parseTimeToMillis(state.wakeTime)

            // If wake time is before bed time add a day
            val adjustedWake = if (wakeMillis <= bedMillis)
                wakeMillis + 24 * 60 * 60 * 1000
            else wakeMillis

            val durationMinutes = ((adjustedWake - bedMillis) / 60000).toInt()

            val log = SleepLogEntity(
                date = today,
                sleepStart = bedMillis,
                sleepEnd = adjustedWake,
                durationMinutes = durationMinutes,
                qualityRating = state.qualityRating,
                notes = state.notes.ifEmpty { null }
            )
            sleepDao.insertSleepLog(log)
            _uiState.update {
                it.copy(
                    savedSuccess = true,
                    notes = ""
                )
            }
            kotlinx.coroutines.delay(3000)
            _uiState.update { it.copy(savedSuccess = false) }
        }
    }

    fun deleteLog(log: SleepLogEntity) {
        viewModelScope.launch {
            sleepDao.deleteSleepLog(log)
        }
    }

    private fun parseTimeToMillis(time: String): Long {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.parse(time)?.time ?: 0L
        } catch (e: Exception) { 0L }
    }

    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours}h ${mins}m"
    }

    fun formatDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return dateStr
            SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(date)
        } catch (e: Exception) { dateStr }
    }

    fun getQualityLabel(rating: Int): String = when (rating) {
        1 -> "Terrible 😴"
        2 -> "Poor 😪"
        3 -> "Okay 🙂"
        4 -> "Good 😊"
        5 -> "Great! 🌟"
        else -> "Okay"
    }

    fun getQualityColor(rating: Int) = when (rating) {
        1 -> 0xFFFF3333
        2 -> 0xFFFF6B6B
        3 -> 0xFFFFBB57
        4 -> 0xFF4CAF50
        5 -> 0xFF00E5CC
        else -> 0xFFFFBB57
    }
}