package com.example.navigator3example.data.nuke

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.navigator3example.data.standards.StandardEntity

@Entity(tableName = "nukeCalibrations")
data class NukeGaugeCalibration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNumber: String,
    val date: Long,
    //val standardsList: List<StandardEntity> = StandardEntity,
    val timestamp: Long = System.currentTimeMillis(),
)
