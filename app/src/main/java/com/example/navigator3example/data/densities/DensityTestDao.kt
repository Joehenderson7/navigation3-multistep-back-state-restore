package com.example.navigator3example.data.densities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DensityTestDao {
    @Insert
    suspend fun insert(test: DensityTestEntity): Long

    @Query("SELECT * FROM density_tests ORDER BY timestamp DESC")
    fun getAll(): Flow<List<DensityTestEntity>>

    @Query("DELETE FROM density_tests WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM density_tests")
    suspend fun deleteAll()
}