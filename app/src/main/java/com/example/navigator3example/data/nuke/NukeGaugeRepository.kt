package com.example.navigator3example.data.nuke

import kotlinx.coroutines.flow.Flow

class NukeGaugeRepository(private val dao: NukeGaugeDao) {
    fun getAllCalibrations(): Flow<List<NukeGaugeCalibration>> = dao.getAllCalibrations()

    fun getAllSerialNumbers(): Flow<List<String>> = dao.getAllSerialNumbers()

    suspend fun insertGauge(serialNumber: String, date: Long): Long {
        val gauge = NukeGaugeCalibration(
            serialNumber = serialNumber,
            date = date,
            timestamp = System.currentTimeMillis(),
        )
        return dao.insertCalibration(gauge)
    }

    suspend fun deleteCalibration(id: Long) = dao.deleteCalibration(id)

    suspend fun deleteAllCalibrations() = dao.deleteAllCalibrations()
}