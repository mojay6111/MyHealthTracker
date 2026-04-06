package com.example.myhealthtracker.data.repository

import com.example.myhealthtracker.data.local.dao.StepDao
import com.example.myhealthtracker.data.local.entity.StepRecordEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: StepDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayStepRecord(): Flow<StepRecordEntity?> =
        stepDao.getStepRecord(today())

    fun getWeeklyRecords(): Flow<List<StepRecordEntity>> {
        val cal = Calendar.getInstance()
        val to = today()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val from = dateFormat.format(cal.time)
        return stepDao.getStepRecordsBetween(from, to)
    }

    fun getRecentRecords(days: Int = 30): Flow<List<StepRecordEntity>> =
        stepDao.getRecentStepRecords(days)

    suspend fun upsertStepRecord(record: StepRecordEntity) =
        stepDao.upsertStepRecord(record)

    private fun today(): String = dateFormat.format(Date())
}