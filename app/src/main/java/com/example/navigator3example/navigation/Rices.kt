package com.example.navigator3example.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RiceTestData(
    val date: String,
    val testNumber: Int,
    val rice: Double,
    val pcf: Double,
    val dryWeightA: Double = 0.0,
    val dryWeightB: Double = 0.0,
    val wetWeightA: Double = 0.0,
    val wetWeightB: Double = 0.0,
    val calibration: String = "Calibrate 1",
    val riceA: Double = 2.452,
    val riceB: Double = 2.452,
    val ricePcfA: Double = 153.2,
    val ricePcfB: Double = 153.2
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiceTests() {
    // State for HMA rice test input
    var selectedCalibrate by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var testDate by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var dryWeightA by rememberSaveable { mutableStateOf("") }
    var dryWeightB by rememberSaveable { mutableStateOf("") }
    var wetWeightA by rememberSaveable { mutableStateOf("") }
    var wetWeightB by rememberSaveable { mutableStateOf("") }
    
    // State for tracking expanded rice cards
    var expandedCards by rememberSaveable { mutableStateOf(setOf<Int>()) }

    // Calibrate options for dropdown
    val calibrateOptions = listOf("Calibrate 1", "Calibrate 2", "Calibrate 3", "Calibrate 4", "Calibrate 5")

    // Sample data for previous rice tests
    val previousTests = remember {
        (1..10).map { index ->
            RiceTestData(
                date = "2025-08-${String.format("%02d", index)}",
                testNumber = index,
                rice = 2.400 + (index * 0.01),
                pcf = 150.0 + (index * 2.5),
                dryWeightA = 100.0 + (index * 1.5),
                dryWeightB = 101.0 + (index * 1.2),
                wetWeightA = 120.0 + (index * 2.0),
                wetWeightB = 121.5 + (index * 1.8),
                calibration = "Calibrate ${(index % 5) + 1}",
                riceA = 2.450 + (index * 0.002),
                riceB = 2.454 + (index * 0.003),
                ricePcfA = 152.0 + (index * 1.1),
                ricePcfB = 154.0 + (index * 1.3)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Panel - HMA Rice Test Input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "🌾 HMA Rice Test",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    // Date Picker Button
                    OutlinedTextField(
                        value = if (testDate.isEmpty()) "Select Date" else testDate,
                        onValueChange = { },
                        label = { Text("Date") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    )
                    
                    // Docked Date Picker
                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { showDatePicker = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                    TextButton(
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                testDate = formatter.format(Date(millis))
                                            }
                                            showDatePicker = false
                                        }
                                    ) {
                                        Text("OK")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    // Calibrate Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCalibrate,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Select Calibration:") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            calibrateOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedCalibrate = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                item{
                    Row {
                        Column (modifier = Modifier.fillMaxWidth(.48f)) {
                            Text("Rice A: 2.452")
                            Text("Rice B: 2.452")
                        }
                        Column (
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Rice A PCF: 153.2: ")
                            Text("Rice B PCF: 153.2: ")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Average Rice: 2.453")
                    Text("Average PCF: 153.2")
                }

                //User input Rice Test
                item {
                    Row {
                        OutlinedTextField(
                            value = dryWeightA,
                            onValueChange = { dryWeightA = it },
                            label = { Text("Dry Weight A") },
                            modifier = Modifier.fillMaxWidth(.48f)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        OutlinedTextField(
                            value = dryWeightB,
                            onValueChange = { dryWeightB = it },
                            label = { Text("Dry Weight B") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row {
                        OutlinedTextField(
                            value = wetWeightA,
                            onValueChange = { wetWeightA = it },
                            label = { Text("Wet Weight A") },
                            modifier = Modifier.fillMaxWidth(.48f)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        OutlinedTextField(
                            value = wetWeightB,
                            onValueChange = { wetWeightB = it },
                            label = { Text("Wet Weight B") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                }

                item {
                    Button(
                        onClick = { /* TODO: Save test */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Rice Test")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Panel - Previous Rice Tests (Scrollable)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Previous Rice Tests",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(previousTests.size) { index ->
                        val testData = previousTests[index]
                        val isExpanded = expandedCards.contains(testData.testNumber)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedCards = if (isExpanded) {
                                        expandedCards - testData.testNumber
                                    } else {
                                        expandedCards + testData.testNumber
                                    }
                                },
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Rice Test #${testData.testNumber}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Date: ${testData.date}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Rice: ${String.format("%.3f", testData.rice)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "PCF: ${String.format("%.1f", testData.pcf)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                // Expanded content with animation
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "Detailed Test Data",
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        
                                        Text(
                                            text = "Calibration: ${testData.calibration}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Dry Weights:",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "A: ${String.format("%.1f", testData.dryWeightA)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "B: ${String.format("%.1f", testData.dryWeightB)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "Wet Weights:",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "A: ${String.format("%.1f", testData.wetWeightA)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "B: ${String.format("%.1f", testData.wetWeightB)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Rice Values:",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "A: ${String.format("%.3f", testData.riceA)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "B: ${String.format("%.3f", testData.riceB)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "PCF Values:",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "A: ${String.format("%.1f", testData.ricePcfA)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "B: ${String.format("%.1f", testData.ricePcfB)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RiceTestsPreview() {
    RiceTests()
}