package com.example.myhealthtracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.myhealthtracker.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext


@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val HYDRATION_CHANNEL_ID = "hydration_reminders"
        const val SEDENTARY_CHANNEL_ID = "sedentary_alerts"
        const val STEP_GOAL_CHANNEL_ID = "step_goal_alerts"

        const val HYDRATION_NOTIFICATION_ID = 2001
        const val SEDENTARY_NOTIFICATION_ID = 2002
        const val STEP_GOAL_NOTIFICATION_ID = 2003

        const val ACHIEVEMENT_CHANNEL_ID = "achievements"

        const val ACHIEVEMENT_NOTIFICATION_ID = 2004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Hydration channel
        manager.createNotificationChannel(
            NotificationChannel(
                HYDRATION_CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water throughout the day"
            }
        )

        // Sedentary channel
        manager.createNotificationChannel(
            NotificationChannel(
                SEDENTARY_CHANNEL_ID,
                "Sedentary Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when you've been inactive too long"
            }
        )

        // Step goal channel
        manager.createNotificationChannel(
            NotificationChannel(
                STEP_GOAL_CHANNEL_ID,
                "Step Goal",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily step goal progress reminders"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                ACHIEVEMENT_CHANNEL_ID,
                "Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when you unlock new achievements"
            }
        )
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showHydrationReminder(totalMl: Int, goalMl: Int) {
        val remaining = goalMl - totalMl
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, HYDRATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("💧 Time to hydrate!")
            .setContentText("You've had ${totalMl}ml. ${remaining}ml left to reach your goal!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "You've had ${totalMl}ml of water today. " +
                                "Drink ${remaining}ml more to hit your ${goalMl}ml goal. " +
                                "Staying hydrated keeps you energized! 💪"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(HYDRATION_NOTIFICATION_ID, notification)
    }

    fun showSedentaryAlert(inactiveMinutes: Int) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, SEDENTARY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🪑 Time to move!")
            .setContentText(
                "You've been sitting for $inactiveMinutes minutes. " +
                        "Take a short walk!"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "You've been inactive for $inactiveMinutes minutes. " +
                                "Even a 5-minute walk improves circulation and boosts energy. " +
                                "Let's get moving! 🚶"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(SEDENTARY_NOTIFICATION_ID, notification)
    }

    fun showStepGoalNudge(currentSteps: Int, goalSteps: Int) {
        val remaining = goalSteps - currentSteps
        val percentage = (currentSteps.toFloat() / goalSteps * 100).toInt()
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, STEP_GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🎯 So close to your goal!")
            .setContentText(
                "$percentage% there! Only $remaining steps to go!"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "You've taken ${"%,d".format(currentSteps)} steps today. " +
                                "Just ${"%,d".format(remaining)} more steps to reach your " +
                                "${"%,d".format(goalSteps)} step goal. You've got this! 🔥"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(STEP_GOAL_NOTIFICATION_ID, notification)
    }

    fun showStepGoalAchieved(steps: Int) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, STEP_GOAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🏆 Goal Achieved!")
            .setContentText(
                "Amazing! You've hit ${"%,d".format(steps)} steps today!"
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(STEP_GOAL_NOTIFICATION_ID, notification)
    }

    fun showAchievementUnlocked(title: String, description: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("🏆 Achievement Unlocked!")
            .setContentText(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$title — $description")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .build()
        manager.notify(ACHIEVEMENT_NOTIFICATION_ID, notification)
    }
}