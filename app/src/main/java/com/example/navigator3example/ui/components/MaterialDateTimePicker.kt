package com.example.navigator3example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Material 3 themed Date Time Picker Component
 * Based on the standards logic from Standards.kt with theme influence
 * 
 * @param value Current selected date as string
 * @param onDateSelected Callback when date is selected
 * @param label Label for the text field
 * @param placeholder Placeholder text when no date is selected
 * @param modifier Modifier for the component
 * @param initialDateMillis Initial date in milliseconds (optional)
 * @param dateFormat Date format pattern (default: "MM/dd/yyyy")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDateTimePicker(
    value: String,
    onDateSelected: (String) -> Unit,
    label: String = "Date",
    placeholder: String = "Select Date",
    modifier: Modifier = Modifier,
    initialDateMillis: Long? = null,
    dateFormat: String = "MM/dd/yyyy"
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis())
    
    // Convert milliseconds to date string for display
    val selectedDate = datePickerState.selectedDateMillis?.let {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.format(Date(it))
    } ?: value

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (selectedDate.isEmpty()) placeholder else selectedDate,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        // Popup-based date picker with theme influence (copied from standards logic)
        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        colors = androidx.compose.material3.DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            headlineContentColor = MaterialTheme.colorScheme.onSurface,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            currentYearContentColor = MaterialTheme.colorScheme.primary,
                            selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                            dayContentColor = MaterialTheme.colorScheme.onSurface,
                            disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledSelectedDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            disabledSelectedDayContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
    
    // Update callback when date changes
    datePickerState.selectedDateMillis?.let { millis ->
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val formattedDate = formatter.format(Date(millis))
        if (formattedDate != value) {
            onDateSelected(formattedDate)
        }
    }
}

/**
 * Utility function to convert milliseconds to date string
 * Copied from standards logic
 */
fun convertMillisToDate(millis: Long, format: String = "MM/dd/yyyy"): String {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.format(Date(millis))
}