package com.example.navigator3example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.Add
import com.example.navigator3example.data.StandardDatabase
import com.example.navigator3example.data.StandardRepository
import com.example.navigator3example.data.StandardEntity

data class Standard @OptIn(ExperimentalMaterial3Api::class)
constructor(
    val date: Long?, 
    val ds: Int, 
    val ms: Int, 
    val serialNumber: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class StandardAnalysis(
    val standard: StandardEntity,
    val densityDifference: Double?,
    val moistureDifference: Double?,
    val densityPassFail: PassFailStatus,
    val moisturePassFail: PassFailStatus,
    val previousStandardsCount: Int
)

enum class PassFailStatus {
    PASS, FAIL, INSUFFICIENT_DATA
}

// Statistical calculation utilities
object StandardStatistics {
    
    fun calculateAverage(values: List<Int>): Double {
        return if (values.isNotEmpty()) values.average() else 0.0
    }
    
    fun calculateRelativeDifference(current: Int, average: Double): Double {
        return if (average != 0.0) ((current - average) / average) * 100 else 0.0
    }
    
    fun checkDensityPassFail(difference: Double?): PassFailStatus {
        return when {
            difference == null -> PassFailStatus.INSUFFICIENT_DATA
            kotlin.math.abs(difference) <= 1.0 -> PassFailStatus.PASS
            else -> PassFailStatus.FAIL
        }
    }
    
    fun checkMoisturePassFail(difference: Double?): PassFailStatus {
        return when {
            difference == null -> PassFailStatus.INSUFFICIENT_DATA
            kotlin.math.abs(difference) <= 2.0 -> PassFailStatus.PASS
            else -> PassFailStatus.FAIL
        }
    }
    
    suspend fun analyzeStandard(
        standard: StandardEntity,
        repository: StandardRepository
    ): StandardAnalysis {
        val previousStandards = repository.getPreviousNStandards(
            standard.serialNumber,
            standard.timestamp,
            4
        )
        
        val densityDifference = if (previousStandards.isNotEmpty()) {
            val avgDensity = calculateAverage(previousStandards.map { it.densityCount })
            calculateRelativeDifference(standard.densityCount, avgDensity)
        } else null
        
        val moistureDifference = if (previousStandards.isNotEmpty()) {
            val avgMoisture = calculateAverage(previousStandards.map { it.moistureCount })
            calculateRelativeDifference(standard.moistureCount, avgMoisture)
        } else null
        
        return StandardAnalysis(
            standard = standard,
            densityDifference = densityDifference,
            moistureDifference = moistureDifference,
            densityPassFail = checkDensityPassFail(densityDifference),
            moisturePassFail = checkMoisturePassFail(moistureDifference),
            previousStandardsCount = previousStandards.size
        )
    }
}

// Utility function to convert milliseconds to date string
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardsScreen(standard: Standard){
    val context = LocalContext.current
    val database = StandardDatabase.getDatabase(context)
    val repository = StandardRepository(database.standardDao())
    val coroutineScope = rememberCoroutineScope()
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = standard.date)
    var densityCount by remember { mutableStateOf(standard.ds.toString()) }
    var moistureCount by remember { mutableStateOf(standard.ms.toString()) }
    var gaugeSerialNumber by remember { mutableStateOf(standard.serialNumber) }
    var saveMessage by remember { mutableStateOf("") }
    
    // Observe saved standards from database
    val savedStandards by repository.getAllStandards().collectAsState(initial = emptyList())
    var showAllStandards by remember { mutableStateOf(false) }
    var standardAnalyses by remember { mutableStateOf<List<StandardAnalysis>>(emptyList()) }
    var showFabMenu by remember { mutableStateOf(false) }
    
    // Analyze standards when data changes
    LaunchedEffect(savedStandards) {
        standardAnalyses = savedStandards.map { standard ->
            StandardStatistics.analyzeStandard(standard, repository)
        }
    }

    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Date Input Field
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { },
                    label = { Text("Date") },
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
                                showModeToggle = false
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = gaugeSerialNumber,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        gaugeSerialNumber = newValue
                    }
                },
                label = { Text("Gauge Serial Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Density Count Input Field
                OutlinedTextField(
                    value = densityCount,
                    onValueChange = { newValue ->
                        // Only allow numeric input
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            densityCount = newValue
                        }
                    },
                    label = { Text("Density Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(.47f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Moisture Count Input Field
                OutlinedTextField(
                    value = moistureCount,
                    onValueChange = { newValue ->
                        // Only allow numeric input
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            moistureCount = newValue
                        }
                    },
                    label = { Text("Moisture Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save Button
            Button(
                onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null && 
                        gaugeSerialNumber.isNotEmpty() && 
                        densityCount.isNotEmpty() && 
                        moistureCount.isNotEmpty()) {
                        
                        coroutineScope.launch {
                            try {
                                repository.insertStandard(
                                    serialNumber = gaugeSerialNumber,
                                    date = selectedDateMillis,
                                    densityCount = densityCount.toInt(),
                                    moistureCount = moistureCount.toInt()
                                )
                                saveMessage = "Standard saved successfully!"
                                // Clear fields after saving
                                gaugeSerialNumber = ""
                                densityCount = ""
                                moistureCount = ""
                            } catch (e: Exception) {
                                saveMessage = "Error saving standard: ${e.message}"
                            }
                        }
                    } else {
                        saveMessage = "Please fill all fields"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Standard")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Save message
            if (saveMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = saveMessage,
                    color = if (saveMessage.contains("Error") || saveMessage.contains("Please fill")) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
            
            // Display saved standards
            if (standardAnalyses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                val displayedAnalyses = if (showAllStandards) {
                    standardAnalyses
                } else {
                    standardAnalyses.take(4)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Standards (${displayedAnalyses.size}/${standardAnalyses.size})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (standardAnalyses.size > 4) {
                        Button(
                            onClick = { showAllStandards = !showAllStandards }
                        ) {
                            Text(if (showAllStandards) "Show Less" else "Show All")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(displayedAnalyses) { analysis ->
                        StandardCard(analysis = analysis)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Floating Action Button with docked popup menu
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showFabMenu = !showFabMenu }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Menu"
                )
            }
            
            // Dropdown menu docked to FAB
            DropdownMenu(
                expanded = showFabMenu,
                onDismissRequest = { showFabMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Generate Test Data") },
                    onClick = {
                        showFabMenu = false
                        // TODO: Implement generate test data functionality
                    }
                )
            }
        }
    }
}

@Composable
fun StandardCard(analysis: StandardAnalysis) {
    val standard = analysis.standard
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Date: ${convertMillisToDate(standard.date)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Density with analysis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DS: ${standard.densityCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    if (analysis.densityDifference != null) {
                        Text(
                            text = "${String.format("%.1f", analysis.densityDifference)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (analysis.densityPassFail) {
                                PassFailStatus.PASS -> MaterialTheme.colorScheme.primary
                                PassFailStatus.FAIL -> MaterialTheme.colorScheme.error
                                PassFailStatus.INSUFFICIENT_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (analysis.densityPassFail) {
                                PassFailStatus.PASS -> "PASS"
                                PassFailStatus.FAIL -> "FAIL"
                                PassFailStatus.INSUFFICIENT_DATA -> "N/A"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = when (analysis.densityPassFail) {
                                PassFailStatus.PASS -> MaterialTheme.colorScheme.primary
                                PassFailStatus.FAIL -> MaterialTheme.colorScheme.error
                                PassFailStatus.INSUFFICIENT_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            
            // Moisture with analysis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MS: ${standard.moistureCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    if (analysis.moistureDifference != null) {
                        Text(
                            text = "${String.format("%.1f", analysis.moistureDifference)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (analysis.moisturePassFail) {
                                PassFailStatus.PASS -> MaterialTheme.colorScheme.primary
                                PassFailStatus.FAIL -> MaterialTheme.colorScheme.error
                                PassFailStatus.INSUFFICIENT_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (analysis.moisturePassFail) {
                                PassFailStatus.PASS -> "PASS"
                                PassFailStatus.FAIL -> "FAIL"
                                PassFailStatus.INSUFFICIENT_DATA -> "N/A"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = when (analysis.moisturePassFail) {
                                PassFailStatus.PASS -> MaterialTheme.colorScheme.primary
                                PassFailStatus.FAIL -> MaterialTheme.colorScheme.error
                                PassFailStatus.INSUFFICIENT_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun StandardsScreenPreview() {
    StandardsScreen(Standard(null, 10, 10))
}