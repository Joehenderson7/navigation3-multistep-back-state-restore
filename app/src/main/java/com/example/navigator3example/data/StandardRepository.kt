package com.example.navigator3example.data

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
            timestamp = System.currentTimeMillis()
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
    
    suspend fun generateTestData() {
        // Clear existing data first
        deleteAllStandards()
        
        val baseTime = System.currentTimeMillis()
        val serialNumber = "12345"
        
        // Generate 15 test standards with varied data
        val testData = listOf(
            // First 4 standards - baseline values
            Triple(100, 50, 0L),  // Density: 100, Moisture: 50
            Triple(102, 51, 1000L), // Density: 102, Moisture: 51
            Triple(98, 49, 2000L),  // Density: 98, Moisture: 49
            Triple(101, 52, 3000L), // Density: 101, Moisture: 52
            
            // Standards 5-8 - should mostly pass (within tolerance)
            Triple(100, 50, 4000L), // Should PASS - exactly at average
            Triple(99, 51, 5000L),  // Should PASS - density -1%, moisture +2%
            Triple(101, 49, 6000L), // Should PASS - density +1%, moisture -2%
            Triple(100, 50, 7000L), // Should PASS - exactly at average
            
            // Standards 9-12 - should fail (outside tolerance)
            Triple(95, 45, 8000L),  // Should FAIL - density -5%, moisture -10%
            Triple(105, 55, 9000L), // Should FAIL - density +5%, moisture +10%
            Triple(92, 58, 10000L), // Should FAIL - both outside tolerance
            Triple(108, 42, 11000L), // Should FAIL - both outside tolerance
            
            // Standards 13-15 - mixed results
            Triple(100, 53, 12000L), // Should FAIL - moisture +3% (over 2% limit)
            Triple(102, 50, 13000L), // Should FAIL - density +2% (over 1% limit)
            Triple(99, 51, 14000L)   // Should PASS - both within tolerance
        )
        
        testData.forEachIndexed { index, (density, moisture, timeOffset) ->
            val standard = StandardEntity(
                serialNumber = serialNumber,
                date = baseTime - (14 - index) * 24 * 60 * 60 * 1000L, // Spread over 15 days
                densityCount = density,
                moistureCount = moisture,
                timestamp = baseTime - (14 - index) * 24 * 60 * 60 * 1000L + timeOffset
            )
            standardDao.insertStandard(standard)
        }
    }
}