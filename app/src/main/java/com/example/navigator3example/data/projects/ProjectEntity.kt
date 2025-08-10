package com.example.navigator3example.data.projects

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.navigator3example.calculations.CalculateRice


@Entity
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String,
    var projectNumber: String,
    var projectLocation: String,
)