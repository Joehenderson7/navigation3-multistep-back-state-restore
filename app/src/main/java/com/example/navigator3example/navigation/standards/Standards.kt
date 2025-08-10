package com.example.navigator3example.navigation.standards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.text.style.TextAlign
import com.example.navigator3example.data.standards.StandardDatabase
import com.example.navigator3example.data.standards.StandardRepository
import com.example.navigator3example.data.standards.StandardEntity
import com.example.navigator3example.ui.components.MaterialDateTimePicker
import kotlin.math.abs
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.example.navigator3example.data.nuke.NukeGaugeDatabase
import com.example.navigator3example.data.nuke.NukeGaugeRepository

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
            abs(difference) <= 1.0 -> PassFailStatus.PASS
            else -> PassFailStatus.FAIL
        }
    }
    
    fun checkMoisturePassFail(difference: Double?): PassFailStatus {
        return when {
            difference == null -> PassFailStatus.INSUFFICIENT_DATA
            abs(difference) <= 2.0 -> PassFailStatus.PASS
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

    // Preferences: last selected gauge serial number
    val prefs = remember { com.example.navigator3example.data.preferences.PreferencesManager.get(context) }
    val savedSerialFromPrefs by prefs.gaugeSerialNumber.collectAsState(initial = "")
    
    var selectedDateMillis by rememberSaveable { mutableStateOf<Long?>(standard.date) }
    var densityCount by rememberSaveable { mutableStateOf(standard.ds.toString()) }
    var moistureCount by rememberSaveable { mutableStateOf(standard.ms.toString()) }
    var gaugeSerialNumber by rememberSaveable { mutableStateOf(standard.serialNumber) }
    var saveMessage by rememberSaveable { mutableStateOf("") }

    // If local state is empty, default to last saved serial
    LaunchedEffect(savedSerialFromPrefs, gaugeSerialNumber) {
        if (gaugeSerialNumber.isEmpty() && savedSerialFromPrefs.isNotEmpty()) {
            gaugeSerialNumber = savedSerialFromPrefs
        }
    }
    
    // Point-of-input validation states
    var densityError by rememberSaveable { mutableStateOf<String?>(null) }
    var moistureError by rememberSaveable { mutableStateOf<String?>(null) }

    fun validateDensity(input: String) {
        val s = input.trim()
        densityError = when {
            s.isEmpty() -> "Required"
            !s.all { it.isDigit() } -> "Digits only"
            s.length > 6 -> "Too many digits"
            else -> null
        }
    }
    fun validateMoisture(input: String) {
        val s = input.trim()
        moistureError = when {
            s.isEmpty() -> "Required"
            !s.all { it.isDigit() } -> "Digits only"
            s.length > 6 -> "Too many digits"
            else -> null
        }
    }
    
    // Observe standards filtered by selected gauge serial number
    val standardsFlow = remember(gaugeSerialNumber) {
        if (gaugeSerialNumber.isNotEmpty()) repository.getStandardsBySerialNumberFlow(gaugeSerialNumber)
        else kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val savedStandards by standardsFlow.collectAsState(initial = emptyList())
    var showAllStandards by rememberSaveable { mutableStateOf(false) }
    var standardAnalyses by remember { mutableStateOf<List<StandardAnalysis>>(emptyList()) }
    var showNukeGauge by rememberSaveable { mutableStateOf(false) }

    // Inline sub-navigation for Nuke Gauge screen (Dialog fullscreen)
    androidx.activity.compose.BackHandler(enabled = showNukeGauge) {
        showNukeGauge = false
    }
    if (showNukeGauge) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showNukeGauge = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
                NukeGaugeScreen()
            }
        }
    }
    
    // Analyze standards when data changes
    LaunchedEffect(savedStandards) {
        standardAnalyses = savedStandards.map { standard ->
            StandardStatistics.analyzeStandard(standard, repository)
        }
    }

    val selectedDate = selectedDateMillis?.let {
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "New Standard", style = MaterialTheme.typography.titleMedium)

                    // Material Date Time Picker (based on standards logic with theme influence)
                    MaterialDateTimePicker(
                        value = selectedDate,
                        onDateSelected = { newDate ->
                            val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            try {
                                val date = formatter.parse(newDate)
                                selectedDateMillis = date?.time
                            } catch (e: Exception) {
                                selectedDateMillis = null
                            }
                        },
                        label = "Date",
                        placeholder = "Select Date",
                        initialDateMillis = selectedDateMillis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row{
                        // Gauge dropdown populated from Room NukeGauge database
                        GaugeSerialDropdown(
                            selected = gaugeSerialNumber,
                            onSelectedChange = { newSerial ->
                                gaugeSerialNumber = newSerial
                                coroutineScope.launch { prefs.setGaugeSerialNumber(newSerial) }
                            },
                            modifier = Modifier.fillMaxWidth(.85f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        Column (modifier = Modifier.align(CenterVertically)){
                            ButtonAddNewNukeGauge (
                                modifier = Modifier.fillMaxWidth(),
                            ){ showNukeGauge = true }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Density Count Input Field
                        OutlinedTextField(
                            value = densityCount,
                            onValueChange = { newValue ->
                                // Point-of-input validation: digits only and length cap
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    densityCount = newValue
                                    validateDensity(newValue)
                                } else {
                                    densityError = "Digits only"
                                }
                            },
                            isError = densityError != null,
                            supportingText = {
                                if (densityError != null) Text(densityError!!)
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
                                // Point-of-input validation: digits only and length cap
                                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                    moistureCount = newValue
                                    validateMoisture(newValue)
                                } else {
                                    moistureError = "Digits only"
                                }
                            },
                            isError = moistureError != null,
                            supportingText = {
                                if (moistureError != null) Text(moistureError!!)
                            },
                            label = { Text("Moisture Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Save Button
                    val canSave = gaugeSerialNumber.isNotEmpty() &&
                        densityCount.isNotEmpty() && moistureCount.isNotEmpty() &&
                        densityError == null && moistureError == null

                    Button(
                        onClick = {
                            val selectedDate = selectedDateMillis
                            if (selectedDate != null && canSave) {
                                coroutineScope.launch {
                                    try {
                                        repository.insertStandard(
                                            serialNumber = gaugeSerialNumber,
                                            date = selectedDate, 
                                            densityCount = densityCount.toInt(),
                                            moistureCount = moistureCount.toInt()
                                        )
                                        saveMessage = "Standard saved successfully!"
                                        // Clear fields after saving
                                        gaugeSerialNumber = ""
                                        densityCount = ""
                                        moistureCount = ""
                                        densityError = null
                                        moistureError = null
                                    } catch (e: Exception) {
                                        saveMessage = "Error saving standard: ${'$'}{e.message}"
                                    }
                                }
                            } else {
                                // Trigger validations to show messages
                                validateDensity(densityCount)
                                validateMoisture(moistureCount)
                                saveMessage = "Please correct the highlighted fields"
                            }
                        },
                        enabled = canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Standard")
                    }

                    // Save message
                    if (saveMessage.isNotEmpty()) {
                        Text(
                            text = saveMessage,
                            color = if (saveMessage.contains("Error") || saveMessage.contains("Please fill")) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Display saved standards
            if (standardAnalyses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                                text = "Saved Standards (${displayedAnalyses.size}/${standardAnalyses.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (standardAnalyses.size > 4) {
                                Button(onClick = { showAllStandards = !showAllStandards }) {
                                    Text(if (showAllStandards) "Show Less" else "Show All")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.material3.Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            items(displayedAnalyses) { analysis ->
                                StandardCard(analysis = analysis)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
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
@Composable
fun ButtonAddNewNukeGauge(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
){
    Column (
        modifier = modifier.fillMaxWidth(),
    ) {
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
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaugeSerialDropdown(
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val db = remember { NukeGaugeDatabase.getDatabase(context) }
    val repo = remember { NukeGaugeRepository(db.nukeGaugeDao()) }
    val serialsFlow = remember { repo.getAllSerialNumbers() }
    val serials by serialsFlow.collectAsState(initial = emptyList())
    var expanded by rememberSaveable { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gauge Serial Number") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            serials.forEach { sn ->
                DropdownMenuItem(
                    text = { Text(sn) },
                    onClick = {
                        onSelectedChange(sn)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun StandardsScreenPreview() {
    StandardsScreen(Standard(null, 10, 10))
}