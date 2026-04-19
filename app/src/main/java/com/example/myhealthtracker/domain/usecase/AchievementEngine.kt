package com.example.myhealthtracker.domain.usecase

import com.example.myhealthtracker.data.local.dao.AchievementDao
import com.example.myhealthtracker.data.local.dao.ActivitySessionDao
import com.example.myhealthtracker.data.local.dao.StepDao
import com.example.myhealthtracker.data.local.dao.WaterDao
import com.example.myhealthtracker.data.local.entity.AchievementEntity
import com.example.myhealthtracker.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementEngine @Inject constructor(
    private val achievementDao: AchievementDao,
    private val stepDao: StepDao,
    private val activitySessionDao: ActivitySessionDao,
    private val waterDao: WaterDao,
    private val notificationHelper: NotificationHelper
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private fun today() = dateFormat.format(Date())

    suspend fun seedAchievements() {
        val achievements = buildAchievementList()
        achievementDao.insertAllAchievements(achievements)
    }

    suspend fun checkAndUnlock(stepGoal: Int) {
        val today = today()
        val todayRecord = stepDao.getStepRecord(today).first()
        val todaySteps = todayRecord?.steps ?: 0
        val allSessions = activitySessionDao.getAllSessions().first()
        val recentRecords = stepDao.getRecentStepRecords(30).first()
        val totalSteps = recentRecords.sumOf { it.steps }
        val totalDistance = allSessions.sumOf { it.distanceMetres }
        val totalWater = waterDao.getTotalWaterForDate(today).first() ?: 0
        val streak = calculateStreak(recentRecords, stepGoal)
        val maxSpeed = allSessions.maxOfOrNull { it.maxSpeedKmh } ?: 0.0
        val longestRun = allSessions
            .filter { it.activityType == "RUN" }
            .maxOfOrNull { it.distanceMetres } ?: 0.0

        // Steps
        if (todaySteps >= 1_000) unlockIfNew("FIRST_1K_STEPS")
        if (todaySteps >= 5_000) unlockIfNew("FIRST_5K_STEPS")
        if (todaySteps >= 10_000) unlockIfNew("FIRST_10K_STEPS")
        if (todaySteps >= 20_000) unlockIfNew("FIRST_20K_STEPS")
        if (totalSteps >= 100_000) unlockIfNew("TOTAL_100K_STEPS")
        if (totalSteps >= 1_000_000) unlockIfNew("TOTAL_1M_STEPS")

        // Streaks
        if (streak >= 3) unlockIfNew("STREAK_3_DAYS")
        if (streak >= 7) unlockIfNew("STREAK_7_DAYS")
        if (streak >= 30) unlockIfNew("STREAK_30_DAYS")

        // Distance
        if (allSessions.isNotEmpty()) unlockIfNew("FIRST_ROUTE")
        if (longestRun >= 5_000) unlockIfNew("FIRST_5KM_RUN")
        if (longestRun >= 10_000) unlockIfNew("FIRST_10KM_RUN")
        if (totalDistance >= 100_000) unlockIfNew("TOTAL_100KM")

        // Speed
        if (maxSpeed >= 12.0) unlockIfNew("SPEED_12KMH")

        // Hydration
        if (totalWater >= 2500) unlockIfNew("FIRST_WATER_GOAL")
    }

    suspend fun checkHistoricalData() {
        val allRecords = stepDao.getRecentStepRecords(365).first()
        val bestDaySteps = allRecords.maxOfOrNull { it.steps } ?: 0
        val totalSteps = allRecords.sumOf { it.steps }
        val allSessions = activitySessionDao.getAllSessions().first()
        val totalDistance = allSessions.sumOf { it.distanceMetres }
        val maxSpeed = allSessions.maxOfOrNull { it.maxSpeedKmh } ?: 0.0
        val longestRun = allSessions
            .filter { it.activityType == "RUN" }
            .maxOfOrNull { it.distanceMetres } ?: 0.0

        // Check all historical step achievements
        if (bestDaySteps >= 1_000) unlockIfNew("FIRST_1K_STEPS")
        if (bestDaySteps >= 5_000) unlockIfNew("FIRST_5K_STEPS")
        if (bestDaySteps >= 10_000) unlockIfNew("FIRST_10K_STEPS")
        if (bestDaySteps >= 20_000) unlockIfNew("FIRST_20K_STEPS")
        if (totalSteps >= 100_000) unlockIfNew("TOTAL_100K_STEPS")
        if (totalSteps >= 1_000_000) unlockIfNew("TOTAL_1M_STEPS")

        // Check streak
        val streak = calculateStreak(allRecords, 10_000)
        if (streak >= 3) unlockIfNew("STREAK_3_DAYS")
        if (streak >= 7) unlockIfNew("STREAK_7_DAYS")
        if (streak >= 30) unlockIfNew("STREAK_30_DAYS")

        // Check distance
        if (allSessions.isNotEmpty()) unlockIfNew("FIRST_ROUTE")
        if (longestRun >= 5_000) unlockIfNew("FIRST_5KM_RUN")
        if (longestRun >= 10_000) unlockIfNew("FIRST_10KM_RUN")
        if (totalDistance >= 100_000) unlockIfNew("TOTAL_100KM")
        if (maxSpeed >= 12.0) unlockIfNew("SPEED_12KMH")
    }

    private suspend fun unlockIfNew(id: String) {
        val existing = achievementDao.getAchievement(id) ?: return
        if (existing.unlockedAt != null) return
        val unlocked = existing.copy(
            unlockedAt = System.currentTimeMillis(),
            progress = 1f
        )
        achievementDao.upsertAchievement(unlocked)
        notificationHelper.showAchievementUnlocked(
            title = unlocked.title,
            description = unlocked.description
        )
    }

    private fun calculateStreak(
        records: List<com.example.myhealthtracker.data.local.entity.StepRecordEntity>,
        goal: Int
    ): Int {
        var streak = 0
        val sorted = records.sortedByDescending { it.date }
        for (record in sorted) {
            if (record.steps >= goal) streak++ else break
        }
        return streak
    }

    private fun buildAchievementList(): List<AchievementEntity> = listOf(
        AchievementEntity(
            id = "FIRST_1K_STEPS",
            title = "First Steps",
            description = "Take your first 1,000 steps",
            iconRes = "ic_achievement_steps",
            category = "STEPS"
        ),
        AchievementEntity(
            id = "FIRST_5K_STEPS",
            title = "Getting Warmed Up",
            description = "Take 5,000 steps in a day",
            iconRes = "ic_achievement_steps",
            category = "STEPS"
        ),
        AchievementEntity(
            id = "FIRST_10K_STEPS",
            title = "10K Club 🎉",
            description = "Take 10,000 steps in a single day",
            iconRes = "ic_achievement_10k",
            category = "STEPS"
        ),
        AchievementEntity(
            id = "FIRST_20K_STEPS",
            title = "Step Machine",
            description = "Take 20,000 steps in a single day",
            iconRes = "ic_achievement_steps",
            category = "STEPS"
        ),
        AchievementEntity(
            id = "TOTAL_100K_STEPS",
            title = "Century Walker",
            description = "Take 100,000 steps total",
            iconRes = "ic_achievement_steps",
            category = "STEPS"
        ),
        AchievementEntity(
            id = "TOTAL_1M_STEPS",
            title = "Million Stepper 🏆",
            description = "Take 1,000,000 steps total",
            iconRes = "ic_achievement_trophy",
            category = "STEPS"
        ),
        AchievementEntity(
            id = "STREAK_3_DAYS",
            title = "On Fire 🔥",
            description = "Hit your step goal 3 days in a row",
            iconRes = "ic_achievement_streak",
            category = "STREAK"
        ),
        AchievementEntity(
            id = "STREAK_7_DAYS",
            title = "Week Warrior",
            description = "Hit your step goal 7 days in a row",
            iconRes = "ic_achievement_streak",
            category = "STREAK"
        ),
        AchievementEntity(
            id = "STREAK_30_DAYS",
            title = "Unstoppable 💪",
            description = "Hit your step goal 30 days in a row",
            iconRes = "ic_achievement_streak",
            category = "STREAK"
        ),
        AchievementEntity(
            id = "FIRST_ROUTE",
            title = "Explorer",
            description = "Record your first GPS route",
            iconRes = "ic_achievement_map",
            category = "DISTANCE"
        ),
        AchievementEntity(
            id = "FIRST_5KM_RUN",
            title = "5K Runner",
            description = "Complete a 5km run",
            iconRes = "ic_achievement_run",
            category = "DISTANCE"
        ),
        AchievementEntity(
            id = "FIRST_10KM_RUN",
            title = "10K Hero",
            description = "Complete a 10km run",
            iconRes = "ic_achievement_run",
            category = "DISTANCE"
        ),
        AchievementEntity(
            id = "TOTAL_100KM",
            title = "Centurion 🗺️",
            description = "Travel 100km total across all activities",
            iconRes = "ic_achievement_distance",
            category = "DISTANCE"
        ),
        AchievementEntity(
            id = "SPEED_12KMH",
            title = "Speed Demon ⚡",
            description = "Run faster than 12 km/h",
            iconRes = "ic_achievement_speed",
            category = "DISTANCE"
        ),
        AchievementEntity(
            id = "FIRST_WATER_GOAL",
            title = "Hydration Hero 💧",
            description = "Hit your water goal for the first time",
            iconRes = "ic_achievement_water",
            category = "HYDRATION"
        ),
        AchievementEntity(
            id = "WATER_GOAL_7_DAYS",
            title = "H2O Champion",
            description = "Hit your water goal 7 days in a row",
            iconRes = "ic_achievement_water",
            category = "HYDRATION"
        )
    )
}