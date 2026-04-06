package com.example.myhealthtracker.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class CalorieCalculator @Inject constructor() {

    fun estimateStrideLengthMetres(
        heightCm: Float,
        activityType: String = "WALK",
        strideMultiplier: Float = 1.0f
    ): Double {
        val base = when (activityType.uppercase()) {
            "RUN", "JOG" -> heightCm * 0.46
            "CYCLE" -> 0.0
            "WALK", "HIKE" -> heightCm * 0.414
            else -> heightCm * 0.414
        }
        return (base / 100.0) * strideMultiplier
    }

    fun calculateCaloriesFromSteps(
        steps: Int,
        weightKg: Float,
        heightCm: Float,
        activityType: String = "WALK",
        strideMultiplier: Float = 1.0f
    ): Double {
        if (steps <= 0 || weightKg <= 0f) return 0.0
        val strideM = estimateStrideLengthMetres(heightCm, activityType, strideMultiplier)
        val metFactor = when (activityType.uppercase()) {
            "RUN", "JOG" -> 0.00114
            "HIKE" -> 0.00086
            else -> 0.00057
        }
        return steps * strideM * weightKg * metFactor
    }

    fun calculateCaloriesFromDuration(
        durationMinutes: Double,
        weightKg: Float,
        activityType: String
    ): Double {
        if (durationMinutes <= 0 || weightKg <= 0f) return 0.0
        val met = when (activityType.uppercase()) {
            "CYCLE" -> 6.8
            "SWIM" -> 7.0
            "RUN" -> 9.8
            "JOG" -> 7.0
            "WALK" -> 3.5
            "HIKE" -> 5.3
            "YOGA" -> 2.5
            else -> 4.0
        }
        return met * weightKg * (durationMinutes / 60.0)
    }

    fun calculateDistanceMetres(
        steps: Int,
        heightCm: Float,
        activityType: String = "WALK",
        strideMultiplier: Float = 1.0f
    ): Double {
        if (steps <= 0 || heightCm <= 0f) return 0.0
        return steps * estimateStrideLengthMetres(heightCm, activityType, strideMultiplier)
    }

    fun calculateBMI(weightKg: Float, heightCm: Float): Double {
        if (weightKg <= 0f || heightCm <= 0f) return 0.0
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }

    fun getBMICategory(bmi: Double): String = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Healthy"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }

    fun calculateBMR(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        sex: String
    ): Double {
        return when (sex.uppercase()) {
            "MALE" -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
            "FEMALE" -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
            else -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 78
        }
    }

    fun formatSteps(steps: Int): String = "%,d".format(steps)

    fun formatDistance(metres: Double, useImperial: Boolean = false): String {
        return if (useImperial) {
            val miles = metres / 1609.34
            if (miles < 0.1) "${(metres * 3.28084).roundToInt()} ft"
            else "%.2f mi".format(miles)
        } else {
            if (metres < 1000) "${metres.roundToInt()} m"
            else "%.2f km".format(metres / 1000.0)
        }
    }

    fun formatCalories(calories: Double): String = "%.0f kcal".format(calories)

    fun formatPace(avgSpeedKmh: Double): String {
        if (avgSpeedKmh <= 0) return "--:--"
        val secondsPerKm = 3600.0 / avgSpeedKmh
        val minutes = (secondsPerKm / 60).toInt()
        val seconds = (secondsPerKm % 60).toInt()
        return "%d:%02d /km".format(minutes, seconds)
    }
}