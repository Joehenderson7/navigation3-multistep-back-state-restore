package com.example.navigator3example

import com.example.navigator3example.data.standards.StandardEntity
import com.example.navigator3example.navigation.standards.Standard
import com.example.navigator3example.navigation.standards.convertMillisToDate
import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StandardTest {

    @Test
    fun `StandardEntity should be created with correct properties`() {
        // Given
        val serialNumber = "12345"
        val date = System.currentTimeMillis()
        val densityCount = 100
        val moistureCount = 50
        val timestamp = System.currentTimeMillis()

        // When
        val standard = StandardEntity(
            id = 1,
            serialNumber = serialNumber,
            date = date,
            densityCount = densityCount,
            moistureCount = moistureCount,
            timestamp = timestamp,
            gaugeSN = "TEST_GAUGE"
        )

        // Then
        assertEquals(1, standard.id)
        assertEquals(serialNumber, standard.serialNumber)
        assertEquals(date, standard.date)
        assertEquals(densityCount, standard.densityCount)
        assertEquals(moistureCount, standard.moistureCount)
        assertEquals(timestamp, standard.timestamp)
    }

    @Test
    fun `Standard data class should include serial number and timestamp`() {
        // Given
        val date = System.currentTimeMillis()
        val ds = 100
        val ms = 50
        val serialNumber = "12345"
        val timestamp = System.currentTimeMillis()

        // When
        val standard = Standard(date, ds, ms, serialNumber, timestamp)

        // Then
        assertEquals(date, standard.date)
        assertEquals(ds, standard.ds)
        assertEquals(ms, standard.ms)
        assertEquals(serialNumber, standard.serialNumber)
        assertEquals(timestamp, standard.timestamp)
    }

    @Test
    fun `convertMillisToDate should format date correctly`() {
        // Given
        val millis = 1640995200000L // January 1, 2022 00:00:00 UTC
        val expectedFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val expectedDate = expectedFormat.format(Date(millis))

        // When
        val result = convertMillisToDate(millis)

        // Then
        assertEquals(expectedDate, result)
    }

    @Test
    fun `StandardEntity should support sorting by timestamp`() {
        // Given
        val now = System.currentTimeMillis()
        val earlier = now - 1000
        val later = now + 1000

        val standard1 = StandardEntity(1, "12345", now, 100, 50, earlier, "GAUGE1")
        val standard2 = StandardEntity(2, "12345", now, 110, 55, now, "GAUGE1")
        val standard3 = StandardEntity(3, "12345", now, 120, 60, later, "GAUGE1")

        val standards = listOf(standard1, standard2, standard3)

        // When - Sort by timestamp descending (most recent first)
        val sortedStandards = standards.sortedByDescending { it.timestamp }

        // Then
        assertEquals(standard3.id, sortedStandards[0].id) // Most recent
        assertEquals(standard2.id, sortedStandards[1].id) // Middle
        assertEquals(standard1.id, sortedStandards[2].id) // Earliest
    }

    @Test
    fun `StandardEntity should support grouping by serial number`() {
        // Given
        val now = System.currentTimeMillis()
        val standard1 = StandardEntity(1, "12345", now, 100, 50, now, "GAUGE1")
        val standard2 = StandardEntity(2, "67890", now, 110, 55, now, "GAUGE2")
        val standard3 = StandardEntity(3, "12345", now, 120, 60, now, "GAUGE1")

        val standards = listOf(standard1, standard2, standard3)

        // When - Group by serial number
        val groupedStandards = standards.groupBy { it.serialNumber }

        // Then
        assertEquals(2, groupedStandards.size)
        assertEquals(2, groupedStandards["12345"]?.size)
        assertEquals(1, groupedStandards["67890"]?.size)
    }
}