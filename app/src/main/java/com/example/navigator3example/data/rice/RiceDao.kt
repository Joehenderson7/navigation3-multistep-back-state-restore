package com.example.navigator3example.data.rice

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RiceDao {
    // Rice entities
    @Insert
    suspend fun insertRice(rice: RiceEntity): Long

    @Query("SELECT * FROM rices ORDER BY date DESC, timestamp DESC")
    fun getAllRices(): Flow<List<RiceEntity>>

    @Query("SELECT * FROM rices WHERE id = :id")
    fun getRiceById(id: Long): Flow<RiceEntity?>

    @Query("SELECT * FROM rices WHERE date >= :startOfDay AND date < :endOfDay ORDER BY timestamp DESC")
    suspend fun getRicesByDate(startOfDay: Long, endOfDay: Long): List<RiceEntity>

    @Query("DELETE FROM rices WHERE id = :id")
    suspend fun deleteRice(id: Long)

    @Query("DELETE FROM rices")
    suspend fun deleteAllRices()

    // Calibration entities
    @Insert
    suspend fun insertCalibration(calibration: RiceCalibration): Long

    @Query("SELECT * FROM riceCalibrations ORDER BY date DESC, timestamp DESC")
    fun getAllCalibrations(): Flow<List<RiceCalibration>>

    @Query("DELETE FROM riceCalibrations WHERE id = :id")
    suspend fun deleteCalibration(id: Long)

    @Query("DELETE FROM riceCalibrations")
    suspend fun deleteAllCalibrations()
}