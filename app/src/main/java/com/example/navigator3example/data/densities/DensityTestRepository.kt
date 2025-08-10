package com.example.navigator3example.data.densities

import kotlinx.coroutines.flow.Flow

class DensityTestRepository(private val dao: DensityTestDao) {

    fun getAll(): Flow<List<DensityTestEntity>> = dao.getAll()

    suspend fun insert(
        testNumber: String,
        testDate: String,
        location: String,
        offset: String,
        riceId: Long?,
        correctionFactor: Double?,
        wet1: Double?,
        wet2: Double?,
        wet3: Double?,
        wet4: Double?,
    ): Long {
        val entity = DensityTestEntity(
            testNumber = testNumber,
            testDate = testDate,
            location = location,
            offset = offset,
            riceId = riceId,
            correctionFactor = correctionFactor,
            wet1 = wet1,
            wet2 = wet2,
            wet3 = wet3,
            wet4 = wet4,
            timestamp = System.currentTimeMillis()
        )
        return dao.insert(entity)
    }

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()
}