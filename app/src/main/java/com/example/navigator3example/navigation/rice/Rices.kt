package com.example.navigator3example.navigation.rice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.navigator3example.calculations.CalculateRice
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.navigator3example.ui.components.MaterialDateTimePicker
import androidx.activity.compose.BackHandler

data class RiceTestData(
    val date: String,
    val testNumber: Int,
    val rice: Float,
    val pcf: Float,
    val dryWeightA: Float = 0.0f,
    val dryWeightB: Float = 0.0f,
    val wetWeightA: Float = 0.0f,
    val wetWeightB: Float = 0.0f,
    val calibration: String = "Calibrate 1",
    val riceA: Float = 2.452f,
    val riceB: Float = 2.452f,
    val ricePcfA: Float = 153.2f,
    val ricePcfB: Float = 153.2f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiceTests() {
    // Sub-navigation state: false = main Rice screen, true = new calibration screen
    var showNewCalibration by rememberSaveable { mutableStateOf(false) }

    // Back handling analogous to Navigation 3 back behavior for sub-route
    BackHandler(enabled = showNewCalibration) {
        showNewCalibration = false
    }

    // State for HMA rice test input
    var selectedCalibrate by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var dryWeightA by rememberSaveable { mutableStateOf("") }
    var dryWeightB by rememberSaveable { mutableStateOf("") }
    var wetWeightA by rememberSaveable { mutableStateOf("") }
    var wetWeightB by rememberSaveable { mutableStateOf("") }

    //UI values
    var isAverageOfTwo by rememberSaveable { mutableStateOf(true) }
    var isFormValid by rememberSaveable { mutableStateOf(false) }

    //Date Picker
    val datePickerState = rememberDatePickerState()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var testDate by rememberSaveable { mutableStateOf("") }

    // Rice Test Calculations
    var riceA by rememberSaveable { mutableStateOf(0.0f) }
    var riceB by rememberSaveable { mutableStateOf(0.0f) }
    var calA by rememberSaveable { mutableStateOf(0.0f) }
    var calB by rememberSaveable { mutableStateOf(0.0f) }
    var pcfA by rememberSaveable { mutableStateOf(0.0f) }
    var pcfB by rememberSaveable { mutableStateOf(0.0f) }
    var averageRice by rememberSaveable { mutableStateOf(0.0f) }
    var averagePCF by rememberSaveable { mutableStateOf(0.0f) }

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
                rice = 2.400f + (index * 0.01f),
                pcf = 150.0f + (index * 2.5f),
                dryWeightA = 100.0f + (index * 1.5f),
                dryWeightB = 101.0f + (index * 1.2f),
                wetWeightA = 120.0f + (index * 2.0f),
                wetWeightB = 121.5f + (index * 1.8f),
                calibration = "Calibrate ${(index % 5) + 1}",
                riceA = 2.450f + (index * 0.002f),
                riceB = 2.454f + (index * 0.003f),
                ricePcfA = 152.0f + (index * 1.1f),
                ricePcfB = 154.0f + (index * 1.3f)
            )
        }
    }

    AnimatedContent(
        targetState = showNewCalibration,
        transitionSpec = {
            // Mirror NavHost tab animations, but direction depends on navigation flow
            if (targetState && !initialState) {
                // Navigating forward to calibration -> slide left
                slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            } else {
                // Navigating back to main -> slide right
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        },
        label = "rice_sub_nav"
    ) { showCal ->
        if (showCal) {
            // New Calibration screen
            NewRiceCalibrationScreen()
        } else {
            // Main Rice screen content
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ){
                                Column {
                                    Text(
                                        "🌾 HMA Rice Test",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Column {
                                    AverageOfTwoToggle(
                                        isChecked = isAverageOfTwo,
                                        onCheckedChange = { isAverageOfTwo = it },
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }

                            }

                            
                            // Material Date Time Picker (based on standards logic)
                            MaterialDateTimePicker(
                                value = testDate,
                                onDateSelected = { selectedDate ->
                                    testDate = selectedDate
                                },
                                label = "Date",
                                placeholder = "Select Date",
                                dateFormat = "yyyy-MM-dd",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Row {
                                Column {
                                    // Calibrate Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded },
                                        modifier = Modifier.fillMaxWidth(.85f)
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
                                Column(
                                    modifier = Modifier.align(CenterVertically)
                                ) {
                                    ButtonAddNewCalibration(
                                        modifier = Modifier.align(Alignment.End)
                                            .fillMaxSize(),
                                        onClick = { showNewCalibration = true },
                                    )
                                }
                            }

                        }
                        item{
                            if(isAverageOfTwo){
                                Row {
                                    Column (modifier = Modifier.fillMaxWidth(.48f)) {
                                        Text("Rice A: $riceA")
                                        Text("Rice B: $riceB")
                                    }
                                    Column (
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Rice A PCF: $pcfA: ")
                                        Text("Rice B PCF: $pcfB: ")
                                    }
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
                                    label = { Text("Dry Weight A") },
                                    modifier = Modifier.fillMaxWidth(.48f),
                                    onValueChange = { dryWeightA = it },
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                OutlinedTextField(
                                    value = wetWeightA,
                                    label = { Text("Wet Weight A") },
                                    modifier = Modifier.fillMaxWidth(),
                                    onValueChange = {
                                        wetWeightA = it
                                        riceA = CalculateRice(dryWeightA.toFloat(), wetWeightA.toFloat(), calA)
                                                    },
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth(),

                            ) {
                                if(isAverageOfTwo){
                                    OutlinedTextField(
                                        value = dryWeightB,
                                        onValueChange = { dryWeightB = it },
                                        label = { Text("Dry Weight B") },
                                        modifier = Modifier.fillMaxWidth(.48f),
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
    }
}
@Composable
fun ButtonAddNewCalibration(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
){
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = true,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        content = {
            Text(
                text = "+",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(CenterVertically)
            )
        }
    )
}

@Composable
fun AverageOfTwoToggle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        thumbContent = {Text("Average of Two")},
        colors = SwitchDefaults.colors(),
        enabled = true,
        modifier = modifier
    )
}

@Preview
@Composable
fun RiceTestsPreview() {
    RiceTests()
}