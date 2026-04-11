package com.example.myhealthtracker.util

import android.content.Context
import androidx.work.*
import com.example.myhealthtracker.worker.HydrationReminderWorker
import com.example.myhealthtracker.worker.SedentaryAlertWorker
import com.example.myhealthtracker.worker.StepGoalNudgeWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleAll() {
        scheduleHydrationReminders()
        scheduleSedentaryAlerts()
        scheduleStepGoalNudge()
    }

    fun cancelAll() {
        workManager.cancelAllWork()
    }

    // Hydration reminder every 2 hours during waking hours
    private fun scheduleHydrationReminders() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            2, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(30, TimeUnit.MINUTES)
            .addTag("hydration_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "hydration_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            hydrationWork
        )
    }

    // Sedentary alert every 90 minutes
    private fun scheduleSedentaryAlerts() {
        val sedentaryWork = PeriodicWorkRequestBuilder<SedentaryAlertWorker>(
            90, TimeUnit.MINUTES
        )
            .setInitialDelay(90, TimeUnit.MINUTES)
            .addTag("sedentary_alert")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "sedentary_alert",
            ExistingPeriodicWorkPolicy.UPDATE,
            sedentaryWork
        )
    }

    // Step goal nudge at 8 PM daily
    private fun scheduleStepGoalNudge() {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If 8 PM has passed today schedule for tomorrow
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val stepNudgeWork = PeriodicWorkRequestBuilder<StepGoalNudgeWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("step_goal_nudge")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "step_goal_nudge",
            ExistingPeriodicWorkPolicy.UPDATE,
            stepNudgeWork
        )
    }
}