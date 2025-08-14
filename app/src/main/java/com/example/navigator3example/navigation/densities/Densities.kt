package com.example.navigator3example.navigation.densities

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.navigator3example.calculations.RiceToPCF
import com.example.navigator3example.data.densities.DensityTestDatabase
import com.example.navigator3example.data.densities.DensityTestRepository
import com.example.navigator3example.data.preferences.PreferencesManager
import com.example.navigator3example.data.rice.RiceDatabase
import com.example.navigator3example.data.rice.RiceRepository
import com.example.navigator3example.ui.components.MaterialDateTimePicker
import com.example.navigator3example.ui.components.SlidingPanels
import com.example.navigator3example.ui.components.convertMillisToDate
import kotlinx.coroutines.launch

private fun riceAverage(rice: com.example.navigator3example.data.rice.RiceEntity): Double? {
    val a = com.example.navigator3example.calculations.CalculateRice(
        rice.dryWeightA,
        rice.wetWeightA,
        rice.calibration.weightA
    )
    val b = com.example.navigator3example.calculations.CalculateRice(
        rice.dryWeightB,
        rice.wetWeightB,
        rice.calibration.weightB
    )
    if (a.isNaN() || b.isNaN()) return null
    return ((a + b) / 2f).toDouble()
}

@Composable
fun Densities() {
    SlidingPanels(
        modifier = Modifier.fillMaxSize(),
        bottomTitle = "Density Tests",
        topTitle = "New Density Test",
        topContent = {
            NuclearDensityInputScreen(onViewAll = { /* Drag up the bottom panel to view all */ })
        },
        bottomContent = {
            DensitiesListScreen(onBack = { /* Bottom panel is draggable; no explicit back */ })
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuclearDensityInputScreen(onViewAll: () -> Unit) {
    val TAG = "Densities"
    // Date selection state (top of screen)
    var testDate by rememberSaveable { mutableStateOf("") }

    // Inputs per requirement
    var testNumber by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var offSet by rememberSaveable { mutableStateOf("") }
    var wet1 by rememberSaveable { mutableStateOf("") }
    var wet2 by rememberSaveable { mutableStateOf("") }
    var wet3 by rememberSaveable { mutableStateOf("") }
    var wet4 by rememberSaveable { mutableStateOf("") }
    var correctionFactor by rememberSaveable { mutableStateOf("") }

    // Rice dropdown state
    var riceExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedRiceId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Load rice list from Room and Preferences
    val context = androidx.compose.ui.platform.LocalContext.current
    val riceDb = remember { RiceDatabase.getDatabase(context) }
    val riceRepo = remember { RiceRepository(riceDb.riceDao()) }
    val prefs = remember { PreferencesManager.get(context) }
    val storedCorrectionFactor by prefs.correctionFactor.collectAsState(initial = "")
    val scope = rememberCoroutineScope()

    // Density tests repository (Room)
    val densityDb = remember { DensityTestDatabase.getDatabase(context) }
    val densityRepo = remember { DensityTestRepository(densityDb.densityTestDao()) }

    // Focus management
    val focusManager = LocalFocusManager.current
    val locationFocus = remember { FocusRequester() }
    val offsetFocus = remember { FocusRequester() }
    val correctionFocus = remember { FocusRequester() }
    val wet1Focus = remember { FocusRequester() }
    val wet2Focus = remember { FocusRequester() }
    val wet3Focus = remember { FocusRequester() }
    val wet4Focus = remember { FocusRequester() }

    // Input validation patterns
    val intPattern = remember { Regex("^\\d*") } // digits only
    val decimal2Pattern = remember { Regex("^-?\\d*(?:\\.\\d{0,2})?") } // up to 2 decimals
    val decimalUnsigned2Pattern = remember { Regex("^\\d*(?:\\.\\d{0,2})?") } // up to 2 decimals, no sign (wet densities)
    val decimal3Pattern = remember { Regex("^-?\\d*(?:\\.\\d{0,3})?") } // up to 3 decimals (correction factor)

    // Initialize local correctionFactor from stored preference once
    LaunchedEffect(storedCorrectionFactor) {
        if (correctionFactor.isEmpty() && storedCorrectionFactor.isNotEmpty()) {
            correctionFactor = storedCorrectionFactor
        }
    }

    // Prefill next test number from preferences (avoid resetting to 1 on first composition)
    var hasInitializedTestNumber by rememberSaveable { mutableStateOf(false) }
    val nextNumberPref by prefs.nextDensityTestNumber.collectAsState(initial = -1)
    LaunchedEffect(nextNumberPref) {
        if (!hasInitializedTestNumber && nextNumberPref > 0) {
            Log.d(TAG, "Loaded next density test number from prefs: $nextNumberPref")
            testNumber = nextNumberPref.toString()
            hasInitializedTestNumber = true
        }
    }

    val rices by riceRepo.getAllRices().collectAsState(initial = emptyList())
    val selectedRice = rices.firstOrNull { it.id == selectedRiceId }

    // Calculations
    val wetValues = listOf(wet1, wet2, wet3, wet4).mapNotNull { it.toDoubleOrNull() }
    val averageWet = remember(wetValues) {
        if (wetValues.isNotEmpty()) wetValues.average() else null
    }
    val cf = correctionFactor.toDoubleOrNull()
    val correctedAverage = remember(averageWet, cf) {
        if (averageWet != null && cf != null) averageWet + cf else null
    }
    val riceAvg = selectedRice?.let { riceAverage(it) }
    val ricePcf = riceAvg?.let { RiceToPCF(it.toFloat()).toDouble() }
    val percentCompaction = remember(correctedAverage, ricePcf) {
        if (correctedAverage != null && ricePcf != null && ricePcf > 0.0) correctedAverage / ricePcf * 100.0 else null
    }

    // Validation derived states
    val testNumberError by remember { derivedStateOf { testNumber.isBlank() || testNumber.toIntOrNull() == null } }
    val testDateError by remember { derivedStateOf { testDate.isBlank() } }
    val correctionError by remember { derivedStateOf {
        if (correctionFactor.isBlank()) false
        else correctionFactor.toDoubleOrNull() == null && !(correctionFactor.endsWith(".") || correctionFactor == "-")
    } }
    val wet1Error by remember { derivedStateOf { wet1.isNotBlank() && !wet1.endsWith('.') && wet1.toDoubleOrNull() == null } }
    val wet2Error by remember { derivedStateOf { wet2.isNotBlank() && !wet2.endsWith('.') && wet2.toDoubleOrNull() == null } }
    val wet3Error by remember { derivedStateOf { wet3.isNotBlank() && !wet3.endsWith('.') && wet3.toDoubleOrNull() == null } }
    val wet4Error by remember { derivedStateOf { wet4.isNotBlank() && !wet4.endsWith('.') && wet4.toDoubleOrNull() == null } }
    val formValid by remember { derivedStateOf { !testNumberError && !testDateError && !correctionError } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Test Number
                    OutlinedTextField(
                        value = testNumber,
                        onValueChange = { new ->
                            intPattern.find(new)?.value?.let { testNumber = it }
                        },
                        label = { Text("Test Number") },
                        singleLine = true,
                        isError = testNumberError,
                        supportingText = {
                            if (testNumberError) Text("Required. Use digits only.")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { locationFocus.requestFocus() }),
                        modifier = Modifier.weight(1f)
                    )

                    // Date
                    MaterialDateTimePicker(
                        value = testDate,
                        onDateSelected = { testDate = it },
                        label = "Test Date",
                        placeholder = "Select Date",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / Sta") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { offsetFocus.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(locationFocus)
                    )
                    // Offset
                    OutlinedTextField(
                        value = offSet,
                        onValueChange = { offSet = it },
                        label = { Text("Offset") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { correctionFocus.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(offsetFocus)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Rice dropdown
                    ExposedDropdownMenuBox(
                        expanded = riceExpanded,
                        onExpandedChange = { riceExpanded = !riceExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedRice?.let { r ->
                                val dateStr = convertMillisToDate(r.date, "M/d")
                                val avg = riceAverage(r)?.let { String.format("%.3f", it) } ?: "—"
                                "$dateStr: $avg " + String.format("%.1f", ricePcf)
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Rice") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = riceExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .focusProperties { canFocus = false }
                        )
                        ExposedDropdownMenu(
                            expanded = riceExpanded,
                            onDismissRequest = { riceExpanded = false }
                        ) {
                            rices.forEach { rice ->
                                val pcf = riceAverage(rice)?.let { com.example.navigator3example.calculations.RiceToPCF(it.toFloat()) }
                                DropdownMenuItem(
                                    text = { Text(text = "${rice.name} • ${String.format("%.1f PCF", pcf)}") },
                                    onClick = {
                                        selectedRiceId = rice.id
                                        riceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = correctionFactor,
                        onValueChange = { newValue ->
                            decimal3Pattern.find(newValue)?.value?.let { filtered ->
                                if (filtered != correctionFactor) {
                                    correctionFactor = filtered
                                    // Persist the last used correction factor (only when valid number)
                                    filtered.toDoubleOrNull()?.let {
                                        scope.launch { prefs.setCorrectionFactor(filtered) }
                                    }
                                }
                            }
                        },
                        label = { Text("Correction Factor") },
                        singleLine = true,
                        isError = correctionError,
                        supportingText = {
                            if (correctionError) Text("Enter a valid number (e.g., 0.125)")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { wet1Focus.requestFocus() }),
                        modifier = Modifier
                            .weight(.5f)
                            .focusRequester(correctionFocus)
                    )
                }

                // Summary display (similar to Standards’ header card)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Average: ${averageWet?.let { String.format("%.2f", it) } ?: "—"}")
                        Text(text = "Corrected Avg: ${correctedAverage?.let { String.format("%.2f", it) } ?: "—"}")
                        val riceLabel = ricePcf?.let { String.format("%.1f PCF", it) } ?: "—"
                        Text(text = "% Compaction (vs Rice $riceLabel): ${percentCompaction?.let { String.format("%.1f%%", it) } ?: "—"}")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = wet1,
                        onValueChange = { new ->
                            decimalUnsigned2Pattern.find(new)?.value?.let { wet1 = it }
                        },
                        label = { Text("Wet Density 1") },
                        singleLine = true,
                        isError = wet1Error,
                        supportingText = { if (wet1Error) Text("Enter a valid number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { wet2Focus.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(wet1Focus)
                    )
                    OutlinedTextField(
                        value = wet2,
                        onValueChange = { new ->
                            decimalUnsigned2Pattern.find(new)?.value?.let { wet2 = it }
                        },
                        label = { Text("Wet Density 2") },
                        singleLine = true,
                        isError = wet2Error,
                        supportingText = { if (wet2Error) Text("Enter a valid number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { wet3Focus.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(wet2Focus)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = wet3,
                        onValueChange = { new ->
                            decimalUnsigned2Pattern.find(new)?.value?.let { wet3 = it }
                        },
                        label = { Text("Wet Density 3") },
                        singleLine = true,
                        isError = wet3Error,
                        supportingText = { if (wet3Error) Text("Enter a valid number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { wet4Focus.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(wet3Focus)
                    )
                    OutlinedTextField(
                        value = wet4,
                        onValueChange = { new ->
                            decimalUnsigned2Pattern.find(new)?.value?.let { wet4 = it }
                        },
                        label = { Text("Wet Density 4") },
                        singleLine = true,
                        isError = wet4Error,
                        supportingText = { if (wet4Error) Text("Enter a valid number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(wet4Focus)
                    )
                }
                // Save and navigate actions
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                val savedNumber = testNumber
                                densityRepo.insert(
                                    testNumber = savedNumber,
                                    testDate = testDate,
                                    location = location,
                                    offset = offSet,
                                    riceId = selectedRiceId,
                                    correctionFactor = correctionFactor.toDoubleOrNull(),
                                    wet1 = wet1.toDoubleOrNull(),
                                    wet2 = wet2.toDoubleOrNull(),
                                    wet3 = wet3.toDoubleOrNull(),
                                    wet4 = wet4.toDoubleOrNull(),
                                )

                                // Compute next number and persist exactly once
                                val next = (savedNumber.toIntOrNull()?.plus(1)) ?: 1
                                Log.d(TAG, "Saved density test number: $savedNumber; setting next to: $next")
                                prefs.setNextDensityTestNumber(next)

                                // Clear wet density inputs and increment the displayed test number
                                wet1 = ""
                                wet2 = ""
                                wet3 = ""
                                wet4 = ""
                                location = ""
                                offSet = ""
                                testNumber = next.toString()
                                // Optionally refocus to first wet density for rapid entry
                                wet1Focus.requestFocus()
                            }
                        },
                        enabled = formValid
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun DensitiesScreenPreview() {
    Densities()
}