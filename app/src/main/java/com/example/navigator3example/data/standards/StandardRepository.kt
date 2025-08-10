package com.example.navigator3example.data.standards

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class StandardRepository(private val standardDao: StandardDao) {
    
    fun getAllStandards(): Flow<List<StandardEntity>> {
        return standardDao.getAllStandards()
    }
    
    suspend fun insertStandard(
        serialNumber: String,
        date: Long,
        densityCount: Int,
        moistureCount: Int
    ) {
        val standard = StandardEntity(
            serialNumber = serialNumber,
            date = date,
            densityCount = densityCount,
            moistureCount = moistureCount,
            timestamp = System.currentTimeMillis(),
            gaugeSN = "3717"
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
        // Get start and end of the day for the given date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis
        
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