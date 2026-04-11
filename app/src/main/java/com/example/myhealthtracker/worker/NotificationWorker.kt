package com.example.myhealthtracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.data.local.dao.StepDao
import com.example.myhealthtracker.data.local.dao.WaterDao
import com.example.myhealthtracker.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class HydrationReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val waterDao: WaterDao,
    private val userProfileDataStore: UserProfileDataStore,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val profile = userProfileDataStore.userProfile.first()
            if (!profile.hydrationRemindersEnabled) return Result.success()

            val today = SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
            ).format(Date())
            val totalMl = waterDao.getTotalWaterForDate(today).first() ?: 0
            val goalMl = profile.dailyWaterGoalMl

            // Only remind if under 80% of goal
            if (totalMl < goalMl * 0.8f) {
                notificationHelper.showHydrationReminder(totalMl, goalMl)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

@HiltWorker
class SedentaryAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val stepDao: StepDao,
    private val userProfileDataStore: UserProfileDataStore,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val profile = userProfileDataStore.userProfile.first()
            if (!profile.sedentaryAlertEnabled) return Result.success()

            val today = SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
            ).format(Date())
            val record = stepDao.getStepRecord(today).first()
            val intervalMinutes = profile.sedentaryAlertIntervalMinutes

            // Check if steps haven't changed recently
            val lastUpdated = record?.updatedAt ?: 0L
            val minutesSinceUpdate = (System.currentTimeMillis() - lastUpdated) / 60000

            if (minutesSinceUpdate >= intervalMinutes) {
                notificationHelper.showSedentaryAlert(minutesSinceUpdate.toInt())
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

@HiltWorker
class StepGoalNudgeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val stepDao: StepDao,
    private val userProfileDataStore: UserProfileDataStore,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val profile = userProfileDataStore.userProfile.first()
            if (!profile.notificationsEnabled) return Result.success()

            val today = SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
            ).format(Date())
            val record = stepDao.getStepRecord(today).first()
            val currentSteps = record?.steps ?: 0
            val goalSteps = profile.dailyStepGoal

            when {
                // Goal achieved
                currentSteps >= goalSteps -> {
                    notificationHelper.showStepGoalAchieved(currentSteps)
                }
                // Between 70-99% of goal — nudge
                currentSteps >= goalSteps * 0.7f -> {
                    notificationHelper.showStepGoalNudge(currentSteps, goalSteps)
                }
                // Under 70% — encourage
                else -> {
                    notificationHelper.showStepGoalNudge(currentSteps, goalSteps)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}