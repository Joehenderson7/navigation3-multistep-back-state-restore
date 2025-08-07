package com.example.navigator3example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StandardDao {
    
    @Insert
    suspend fun insertStandard(standard: StandardEntity)
    
    @Query("SELECT * FROM standards WHERE serialNumber = :serialNumber ORDER BY timestamp DESC")
    suspend fun getStandardsBySerialNumber(serialNumber: String): List<StandardEntity>
    
    @Query("SELECT * FROM standards WHERE serialNumber = :serialNumber ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLastNStandardsBySerialNumber(serialNumber: String, limit: Int): List<StandardEntity>
    
    @Query("SELECT * FROM standards WHERE serialNumber = :serialNumber AND timestamp < :beforeTimestamp ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getPreviousNStandards(serialNumber: String, beforeTimestamp: Long, limit: Int): List<StandardEntity>
    
    @Query("SELECT * FROM standards ORDER BY date DESC, timestamp DESC")
    fun getAllStandards(): Flow<List<StandardEntity>>
    
    @Query("SELECT * FROM standards WHERE serialNumber = :serialNumber ORDER BY timestamp DESC")
    fun getStandardsBySerialNumberFlow(serialNumber: String): Flow<List<StandardEntity>>
    
    @Query("SELECT * FROM standards WHERE date >= :startOfDay AND date < :endOfDay ORDER BY timestamp DESC")
    suspend fun getStandardsByDate(startOfDay: Long, endOfDay: Long): List<StandardEntity>
    
    @Query("SELECT DISTINCT serialNumber FROM standards ORDER BY serialNumber")
    suspend fun getAllSerialNumbers(): List<String>
    
    @Query("DELETE FROM standards WHERE id = :id")
    suspend fun deleteStandard(id: Long)
    
    @Query("DELETE FROM standards")
    suspend fun deleteAllStandards()
}