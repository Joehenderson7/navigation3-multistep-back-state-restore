package com.example.navigator3example.data.rice

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus

class RiceRepository(
    private val dao: RiceDao,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

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
        val instant = Instant.fromEpochMilliseconds(date)
        val localDate = instant.toLocalDateTime(timeZone).date
        val start = localDate.atStartOfDayIn(timeZone).toEpochMilliseconds()
        val end = (localDate + DatePeriod(days = 1)).atStartOfDayIn(timeZone).toEpochMilliseconds()
        return start to end
    }
}