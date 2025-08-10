package com.example.navigator3example.data.nuke

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NukeGaugeDao {
    @Insert
    suspend fun insertCalibration(calibration: NukeGaugeCalibration): Long

    @Query("SELECT * FROM nukeCalibrations ORDER BY date DESC, timestamp DESC")
    fun getAllCalibrations(): Flow<List<NukeGaugeCalibration>>

    @Query("SELECT DISTINCT serialNumber FROM nukeCalibrations ORDER BY serialNumber")
    fun getAllSerialNumbers(): Flow<List<String>>

    @Query("DELETE FROM nukeCalibrations WHERE id = :id")
    suspend fun deleteCalibration(id: Long)

    @Query("DELETE FROM nukeCalibrations")
    suspend fun deleteAllCalibrations()
}
