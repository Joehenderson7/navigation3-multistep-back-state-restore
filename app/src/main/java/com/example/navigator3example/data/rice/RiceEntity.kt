package com.example.navigator3example.data.rice

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.navigator3example.calculations.CalculateRice

@Entity(tableName = "rices")
data class RiceEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: Long,
    val dryWeightA: Float,
    val dryWeightB: Float,
    val wetWeightA: Float,
    val wetWeightB: Float,
    val timestamp: Long = System.currentTimeMillis(),
    @Embedded(prefix = "cal_")
    val calibration: RiceCalibration,
)

@Entity(tableName = "riceCalibrations")
data class RiceCalibration(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weightA: Float,
    val weightB: Float,
    val date: Long,
    val timestamp: Long = System.currentTimeMillis(),
)

fun RiceEntity.getAverageRice(): Float =
    (CalculateRice(this.dryWeightA,this.wetWeightA,this.calibration.weightA) +
            CalculateRice(this.dryWeightB,this.wetWeightB,this.calibration.weightB) ) / 2

fun RiceEntity.getRiceA(): Float =
    CalculateRice(this.dryWeightA,this.wetWeightA,this.calibration.weightA)

fun RiceEntity.getRiceB(): Float =
    CalculateRice(this.dryWeightB,this.wetWeightB,this.calibration.weightB)