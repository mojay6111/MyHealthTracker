package com.example.myhealthtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myhealthtracker.data.local.dao.*
import com.example.myhealthtracker.data.local.entity.*

@Database(
    entities = [
        StepRecordEntity::class,
        WaterLogEntity::class,
        WeightLogEntity::class,
        ActivitySessionEntity::class,
        PersonalRecordEntity::class,
        AchievementEntity::class,
        SleepLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FitTrackDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao
    abstract fun activitySessionDao(): ActivitySessionDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun achievementDao(): AchievementDao
    abstract fun sleepDao(): SleepDao

    companion object {
        const val DATABASE_NAME = "fittrack.db"
    }
}