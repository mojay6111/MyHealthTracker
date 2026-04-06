package com.example.myhealthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_records")
data class StepRecordEntity(
    @PrimaryKey
    val date: String,
    val steps: Int = 0,
    val caloriesBurned: Double = 0.0,
    val distanceMetres: Double = 0.0,
    val activeMinutes: Int = 0,
    val goalSteps: Int = 10_000,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val weightKg: Double,
    val bmi: Double? = null,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_sessions")
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val activityType: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val durationSeconds: Long,
    val distanceMetres: Double,
    val steps: Int = 0,
    val caloriesBurned: Double,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val avgPaceMinPerKm: Double = 0.0,
    val routePointsJson: String? = null,
    val mapSnapshotUrl: String? = null,
    val elevationGainMetres: Double = 0.0,
    val notes: String? = null
)

@Entity(tableName = "personal_records")
data class PersonalRecordEntity(
    @PrimaryKey
    val category: String,
    val value: Double,
    val unit: String,
    val achievedDate: String,
    val sessionId: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconRes: String,
    val unlockedAt: Long? = null,
    val progress: Float = 0f,
    val category: String
)

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val sleepStart: Long,
    val sleepEnd: Long,
    val durationMinutes: Int,
    val qualityRating: Int,
    val notes: String? = null
)