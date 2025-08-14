package com.example.navigator3example.data.standards

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus

class StandardRepository(
    private val standardDao: StandardDao,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {
    
    fun getAllStandards(): Flow<List<StandardEntity>> {
        return standardDao.getAllStandards()
    }
    
    suspend fun insertStandard(
        serialNumber: String,
        date: Long,
        densityCount: Int,
        moistureCount: Int,
        calibrationId: Long? = null
    ) {
        val standard = StandardEntity(
            serialNumber = serialNumber,
            date = date,
            densityCount = densityCount,
            moistureCount = moistureCount,
            timestamp = System.currentTimeMillis(),
            gaugeSN = "3717",
            calibrationId = calibrationId
        )
        standardDao.insertStandard(standard)
    }
    
    suspend fun getStandardsBySerialNumber(serialNumber: String): List<StandardEntity> {
        return standardDao.getStandardsBySerialNumber(serialNumber)
    }
    
    suspend fun getLastNStandardsBySerialNumber(serialNumber: String, limit: Int): List<StandardEntity> {
        return standardDao.getLastNStandardsBySerialNumber(serialNumber, limit)
    }
    
    suspend fun getPreviousNStandards(serialNumber: String, beforeTimestamp: Long, limit: Int): List<StandardEntity> {
        return standardDao.getPreviousNStandards(serialNumber, beforeTimestamp, limit)
    }
    
    fun getStandardsBySerialNumberFlow(serialNumber: String): Flow<List<StandardEntity>> {
        return standardDao.getStandardsBySerialNumberFlow(serialNumber)
    }
    
    suspend fun getStandardsByDate(date: Long): List<StandardEntity> {
        val instant = Instant.fromEpochMilliseconds(date)
        val localDate = instant.toLocalDateTime(timeZone).date
        val startOfDay = localDate.atStartOfDayIn(timeZone).toEpochMilliseconds()
        val endOfDay = (localDate + DatePeriod(days = 1)).atStartOfDayIn(timeZone).toEpochMilliseconds()
        return standardDao.getStandardsByDate(startOfDay, endOfDay)
    }
    
    suspend fun getAllSerialNumbers(): List<String> {
        return standardDao.getAllSerialNumbers()
    }
    
    suspend fun deleteStandard(id: Long) {
        standardDao.deleteStandard(id)
    }
    
    suspend fun deleteAllStandards() {
        standardDao.deleteAllStandards()
    }
    
}