package com.example.myhealthtracker.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val sex: String = "PREFER_NOT_TO_SAY",
    val dailyStepGoal: Int = 10_000,
    val dailyWaterGoalMl: Int = 2_500,
    val dailyCalorieGoal: Int = 2_000,
    val unitSystem: String = "METRIC",
    val themeMode: String = "DARK",
    val accentColor: String = "TEAL",
    val onboardingComplete: Boolean = false,
    val strideMultiplier: Float = 1.0f,
    val notificationsEnabled: Boolean = true,
    val sedentaryAlertEnabled: Boolean = true,
    val sedentaryAlertIntervalMinutes: Int = 90,
    val hydrationRemindersEnabled: Boolean = true
)

@Singleton
class UserProfileDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val NAME = stringPreferencesKey("name")
        val AGE = intPreferencesKey("age")
        val HEIGHT_CM = floatPreferencesKey("height_cm")
        val WEIGHT_KG = floatPreferencesKey("weight_kg")
        val SEX = stringPreferencesKey("sex")
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        val DAILY_WATER_GOAL_ML = intPreferencesKey("daily_water_goal_ml")
        val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        val UNIT_SYSTEM = stringPreferencesKey("unit_system")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val STRIDE_MULTIPLIER = floatPreferencesKey("stride_multiplier")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SEDENTARY_ALERT_ENABLED = booleanPreferencesKey("sedentary_alert_enabled")
        val SEDENTARY_ALERT_INTERVAL = intPreferencesKey("sedentary_alert_interval_minutes")
        val HYDRATION_REMINDERS_ENABLED = booleanPreferencesKey("hydration_reminders_enabled")
    }

    val userProfile: Flow<UserProfile> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            UserProfile(
                name = prefs[Keys.NAME] ?: "",
                age = prefs[Keys.AGE] ?: 0,
                heightCm = prefs[Keys.HEIGHT_CM] ?: 0f,
                weightKg = prefs[Keys.WEIGHT_KG] ?: 0f,
                sex = prefs[Keys.SEX] ?: "PREFER_NOT_TO_SAY",
                dailyStepGoal = prefs[Keys.DAILY_STEP_GOAL] ?: 10_000,
                dailyWaterGoalMl = prefs[Keys.DAILY_WATER_GOAL_ML] ?: 2_500,
                dailyCalorieGoal = prefs[Keys.DAILY_CALORIE_GOAL] ?: 2_000,
                unitSystem = prefs[Keys.UNIT_SYSTEM] ?: "METRIC",
                themeMode = prefs[Keys.THEME_MODE] ?: "DARK",
                accentColor = prefs[Keys.ACCENT_COLOR] ?: "TEAL",
                onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false,
                strideMultiplier = prefs[Keys.STRIDE_MULTIPLIER] ?: 1.0f,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                sedentaryAlertEnabled = prefs[Keys.SEDENTARY_ALERT_ENABLED] ?: true,
                sedentaryAlertIntervalMinutes = prefs[Keys.SEDENTARY_ALERT_INTERVAL] ?: 90,
                hydrationRemindersEnabled = prefs[Keys.HYDRATION_REMINDERS_ENABLED] ?: true
            )
        }

    suspend fun updateProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NAME] = profile.name
            prefs[Keys.AGE] = profile.age
            prefs[Keys.HEIGHT_CM] = profile.heightCm
            prefs[Keys.WEIGHT_KG] = profile.weightKg
            prefs[Keys.SEX] = profile.sex
            prefs[Keys.DAILY_STEP_GOAL] = profile.dailyStepGoal
            prefs[Keys.DAILY_WATER_GOAL_ML] = profile.dailyWaterGoalMl
            prefs[Keys.DAILY_CALORIE_GOAL] = profile.dailyCalorieGoal
            prefs[Keys.UNIT_SYSTEM] = profile.unitSystem
            prefs[Keys.THEME_MODE] = profile.themeMode
            prefs[Keys.ACCENT_COLOR] = profile.accentColor
            prefs[Keys.ONBOARDING_COMPLETE] = profile.onboardingComplete
            prefs[Keys.STRIDE_MULTIPLIER] = profile.strideMultiplier
            prefs[Keys.NOTIFICATIONS_ENABLED] = profile.notificationsEnabled
            prefs[Keys.SEDENTARY_ALERT_ENABLED] = profile.sedentaryAlertEnabled
            prefs[Keys.SEDENTARY_ALERT_INTERVAL] = profile.sedentaryAlertIntervalMinutes
            prefs[Keys.HYDRATION_REMINDERS_ENABLED] = profile.hydrationRemindersEnabled
        }
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETE] = true
        }
    }

    suspend fun updateStepGoal(goal: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_STEP_GOAL] = goal
        }
    }

    suspend fun updateTheme(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }
}