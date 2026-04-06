package com.example.myhealthtracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.myhealthtracker.MainActivity
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.example.myhealthtracker.data.local.dao.StepDao
import com.example.myhealthtracker.data.local.entity.StepRecordEntity
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    @Inject lateinit var stepDao: StepDao
    @Inject lateinit var userProfileDataStore: UserProfileDataStore
    @Inject lateinit var calorieCalculator: CalorieCalculator

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null

    private var baselineSteps: Int = -1
    private var todayDate: String = getTodayDate()
    private var lastSavedSteps: Int = 0

    companion object {
        const val CHANNEL_ID = "step_counter_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.myhealthtracker.STOP_STEP_SERVICE"
        const val ACTION_STEPS_UPDATED = "com.example.myhealthtracker.STEPS_UPDATED"
        const val EXTRA_STEPS = "extra_steps"
        const val EXTRA_CALORIES = "extra_calories"
        const val EXTRA_DISTANCE = "extra_distance"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(0, 0.0))
        restoreBaseline()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        registerSensorListener()
        return START_STICKY
    }

    private fun registerSensorListener() {
        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return
        val totalStepsSinceReboot = event.values[0].toInt()

        val currentDate = getTodayDate()
        if (currentDate != todayDate) {
            handleMidnightReset(totalStepsSinceReboot)
        }

        if (baselineSteps == -1) {
            baselineSteps = totalStepsSinceReboot
        }

        val todaySteps = (totalStepsSinceReboot - baselineSteps).coerceAtLeast(0)

        if (todaySteps != lastSavedSteps) {
            lastSavedSteps = todaySteps
            persistSteps(todaySteps)
        }
    }

    private fun persistSteps(steps: Int) {
        serviceScope.launch {
            try {
                val profile = userProfileDataStore.userProfile.first()
                val calories = calorieCalculator.calculateCaloriesFromSteps(
                    steps = steps,
                    weightKg = profile.weightKg,
                    heightCm = profile.heightCm,
                    strideMultiplier = profile.strideMultiplier
                )
                val distanceMetres = calorieCalculator.calculateDistanceMetres(
                    steps = steps,
                    heightCm = profile.heightCm,
                    strideMultiplier = profile.strideMultiplier
                )

                val record = StepRecordEntity(
                    date = todayDate,
                    steps = steps,
                    caloriesBurned = calories,
                    distanceMetres = distanceMetres,
                    goalSteps = profile.dailyStepGoal
                )
                stepDao.upsertStepRecord(record)

                withContext(Dispatchers.Main) {
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        buildNotification(steps, calories)
                    )
                }

                val intent = Intent(ACTION_STEPS_UPDATED).apply {
                    putExtra(EXTRA_STEPS, steps)
                    putExtra(EXTRA_CALORIES, calories.toFloat())
                    putExtra(EXTRA_DISTANCE, distanceMetres.toFloat())
                    setPackage(packageName)
                }
                sendBroadcast(intent)

                saveBaselineToPrefs(baselineSteps, todayDate)

            } catch (e: Exception) {
                // Service must not crash
            }
        }
    }

    private fun handleMidnightReset(currentSensorTotal: Int) {
        todayDate = getTodayDate()
        baselineSteps = currentSensorTotal
        lastSavedSteps = 0
        saveBaselineToPrefs(baselineSteps, todayDate)
    }

    private fun restoreBaseline() {
        val prefs = getSharedPreferences("step_service_prefs", Context.MODE_PRIVATE)
        val savedDate = prefs.getString("baseline_date", null)
        val savedBaseline = prefs.getInt("baseline_steps", -1)
        if (savedDate == getTodayDate() && savedBaseline != -1) {
            baselineSteps = savedBaseline
        }
    }

    private fun saveBaselineToPrefs(baseline: Int, date: String) {
        getSharedPreferences("step_service_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("baseline_steps", baseline)
            .putString("baseline_date", date)
            .apply()
    }

    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Step Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Tracks your daily steps in the background"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(steps: Int, calories: Double): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, StepCounterService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitTrack Active")
            .setContentText("${"%,d".format(steps)} steps · ${"%.0f".format(calories)} kcal")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPending)
            .build()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
        super.onDestroy()
    }
}