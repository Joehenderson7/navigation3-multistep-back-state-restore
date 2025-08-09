package com.example.navigator3example.data.rice

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class RiceRepository(private val dao: RiceDao) {

    // Rices
    fun getAllRices(): Flow<List<RiceEntity>> = dao.getAllRices()

    fun getRiceById(id: Long): Flow<RiceEntity?> = dao.getRiceById(id)

    suspend fun insertRice(
        name: String,
        date: Long,
        dryWeightA: Float,
        dryWeightB: Float,
        wetWeightA: Float,
        wetWeightB: Float,
        calibration: RiceCalibration,
    ): Long {
        val rice = RiceEntity(
            name = name,
            date = date,
            dryWeightA = dryWeightA,
            dryWeightB = dryWeightB,
            wetWeightA = wetWeightA,
            wetWeightB = wetWeightB,
            timestamp = System.currentTimeMillis(),
            calibration = calibration,
        )
        return dao.insertRice(rice)
    }

    suspend fun getRicesByDate(date: Long): List<RiceEntity> {
        val (startOfDay, endOfDay) = startEndOfDay(date)
        return dao.getRicesByDate(startOfDay, endOfDay)
    }

    suspend fun deleteRice(id: Long) = dao.deleteRice(id)

    suspend fun deleteAllRices() = dao.deleteAllRices()

    // Calibrations
    fun getAllCalibrations(): Flow<List<RiceCalibration>> = dao.getAllCalibrations()

    suspend fun insertCalibration(weightA: Float, weightB: Float, date: Long): Long {
        val calibration = RiceCalibration(
            weightA = weightA,
            weightB = weightB,
            date = date,
            timestamp = System.currentTimeMillis(),
        )
        return dao.insertCalibration(calibration)
    }

    suspend fun deleteCalibration(id: Long) = dao.deleteCalibration(id)

    suspend fun deleteAllCalibrations() = dao.deleteAllCalibrations()

    private fun startEndOfDay(date: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val end = calendar.timeInMillis
        return start to end
    }
}