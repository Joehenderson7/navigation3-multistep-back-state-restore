package com.example.navigator3example.navigation.rice

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
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
import com.example.navigator3example.calculations.RiceToPCF
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.navigator3example.ui.components.MaterialDateTimePicker
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.navigator3example.data.rice.RiceDatabase
import com.example.navigator3example.data.rice.RiceRepository
import com.example.navigator3example.data.rice.RiceCalibration
import com.example.navigator3example.data.rice.getAverageRice
import com.example.navigator3example.data.rice.getRiceA
import com.example.navigator3example.data.rice.getRiceB
import com.example.navigator3example.ui.components.convertMillisToDate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.navigator3example.data.preferences.PreferencesManager
import kotlinx.coroutines.launch

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection

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

    // Focus management for navigating between fields via IME actions
    val focusManager = LocalFocusManager.current
    val dryARequester = remember { FocusRequester() }
    val wetARequester = remember { FocusRequester() }
    val dryBRequester = remember { FocusRequester() }
    val wetBRequester = remember { FocusRequester() }

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

    // Track selected calibration object for saving
    var selectedCalibration by remember { mutableStateOf<RiceCalibration?>(null) }

    // State for tracking expanded rice cards
    var expandedCards by rememberSaveable { mutableStateOf(setOf<Long>()) }

    // Calibrate options from Room (RiceRepository)
    val context = LocalContext.current
    val riceDb = remember { RiceDatabase.getDatabase(context) }
    val riceRepo = remember { RiceRepository(riceDb.riceDao()) }
    val calibrationsFlow = remember { riceRepo.getAllCalibrations() }
    val calibrations by calibrationsFlow.collectAsState(initial = emptyList())

    // Preferences for remembering last selected calibration
    val prefs = remember { PreferencesManager.get(context) }
    val scope = rememberCoroutineScope()
    val lastCalId by prefs.lastRiceCalibrationId.collectAsState(initial = -1L)

    // Observe previous rice tests from database (ordered by date desc)
    val rices by riceRepo.getAllRices().collectAsState(initial = emptyList())

    // Restore last selected calibration when data is available and nothing selected yet
    LaunchedEffect(calibrations, lastCalId) {
        if (selectedCalibrate.isEmpty() && lastCalId != -1L) {
            calibrations.firstOrNull { it.id == lastCalId }?.let { cal ->
                val label = "${convertMillisToDate(cal.date)}  •  A: ${String.format(Locale.getDefault(), "%.3f", cal.weightA)}  •  B: ${String.format(Locale.getDefault(), "%.3f", cal.weightB)}"
                selectedCalibrate = label
                calA = cal.weightA
                calB = cal.weightB
                selectedCalibration = cal
            }
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
            RiceCalibrationScreen()
        } else {
            // Main Rice screen content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Validation helpers and state
                fun isDecimalInput(s: String): Boolean {
                    // Allow digits and at most one dot
                    return s.isEmpty() || s.all { it.isDigit() || it == '.' } && s.count { it == '.' } <= 1
                }
                var dryAError by rememberSaveable { mutableStateOf<String?>(null) }
                var wetAError by rememberSaveable { mutableStateOf<String?>(null) }
                var dryBError by rememberSaveable { mutableStateOf<String?>(null) }
                var wetBError by rememberSaveable { mutableStateOf<String?>(null) }
                
                fun validateRequiredDecimal(input: String): String? {
                    return when {
                        input.isEmpty() -> "Required"
                        !isDecimalInput(input) -> "Invalid number"
                        input == "." -> "Invalid number"
                        else -> null
                    }
                }
                // Top Panel - HMA Rice Test Input
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Column {
                                    AverageOfTwoToggle(
                                        isChecked = isAverageOfTwo,
                                        onCheckedChange = {
                                            isAverageOfTwo = it
                                            // Recalculate averages when mode changes
                                            val aValid = riceA.isFinite() && riceA > 0f
                                            val bValid = riceB.isFinite() && riceB > 0f
                                            pcfA = if (aValid) RiceToPCF(riceA) else 0f
                                            pcfB = if (bValid) RiceToPCF(riceB) else 0f
                                            val values = if (isAverageOfTwo) listOfNotNull(
                                                if (aValid) riceA else null,
                                                if (bValid) riceB else null
                                            ) else listOfNotNull(if (aValid) riceA else null)
                                            averageRice = if (values.isNotEmpty()) values.average().toFloat() else 0f
                                            averagePCF = if (averageRice > 0f) RiceToPCF(averageRice) else 0f
                                        },
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
                                            calibrations.forEach { cal ->
                                                val label = "${convertMillisToDate(cal.date)}  •  A: ${String.format(Locale.getDefault(), "%.3f", cal.weightA)}  •  B: ${String.format(Locale.getDefault(), "%.3f", cal.weightB)}"
                                                DropdownMenuItem(
                                                    text = { Text(label) },
                                                    onClick = {
                                                        selectedCalibrate = label
                                                        calA = cal.weightA
                                                        calB = cal.weightB
                                                        selectedCalibration = cal
                                                        // Save preference for last selected calibration
                                                        scope.launch { prefs.setLastRiceCalibrationId(cal.id) }
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
                                        Text("Rice A: ${String.format(Locale.getDefault(), "%.3f", riceA)}")
                                        Text("Rice B: ${String.format(Locale.getDefault(), "%.3f", riceB)}")
                                    }
                                    Column (
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Rice A PCF: ${String.format(Locale.getDefault(), "%.1f", pcfA)}")
                                        Text("Rice B PCF: ${String.format(Locale.getDefault(), "%.1f", pcfB)}")
                                    }
                                }
                            } else {
                                // Show single rice mode values for clarity
                                Row {
                                    Column (modifier = Modifier.fillMaxWidth(.48f)) {
                                        Text("Rice A: ${String.format(Locale.getDefault(), "%.3f", riceA)}")
                                    }
                                    Column (
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Rice A PCF: ${String.format(Locale.getDefault(), "%.1f", pcfA)}")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text("Average Rice: ${String.format(Locale.getDefault(), "%.3f", averageRice)}")
                            Text("Average PCF: ${String.format(Locale.getDefault(), "%.1f", averagePCF)}")
                        }

                        //User input Rice Test
                        item {
                            Row {
                                OutlinedTextField(
                                    value = dryWeightA,
                                    label = { Text("Dry Weight A") },
                                    modifier = Modifier
                                        .fillMaxWidth(.48f)
                                        .focusRequester(dryARequester),
                                    onValueChange = { newValue ->
                                        if (isDecimalInput(newValue)) {
                                            dryWeightA = newValue
                                            dryAError = validateRequiredDecimal(newValue)
                                            // Only compute when both inputs valid
                                            val d = newValue.toFloatOrNull()
                                            val w = wetWeightA.toFloatOrNull()
                                            val cal = calA
                                            if (d != null && w != null) {
                                                val result = CalculateRice(d, w, cal)
                                                if (!result.isNaN()) {
                                                    riceA = result
                                                    pcfA = RiceToPCF(riceA)
                                                    val aValid = riceA.isFinite() && riceA > 0f
                                                    val bValid = riceB.isFinite() && riceB > 0f
                                                    val values = if (isAverageOfTwo) listOfNotNull(
                                                        if (aValid) riceA else null,
                                                        if (bValid) riceB else null
                                                    ) else listOfNotNull(if (aValid) riceA else null)
                                                    averageRice = if (values.isNotEmpty()) values.average().toFloat() else 0f
                                                    averagePCF = if (averageRice > 0f) RiceToPCF(averageRice) else 0f
                                                }
                                            }
                                        } else {
                                            dryAError = "Invalid number"
                                        }
                                    },
                                    isError = dryAError != null,
                                    supportingText = { dryAError?.let { Text(it) } },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { wetARequester.requestFocus() }
                                    ),
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                OutlinedTextField(
                                    value = wetWeightA,
                                    label = { Text("Wet Weight A") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(wetARequester),
                                    onValueChange = { newValue ->
                                        if (isDecimalInput(newValue)) {
                                            wetWeightA = newValue
                                            wetAError = validateRequiredDecimal(newValue)
                                            val d = dryWeightA.toFloatOrNull()
                                            val w = newValue.toFloatOrNull()
                                            val cal = calA
                                            if (d != null && w != null) {
                                                val result = CalculateRice(d, w, cal)
                                                if (!result.isNaN()) {
                                                    riceA = result
                                                    pcfA = RiceToPCF(riceA)
                                                    val aValid = riceA.isFinite() && riceA > 0f
                                                    val bValid = riceB.isFinite() && riceB > 0f
                                                    val values = if (isAverageOfTwo) listOfNotNull(
                                                        if (aValid) riceA else null,
                                                        if (bValid) riceB else null
                                                    ) else listOfNotNull(if (aValid) riceA else null)
                                                    averageRice = if (values.isNotEmpty()) values.average().toFloat() else 0f
                                                    averagePCF = if (averageRice > 0f) RiceToPCF(averageRice) else 0f
                                                }
                                            }
                                        } else {
                                            wetAError = "Invalid number"
                                        }
                                    },
                                    isError = wetAError != null,
                                    supportingText = { wetAError?.let { Text(it) } },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = if (isAverageOfTwo) ImeAction.Next else ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { dryBRequester.requestFocus() },
                                        onDone = { focusManager.clearFocus() }
                                    ),
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth(),

                            ) {
                                if(isAverageOfTwo){
                                    OutlinedTextField(
                                        value = dryWeightB,
                                        onValueChange = { newValue ->
                                            if (isDecimalInput(newValue)) {
                                                dryWeightB = newValue
                                                dryBError = validateRequiredDecimal(newValue)
                                                val d = newValue.toFloatOrNull()
                                                val w = wetWeightB.toFloatOrNull()
                                                val cal = calB
                                                if (d != null && w != null) {
                                                    val result = CalculateRice(d, w, cal)
                                                    if (!result.isNaN()) {
                                                        riceB = result
                                                        pcfB = RiceToPCF(riceB)
                                                        val aValid = riceA.isFinite() && riceA > 0f
                                                        val bValid = riceB.isFinite() && riceB > 0f
                                                        val values = if (isAverageOfTwo) listOfNotNull(
                                                            if (aValid) riceA else null,
                                                            if (bValid) riceB else null
                                                        ) else listOfNotNull(if (aValid) riceA else null)
                                                        averageRice = if (values.isNotEmpty()) values.average().toFloat() else 0f
                                                        averagePCF = if (averageRice > 0f) RiceToPCF(averageRice) else 0f
                                                    }
                                                }
                                            } else {
                                                dryBError = "Invalid number"
                                            }
                                        },
                                        label = { Text("Dry Weight B") },
                                        isError = dryBError != null,
                                        supportingText = { dryBError?.let { Text(it) } },
                                        modifier = Modifier
                                            .fillMaxWidth(.48f)
                                            .focusRequester(dryBRequester),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Next
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { wetBRequester.requestFocus() }
                                        ),
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    OutlinedTextField(
                                        value = wetWeightB,
                                        onValueChange = { newValue ->
                                            if (isDecimalInput(newValue)) {
                                                wetWeightB = newValue
                                                wetBError = validateRequiredDecimal(newValue)
                                                val d = dryWeightB.toFloatOrNull()
                                                val w = newValue.toFloatOrNull()
                                                val cal = calB
                                                if (d != null && w != null) {
                                                    val result = CalculateRice(d, w, cal)
                                                    if (!result.isNaN()) {
                                                        riceB = result
                                                        pcfB = RiceToPCF(riceB)
                                                        val aValid = riceA.isFinite() && riceA > 0f
                                                        val bValid = riceB.isFinite() && riceB > 0f
                                                        val values = if (isAverageOfTwo) listOfNotNull(
                                                            if (aValid) riceA else null,
                                                            if (bValid) riceB else null
                                                        ) else listOfNotNull(if (aValid) riceA else null)
                                                        averageRice = if (values.isNotEmpty()) values.average().toFloat() else 0f
                                                        averagePCF = if (averageRice > 0f) RiceToPCF(averageRice) else 0f
                                                    }
                                                }
                                            } else {
                                                wetBError = "Invalid number"
                                            }
                                        },
                                        label = { Text("Wet Weight B") },
                                        isError = wetBError != null,
                                        supportingText = { wetBError?.let { Text(it) } },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(wetBRequester),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Decimal,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = { focusManager.clearFocus() }
                                        ),
                                    )
                                }
                            }

                        }

                        item {
                            Button(
                                onClick = {
                                    // Validate required fields
                                    val aDry = dryWeightA.toFloatOrNull()
                                    val aWet = wetWeightA.toFloatOrNull()
                                    val bDry = dryWeightB.toFloatOrNull()
                                    val bWet = wetWeightB.toFloatOrNull()
                                    val hasCal = selectedCalibration != null || (calA != 0f || calB != 0f)

                                    val singleModeOk = aDry != null && aWet != null
                                    val dualModeOk = singleModeOk && bDry != null && bWet != null

                                    val inputsOk = if (isAverageOfTwo) dualModeOk else singleModeOk

                                    if (!inputsOk || !hasCal) {
                                        // Simple inline validation feedback by setting error texts
                                        if (aDry == null) { /* show error via state already present */ }
                                        if (aWet == null) { /* show error via state already present */ }
                                        if (isAverageOfTwo) {
                                            if (bDry == null) { /* show error */ }
                                            if (bWet == null) { /* show error */ }
                                        }
                                        return@Button
                                    }

                                    // Parse date
                                    val dateMillis = try {
                                        if (testDate.isNotEmpty()) {
                                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                            sdf.isLenient = false
                                            sdf.parse(testDate)?.time ?: System.currentTimeMillis()
                                        } else System.currentTimeMillis()
                                    } catch (e: Exception) { System.currentTimeMillis() }

                                    // Build calibration to embed
                                    val cal = selectedCalibration ?: RiceCalibration(
                                        id = 0,
                                        weightA = calA,
                                        weightB = calB,
                                        date = dateMillis
                                    )

                                    // In single mode, set B weights to 0 if not provided
                                    val finalDryB = if (isAverageOfTwo) (bDry ?: 0f) else 0f
                                    val finalWetB = if (isAverageOfTwo) (bWet ?: 0f) else 0f

                                    scope.launch {
                                        riceRepo.insertRice(
                                            name = "HMA Rice Test",
                                            date = dateMillis,
                                            dryWeightA = aDry ?: 0f,
                                            dryWeightB = finalDryB,
                                            wetWeightA = aWet ?: 0f,
                                            wetWeightB = finalWetB,
                                            calibration = cal
                                        )

                                        // Reset inputs minimally
                                        // Keep calibration selection as convenience
                                        dryWeightA = ""
                                        wetWeightA = ""
                                        if (isAverageOfTwo) {
                                            dryWeightB = ""
                                            wetWeightB = ""
                                        }
                                    }
                                },
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            items(rices, key = { it.id }) { rice ->
                                val isExpanded = expandedCards.contains(rice.id)

                                val avgRice = rice.getAverageRice()
                                val avgPcf = if (avgRice > 0f) RiceToPCF(avgRice) else 0f

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expandedCards = if (isExpanded) {
                                                expandedCards - rice.id
                                            } else {
                                                expandedCards + rice.id
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
                                            text = "Rice Test",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Date: ${convertMillisToDate(rice.date, "yyyy-MM-dd")}"
                                            )
                                            Text(
                                                text = "Rice: ${String.format("%.3f", avgRice)}"
                                            )
                                        }
                                        Text(
                                            text = "PCF: ${String.format("%.1f", avgPcf)}"
                                        )

                                        AnimatedVisibility(
                                            visible = isExpanded,
                                            enter = expandVertically(),
                                            exit = shrinkVertically()
                                        ) {
                                            Column {
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("Dry Weights:")
                                                        Text("A: ${String.format("%.1f", rice.dryWeightA)}")
                                                        Text("B: ${String.format("%.1f", rice.dryWeightB)}")
                                                    }
                                                    Column {
                                                        Text("Wet Weights:")
                                                        Text("A: ${String.format("%.1f", rice.wetWeightA)}")
                                                        Text("B: ${String.format("%.1f", rice.wetWeightB)}")
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                val riceAVal = rice.getRiceA()
                                                val riceBVal = rice.getRiceB()

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("Rice Values:")
                                                        Text("A: ${String.format("%.3f", riceAVal)}")
                                                        Text("B: ${String.format("%.3f", riceBVal)}")
                                                    }
                                                    Column {
                                                        Text("PCF Values:")
                                                        Text("A: ${String.format("%.1f", RiceToPCF(riceAVal))}")
                                                        Text("B: ${String.format("%.1f", RiceToPCF(riceBVal))}")
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Text(
                                                    text = "Calibration: A ${String.format("%.3f", rice.calibration.weightA)} • B ${String.format("%.3f", rice.calibration.weightB)}"
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