package com.example.navigator3example.data.densities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "density_tests")
data class DensityTestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val testNumber: String,
    val testDate: String, // Stored as human-readable string, aligns with current UI state
    val location: String,
    val offset: String,
    val riceId: Long?,
    val correctionFactor: Double?,
    val wet1: Double?,
    val wet2: Double?,
    val wet3: Double?,
    val wet4: Double?,
    val timestamp: Long = System.currentTimeMillis()
)