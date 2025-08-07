package com.example.navigator3example

import com.example.navigator3example.data.StandardEntity
import com.example.navigator3example.navigation.StandardStatistics
import com.example.navigator3example.navigation.PassFailStatus
import org.junit.Test
import org.junit.Assert.*

class StandardAnalysisTest {

    @Test
    fun `calculateAverage should return correct average`() {
        // Given
        val values = listOf(100, 102, 98, 101)
        
        // When
        val result = StandardStatistics.calculateAverage(values)
        
        // Then
        assertEquals(100.25, result, 0.01)
    }

    @Test
    fun `calculateRelativeDifference should return correct percentage`() {
        // Given
        val current = 101
        val average = 100.0
        
        // When
        val result = StandardStatistics.calculateRelativeDifference(current, average)
        
        // Then
        assertEquals(1.0, result, 0.01)
    }

    @Test
    fun `checkDensityPassFail should pass within 1 percent tolerance`() {
        // Given
        val withinTolerance = 0.9
        val atTolerance = 1.0
        val outsideTolerance = 1.1
        
        // When & Then
        assertEquals(PassFailStatus.PASS, StandardStatistics.checkDensityPassFail(withinTolerance))
        assertEquals(PassFailStatus.PASS, StandardStatistics.checkDensityPassFail(atTolerance))
        assertEquals(PassFailStatus.FAIL, StandardStatistics.checkDensityPassFail(outsideTolerance))
        assertEquals(PassFailStatus.INSUFFICIENT_DATA, StandardStatistics.checkDensityPassFail(null))
    }

    @Test
    fun `checkMoisturePassFail should pass within 2 percent tolerance`() {
        // Given
        val withinTolerance = 1.9
        val atTolerance = 2.0
        val outsideTolerance = 2.1
        
        // When & Then
        assertEquals(PassFailStatus.PASS, StandardStatistics.checkMoisturePassFail(withinTolerance))
        assertEquals(PassFailStatus.PASS, StandardStatistics.checkMoisturePassFail(atTolerance))
        assertEquals(PassFailStatus.FAIL, StandardStatistics.checkMoisturePassFail(outsideTolerance))
        assertEquals(PassFailStatus.INSUFFICIENT_DATA, StandardStatistics.checkMoisturePassFail(null))
    }

    @Test
    fun `test data scenarios should produce expected pass fail results`() {
        // Given - Test data based on the generated test data
        // First 4 standards: density avg = 100.25, moisture avg = 50.5
        val baselineStandards = listOf(
            StandardEntity(1, "12345", System.currentTimeMillis(), 100, 50, System.currentTimeMillis()),
            StandardEntity(2, "12345", System.currentTimeMillis(), 102, 51, System.currentTimeMillis()),
            StandardEntity(3, "12345", System.currentTimeMillis(), 98, 49, System.currentTimeMillis()),
            StandardEntity(4, "12345", System.currentTimeMillis(), 101, 52, System.currentTimeMillis())
        )
        
        val avgDensity = StandardStatistics.calculateAverage(baselineStandards.map { it.densityCount })
        val avgMoisture = StandardStatistics.calculateAverage(baselineStandards.map { it.moistureCount })
        
        // Test cases from generated data
        // Baseline: density avg = 100.25, moisture avg = 50.5
        val testCases = listOf(
            // 100 vs 100.25 = -0.2%, 50 vs 50.5 = -1.0% -> PASS/PASS
            Triple(100, 50, "PASS/PASS"),
            // 99 vs 100.25 = -1.2%, 51 vs 50.5 = +1.0% -> FAIL/PASS (density outside ±1%)
            Triple(99, 51, "FAIL/PASS"),
            // 101 vs 100.25 = +0.7%, 49 vs 50.5 = -3.0% -> PASS/FAIL (moisture outside ±2%)
            Triple(101, 49, "PASS/FAIL"),
            // 95 vs 100.25 = -5.2%, 45 vs 50.5 = -10.9% -> FAIL/FAIL
            Triple(95, 45, "FAIL/FAIL"),
            // 105 vs 100.25 = +4.7%, 55 vs 50.5 = +8.9% -> FAIL/FAIL
            Triple(105, 55, "FAIL/FAIL"),
            // 100 vs 100.25 = -0.2%, 53 vs 50.5 = +4.9% -> PASS/FAIL (moisture outside ±2%)
            Triple(100, 53, "PASS/FAIL"),
            // 102 vs 100.25 = +1.7%, 50 vs 50.5 = -1.0% -> FAIL/PASS (density outside ±1%)
            Triple(102, 50, "FAIL/PASS")
        )
        
        // When & Then
        testCases.forEach { (density, moisture, expected) ->
            val densityDiff = StandardStatistics.calculateRelativeDifference(density, avgDensity)
            val moistureDiff = StandardStatistics.calculateRelativeDifference(moisture, avgMoisture)
            
            val densityResult = StandardStatistics.checkDensityPassFail(densityDiff)
            val moistureResult = StandardStatistics.checkMoisturePassFail(moistureDiff)
            
            val actualResult = "${densityResult.name}/${moistureResult.name}"
            
            println("[DEBUG_LOG] Density: $density (${String.format("%.1f", densityDiff)}%), Moisture: $moisture (${String.format("%.1f", moistureDiff)}%) -> $actualResult")
            assertEquals("Failed for density=$density, moisture=$moisture", expected, actualResult)
        }
    }

    @Test
    fun `statistical calculations should handle edge cases`() {
        // Given
        val emptyList = emptyList<Int>()
        val singleValue = listOf(100)
        
        // When & Then
        assertEquals(0.0, StandardStatistics.calculateAverage(emptyList), 0.01)
        assertEquals(100.0, StandardStatistics.calculateAverage(singleValue), 0.01)
        assertEquals(0.0, StandardStatistics.calculateRelativeDifference(100, 0.0), 0.01)
    }
}