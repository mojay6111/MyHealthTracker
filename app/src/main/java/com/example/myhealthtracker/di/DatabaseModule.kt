package com.example.myhealthtracker.di

import android.content.Context
import androidx.room.Room
import com.example.myhealthtracker.data.local.FitTrackDatabase
import com.example.myhealthtracker.data.local.dao.*
import com.example.myhealthtracker.util.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FitTrackDatabase {
        return Room.databaseBuilder(
            context,
            FitTrackDatabase::class.java,
            FitTrackDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStepDao(db: FitTrackDatabase): StepDao = db.stepDao()

    @Provides
    fun provideWaterDao(db: FitTrackDatabase): WaterDao = db.waterDao()

    @Provides
    fun provideWeightDao(db: FitTrackDatabase): WeightDao = db.weightDao()

    @Provides
    fun provideActivitySessionDao(db: FitTrackDatabase): ActivitySessionDao =
        db.activitySessionDao()

    @Provides
    fun providePersonalRecordDao(db: FitTrackDatabase): PersonalRecordDao =
        db.personalRecordDao()

    @Provides
    fun provideAchievementDao(db: FitTrackDatabase): AchievementDao =
        db.achievementDao()

    @Provides
    fun provideSleepDao(db: FitTrackDatabase): SleepDao = db.sleepDao()

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper = NotificationHelper(context)

}