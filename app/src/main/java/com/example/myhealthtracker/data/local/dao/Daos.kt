package com.example.myhealthtracker.data.local.dao

import androidx.room.*
import com.example.myhealthtracker.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_records WHERE date = :date")
    fun getStepRecord(date: String): Flow<StepRecordEntity?>

    @Query("SELECT * FROM step_records ORDER BY date DESC LIMIT :limit")
    fun getRecentStepRecords(limit: Int = 30): Flow<List<StepRecordEntity>>

    @Query("SELECT * FROM step_records WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun getStepRecordsBetween(from: String, to: String): Flow<List<StepRecordEntity>>

    @Query("SELECT SUM(steps) FROM step_records WHERE date BETWEEN :from AND :to")
    fun getTotalStepsBetween(from: String, to: String): Flow<Int?>

    @Query("SELECT MAX(steps) FROM step_records")
    fun getMaxStepsEver(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStepRecord(record: StepRecordEntity)

    @Query("DELETE FROM step_records WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE date = :date")
    fun getTotalWaterForDate(date: String): Flow<Int?>

    @Query("SELECT * FROM water_logs WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun getWaterLogsBetween(from: String, to: String): Flow<List<WaterLogEntity>>

    @Insert
    suspend fun insertWaterLog(log: WaterLogEntity): Long

    @Delete
    suspend fun deleteWaterLog(log: WaterLogEntity)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllWeightLogs(): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun getWeightLogsBetween(from: String, to: String): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWeight(): Flow<WeightLogEntity?>

    @Insert
    suspend fun insertWeightLog(log: WeightLogEntity): Long

    @Delete
    suspend fun deleteWeightLog(log: WeightLogEntity)

    @Update
    suspend fun updateWeightLog(log: WeightLogEntity)
}

@Dao
interface ActivitySessionDao {
    @Query("SELECT * FROM activity_sessions ORDER BY startTimestamp DESC")
    fun getAllSessions(): Flow<List<ActivitySessionEntity>>

    @Query("SELECT * FROM activity_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): ActivitySessionEntity?

    @Query("SELECT * FROM activity_sessions WHERE activityType = :type ORDER BY startTimestamp DESC")
    fun getSessionsByType(type: String): Flow<List<ActivitySessionEntity>>

    @Query("SELECT * FROM activity_sessions WHERE date BETWEEN :from AND :to ORDER BY startTimestamp ASC")
    fun getSessionsBetween(from: String, to: String): Flow<List<ActivitySessionEntity>>

    @Query("SELECT * FROM activity_sessions ORDER BY startTimestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<ActivitySessionEntity>>

    @Query("SELECT SUM(distanceMetres) FROM activity_sessions")
    fun getTotalDistanceEver(): Flow<Double?>

    @Query("SELECT MAX(distanceMetres) FROM activity_sessions WHERE activityType = :type")
    fun getLongestSessionByType(type: String): Flow<Double?>

    @Insert
    suspend fun insertSession(session: ActivitySessionEntity): Long

    @Update
    suspend fun updateSession(session: ActivitySessionEntity)

    @Delete
    suspend fun deleteSession(session: ActivitySessionEntity)
}

@Dao
interface PersonalRecordDao {
    @Query("SELECT * FROM personal_records ORDER BY updatedAt DESC")
    fun getAllPersonalRecords(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE category = :category")
    suspend fun getRecord(category: String): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: PersonalRecordEntity)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    fun getUnlockedAchievements(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAchievements(achievements: List<AchievementEntity>)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_logs ORDER BY sleepStart DESC")
    fun getAllSleepLogs(): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM sleep_logs ORDER BY sleepStart DESC LIMIT :limit")
    fun getRecentSleepLogs(limit: Int = 7): Flow<List<SleepLogEntity>>

    @Insert
    suspend fun insertSleepLog(log: SleepLogEntity): Long

    @Delete
    suspend fun deleteSleepLog(log: SleepLogEntity)

    @Update
    suspend fun updateSleepLog(log: SleepLogEntity)
}