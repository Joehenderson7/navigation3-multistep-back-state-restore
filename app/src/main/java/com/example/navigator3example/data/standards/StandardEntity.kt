package com.example.navigator3example.data.standards

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "standards")
data class StandardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNumber: String,
    val date: Long,
    val densityCount: Int,
    val moistureCount: Int,
    val timestamp: Long = System.currentTimeMillis(), // For sorting same-day entries by most recent
    val gaugeSN: String
)

@Entity(tableName = "nukeGauges")
data class NukeGuage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNumber: String,
    val date: Long,
    val standardsList: List<StandardEntity>
)