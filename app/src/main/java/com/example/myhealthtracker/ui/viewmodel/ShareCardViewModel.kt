package com.example.myhealthtracker.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.data.local.dao.ActivitySessionDao
import com.example.myhealthtracker.data.local.dao.StepDao
import com.example.myhealthtracker.data.local.entity.ActivitySessionEntity
import com.example.myhealthtracker.data.local.entity.StepRecordEntity
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ShareCardUiState(
    val userName: String = "",
    val steps: Int = 0,
    val calories: Double = 0.0,
    val distanceMetres: Double = 0.0,
    val stepGoal: Int = 10_000,
    val progressFraction: Float = 0f,
    val formattedSteps: String = "0",
    val formattedCalories: String = "0 kcal",
    val formattedDistance: String = "0 m",
    val todayDate: String = "",
    val latestSession: ActivitySessionEntity? = null,
    val isSharing: Boolean = false
)

@HiltViewModel
class ShareCardViewModel @Inject constructor(
    application: Application,
    private val stepDao: StepDao,
    private val activitySessionDao: ActivitySessionDao,
    private val userProfileDataStore: UserProfileDataStore,
    private val calorieCalculator: CalorieCalculator
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ShareCardUiState())
    val uiState: StateFlow<ShareCardUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    init {
        loadData()
    }

    private fun loadData() {
        val today = dateFormat.format(Date())
        val displayDate = displayDateFormat.format(Date())

        viewModelScope.launch {
            userProfileDataStore.userProfile.collect { profile ->
                _uiState.update {
                    it.copy(
                        userName = profile.name,
                        stepGoal = profile.dailyStepGoal,
                        todayDate = displayDate
                    )
                }
            }
        }

        viewModelScope.launch {
            stepDao.getStepRecord(today).collect { record ->
                val steps = record?.steps ?: 0
                val goal = _uiState.value.stepGoal
                _uiState.update {
                    it.copy(
                        steps = steps,
                        calories = record?.caloriesBurned ?: 0.0,
                        distanceMetres = record?.distanceMetres ?: 0.0,
                        progressFraction = (steps.toFloat() / goal).coerceIn(0f, 1f),
                        formattedSteps = calorieCalculator.formatSteps(steps),
                        formattedCalories = calorieCalculator.formatCalories(
                            record?.caloriesBurned ?: 0.0
                        ),
                        formattedDistance = calorieCalculator.formatDistance(
                            record?.distanceMetres ?: 0.0
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            activitySessionDao.getRecentSessions(1).collect { sessions ->
                _uiState.update { it.copy(latestSession = sessions.firstOrNull()) }
            }
        }
    }

    fun shareCard(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true) }
            try {
                val context = getApplication<Application>()
                val file = File(
                    context.cacheDir,
                    "fittrack_share_${System.currentTimeMillis()}.png"
                )
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out my fitness progress with FitTrack! 💪🏃"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share your progress")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e: Exception) {
                // handle error
            } finally {
                _uiState.update { it.copy(isSharing = false) }
            }
        }
    }
}