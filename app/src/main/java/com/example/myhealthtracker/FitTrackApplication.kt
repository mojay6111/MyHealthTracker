package com.example.myhealthtracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.myhealthtracker.domain.usecase.AchievementEngine
import com.example.myhealthtracker.util.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FitTrackApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var achievementEngine: AchievementEngine

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationScheduler.scheduleAll()
        MainScope().launch {
            achievementEngine.seedAchievements()
            achievementEngine.checkAndUnlock(10_000)
            achievementEngine.checkHistoricalData()
        }
    }
}