package com.example.myhealthtracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.myhealthtracker.MainActivity
import com.example.myhealthtracker.data.local.dao.ActivitySessionDao
import com.example.myhealthtracker.data.local.entity.ActivitySessionEntity
import com.example.myhealthtracker.domain.usecase.CalorieCalculator
import com.example.myhealthtracker.data.datastore.UserProfileDataStore
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RouteTrackingService : Service() {

    @Inject lateinit var activitySessionDao: ActivitySessionDao
    @Inject lateinit var calorieCalculator: CalorieCalculator
    @Inject lateinit var userProfileDataStore: UserProfileDataStore

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Session state
    private var activityType = "WALK"
    private var startTime = 0L
    private var totalDistanceMetres = 0.0
    private var lastLocation: Location? = null
    private val routePoints = mutableListOf<RoutePoint>()
    private var maxSpeedKmh = 0.0
    private var isTracking = false
    private var isPaused = false

    companion object {
        const val CHANNEL_ID = "route_tracking_channel"
        const val NOTIFICATION_ID = 1002
        const val ACTION_START = "com.example.myhealthtracker.START_ROUTE"
        const val ACTION_STOP = "com.example.myhealthtracker.STOP_ROUTE"
        const val ACTION_PAUSE = "com.example.myhealthtracker.PAUSE_ROUTE"
        const val ACTION_RESUME = "com.example.myhealthtracker.RESUME_ROUTE"
        const val EXTRA_ACTIVITY_TYPE = "extra_activity_type"

        // Broadcasts
        const val ACTION_LOCATION_UPDATE = "com.example.myhealthtracker.LOCATION_UPDATE"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_DISTANCE = "extra_distance"
        const val EXTRA_SPEED_KMH = "extra_speed_kmh"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_DETECTED_ACTIVITY = "extra_detected_activity"

        // Speed thresholds for auto detection (km/h)
        const val WALK_MAX_SPEED = 7.0
        const val RUN_MAX_SPEED = 20.0
    }

    @kotlinx.serialization.Serializable
    data class RoutePoint(
        val lat: Double,
        val lng: Double,
        val timestamp: Long,
        val speedKmh: Double
    )

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                activityType = intent.getStringExtra(EXTRA_ACTIVITY_TYPE) ?: "WALK"
                startTracking()
            }
            ACTION_STOP -> stopTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
        }
        return START_NOT_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isTracking || isPaused) return
                result.lastLocation?.let { location ->
                    processLocation(location)
                }
            }
        }
    }

    private fun startTracking() {
        isTracking = true
        isPaused = false
        startTime = System.currentTimeMillis()
        routePoints.clear()
        totalDistanceMetres = 0.0
        lastLocation = null

        startForeground(NOTIFICATION_ID, buildNotification("Starting GPS..."))
        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // every 3 seconds
        ).apply {
            setMinUpdateDistanceMeters(5f) // minimum 5 metres movement
            setGranularity(Granularity.GRANULARITY_FINE)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun processLocation(location: Location) {
        val speedKmh = location.speed * 3.6 // m/s to km/h

        // Auto detect activity type from speed
        val detectedActivity = when {
            speedKmh < WALK_MAX_SPEED -> "WALK"
            speedKmh < RUN_MAX_SPEED -> "RUN"
            else -> "CYCLE"
        }

        // Update max speed
        if (speedKmh > maxSpeedKmh) maxSpeedKmh = speedKmh

        // Calculate distance from last point
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)
            if (distance > 2f) { // ignore tiny movements < 2m
                totalDistanceMetres += distance
            }
        }
        lastLocation = location

        // Add point to route
        routePoints.add(
            RoutePoint(
                lat = location.latitude,
                lng = location.longitude,
                timestamp = System.currentTimeMillis(),
                speedKmh = speedKmh
            )
        )

        val durationSeconds = (System.currentTimeMillis() - startTime) / 1000
        val avgSpeedKmh = if (durationSeconds > 0)
            (totalDistanceMetres / durationSeconds) * 3.6
        else 0.0

        // Update notification
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            NOTIFICATION_ID,
            buildNotification(
                "${"%.2f".format(totalDistanceMetres / 1000)}km · " +
                        "${durationSeconds / 60}min · " +
                        "${"%.1f".format(speedKmh)}km/h"
            )
        )

        // Broadcast update to UI
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra(EXTRA_LATITUDE, location.latitude)
            putExtra(EXTRA_LONGITUDE, location.longitude)
            putExtra(EXTRA_DISTANCE, totalDistanceMetres.toFloat())
            putExtra(EXTRA_SPEED_KMH, speedKmh.toFloat())
            putExtra(EXTRA_DURATION, durationSeconds)
            putExtra(EXTRA_DETECTED_ACTIVITY, detectedActivity)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun pauseTracking() {
        isPaused = true
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun resumeTracking() {
        isPaused = false
        requestLocationUpdates()
    }

    private fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        saveSession()
    }

    private fun saveSession() {
        serviceScope.launch {
            try {
                val profile = userProfileDataStore.userProfile.first()
                val endTime = System.currentTimeMillis()
                val durationSeconds = (endTime - startTime) / 1000
                val avgSpeedKmh = if (durationSeconds > 0)
                    (totalDistanceMetres / durationSeconds) * 3.6
                else 0.0
                val avgPace = if (avgSpeedKmh > 0) 60.0 / avgSpeedKmh else 0.0

                val calories = calorieCalculator.calculateCaloriesFromDuration(
                    durationMinutes = durationSeconds / 60.0,
                    weightKg = profile.weightKg,
                    activityType = activityType
                )

                val routeJson = if (routePoints.isNotEmpty())
                    Json.encodeToString(routePoints)
                else null

                val session = ActivitySessionEntity(
                    date = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(Date()),
                    activityType = activityType,
                    startTimestamp = startTime,
                    endTimestamp = endTime,
                    durationSeconds = durationSeconds,
                    distanceMetres = totalDistanceMetres,
                    caloriesBurned = calories,
                    avgSpeedKmh = avgSpeedKmh,
                    maxSpeedKmh = maxSpeedKmh,
                    avgPaceMinPerKm = avgPace,
                    routePointsJson = routeJson
                )
                activitySessionDao.insertSession(session)
            } catch (e: Exception) {
                // silent fail
            } finally {
                withContext(Dispatchers.Main) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Route Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Tracks your route while exercising"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(status: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, RouteTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$activityType in progress")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_menu_mapmode)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPending)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
        super.onDestroy()
    }
}