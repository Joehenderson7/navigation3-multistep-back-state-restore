package com.example.navigator3example.navigation

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiceTests() {
    // State for HMA rice test input
    var selectedCalibrate by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var testDate by rememberSaveable { mutableStateOf("") }
    var dryWeightA by rememberSaveable { mutableStateOf("") }
    var dryWeightB by rememberSaveable { mutableStateOf("") }
    var wetWeightA by rememberSaveable { mutableStateOf("") }
    var wetWeightB by rememberSaveable { mutableStateOf("") }

    // Calibrate options for dropdown
    val calibrateOptions = listOf("Calibrate 1", "Calibrate 2", "Calibrate 3", "Calibrate 4", "Calibrate 5")

    // Sample data for previous rice tests
    val previousTests = remember {
        (1..10).map { index ->
            "Rice Test #$index" to "Moisture: ${12 + index}%, Temp: ${20 + index}°C, Duration: ${30 + index}min"
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
                    OutlinedTextField(
                        value = testDate,
                        onValueChange = { testDate = it },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        Row (){
                            Text("Rice A: 2.452")
                        }

                        Spacer(modifier = Modifier.width(105.dp))
                        Text("Average Rice: 2.425")
                    }

                    Row {
                        Text("Rice B: ")
                        Spacer(modifier = Modifier.width(145.dp))
                        Text("Rice B: ")
                    }
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
                        val (title, description) = previousTests[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = description,
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

@Preview
@Composable
fun RiceTestsPreview() {
    RiceTests()
}