package com.example.navigator3example.data.standards

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.navigator3example.data.nuke.NukeGaugeCalibration

@Entity(
    tableName = "standards",
    foreignKeys = [
        ForeignKey(
            entity = NukeGaugeCalibration::class,
            parentColumns = ["id"],
            childColumns = ["calibrationId"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("calibrationId"), Index("serialNumber"), Index("gaugeSN")]
)

data class StandardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNumber: String,
    val date: Long,
    val densityCount: Int,
    val moistureCount: Int,
    val timestamp: Long = System.currentTimeMillis(), // For sorting same-day entries by most recent
    val gaugeSN: String,
    val calibrationId: Long? = null
)
