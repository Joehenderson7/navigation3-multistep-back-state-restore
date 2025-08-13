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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.DropdownMenu
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
import com.example.navigator3example.ui.components.SlidingPanels

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

    // UI values
    var isAverageOfTwo by rememberSaveable { mutableStateOf(true) }
    var isLabDataEntry by rememberSaveable { mutableStateOf(true) }
    var isFormValid by rememberSaveable { mutableStateOf(false) }

    // Lab mode direct inputs
    var labRiceText by rememberSaveable { mutableStateOf("") }
    var labPCFText by rememberSaveable { mutableStateOf("") }
    var labRiceError by rememberSaveable { mutableStateOf<String?>(null) }
    var labPCFError by rememberSaveable { mutableStateOf<String?>(null) }

    // Date Picker
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

    var riceMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedRiceId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Restore last selected calibration when data is available and nothing selected yet
    LaunchedEffect(calibrations, lastCalId) {
        if (selectedCalibrate.isEmpty() && lastCalId != -1L) {
            calibrations.firstOrNull { it.id == lastCalId }?.let { cal ->
                val label = "${convertMillisToDate(cal.date, "MM/dd")}  •  A: ${String.format(Locale.getDefault(), "%.3f", cal.weightA)}  •  B: ${String.format(Locale.getDefault(), "%.3f", cal.weightB)}"
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
            if (targetState && !initialState) {
                slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            } else {
                slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            }
        },
        label = "rice_sub_nav"
    ) { showCal ->
        if (showCal) {
            RiceCalibrationScreen()
        } else {
            fun isDecimalInput(s: String): Boolean {
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

            SlidingPanels(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                bottomTitle = "Rice Tests",
                topTitle = "New Rice",
                topContent = {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(10.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Toggles + Date
                            item {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Row {
                                        Text(text = "Lab Data Entry", modifier = Modifier.align(CenterVertically))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Switch(
                                            checked = isLabDataEntry,
                                            onCheckedChange = { checked ->
                                                isLabDataEntry = checked
                                                if (!checked) {
                                                    val r = labRiceText.toFloatOrNull() ?: labPCFText.toFloatOrNull()?.div(62.4f)
                                                    averageRice = r ?: 0f
                                                    averagePCF = if (averageRice > 0f) RiceToPCF(averageRice) else 0f
                                                } else {
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
                                                }
                                            },
                                            colors = SwitchDefaults.colors(),
                                            enabled = true
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Row {
                                        Text(text = "Two Tests", modifier = Modifier.align(CenterVertically))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Switch(
                                            checked = isAverageOfTwo,
                                            onCheckedChange = { checked ->
                                                isAverageOfTwo = checked
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
                                            colors = SwitchDefaults.colors(),
                                            enabled = isLabDataEntry
                                        )
                                    }
                                }
                                MaterialDateTimePicker(
                                    value = testDate,
                                    onDateSelected = { selectedDate -> testDate = selectedDate },
                                    label = "Date",
                                    placeholder = "Select Date",
                                    dateFormat = "yyyy-MM-dd",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Calibration chooser
                            if (isLabDataEntry) {
                                item {
                                    Row {
                                        Column {
                                            ExposedDropdownMenuBox(
                                                expanded = expanded,
                                                onExpandedChange = { expanded = !expanded },
                                                modifier = Modifier.fillMaxWidth(.90f)
                                            ) {
                                                OutlinedTextField(
                                                    value = selectedCalibrate,
                                                    onValueChange = { },
                                                    readOnly = true,
                                                    label = { Text("Select Calibration:") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    calibrations.forEach { cal ->
                                                        val label = "${convertMillisToDate(cal.date, "MM/dd")} \u00A0A: ${String.format(Locale.getDefault(), "%.1f", cal.weightA)}  B: ${String.format(Locale.getDefault(), "%.1f", cal.weightB)}"
                                                        DropdownMenuItem(
                                                            text = { Text(label) },
                                                            onClick = {
                                                                selectedCalibrate = label
                                                                calA = cal.weightA
                                                                calB = cal.weightB
                                                                selectedCalibration = cal
                                                                scope.launch { prefs.setLastRiceCalibrationId(cal.id) }
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Column(modifier = Modifier.align(CenterVertically)) {
                                            Button(
                                                onClick = { showNewCalibration = true },
                                                enabled = true,
                                                shape = MaterialTheme.shapes.small,
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                            ) {
                                                Text(
                                                    text = "+",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    textAlign = TextAlign.Center,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Values + averages
                            item {
                                if (isLabDataEntry) {
                                    if (isAverageOfTwo) {
                                        Row {
                                            Column(modifier = Modifier.fillMaxWidth(.48f)) {
                                                Text("Rice A: ${String.format(Locale.getDefault(), "%.3f", riceA)}")
                                                Text("Rice B: ${String.format(Locale.getDefault(), "%.3f", riceB)}")
                                            }
                                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                                Text("Rice A PCF: ${String.format(Locale.getDefault(), "%.1f", pcfA)}")
                                                Text("Rice B PCF: ${String.format(Locale.getDefault(), "%.1f", pcfB)}")
                                            }
                                        }
                                    } else {
                                        Row {
                                            Column(modifier = Modifier.fillMaxWidth(.48f)) { Text("Rice A: ${String.format(Locale.getDefault(), "%.3f", riceA)}") }
                                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) { Text("Rice A PCF: ${String.format(Locale.getDefault(), "%.1f", pcfA)}") }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Average Rice: ${String.format(Locale.getDefault(), "%.3f", averageRice)}")
                                Text("Average PCF: ${String.format(Locale.getDefault(), "%.1f", averagePCF)}")
                            }

                            // Inputs
                            if (isLabDataEntry) {
                                item {
                                    Row {
                                        OutlinedTextField(
                                            value = dryWeightA,
                                            label = { Text("Dry Weight A") },
                                            modifier = Modifier.fillMaxWidth(.48f).focusRequester(dryARequester),
                                            onValueChange = { newValue ->
                                                if (isDecimalInput(newValue)) {
                                                    dryWeightA = newValue
                                                    dryAError = validateRequiredDecimal(newValue)
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
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                            keyboardActions = KeyboardActions(onNext = { wetARequester.requestFocus() }),
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        OutlinedTextField(
                                            value = wetWeightA,
                                            label = { Text("Wet Weight A") },
                                            modifier = Modifier.fillMaxWidth().focusRequester(wetARequester),
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
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = if (isAverageOfTwo) ImeAction.Next else ImeAction.Done),
                                            keyboardActions = KeyboardActions(onNext = { dryBRequester.requestFocus() }, onDone = { focusManager.clearFocus() }),
                                        )
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                        if (isAverageOfTwo) {
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
                                                modifier = Modifier.fillMaxWidth(.48f).focusRequester(dryBRequester),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                                keyboardActions = KeyboardActions(onNext = { wetBRequester.requestFocus() }),
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
                                                modifier = Modifier.fillMaxWidth().focusRequester(wetBRequester),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                            )
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Row {
                                        OutlinedTextField(
                                            value = labRiceText,
                                            onValueChange = { newValue ->
                                                if (isDecimalInput(newValue)) {
                                                    labRiceText = newValue
                                                    labRiceError = validateRequiredDecimal(newValue)
                                                    val r = newValue.toFloatOrNull()
                                                    if (r != null && r > 0f) {
                                                        labPCFText = String.format(Locale.getDefault(), "%.1f", RiceToPCF(r))
                                                        averageRice = r
                                                        averagePCF = RiceToPCF(r)
                                                    } else {
                                                        averageRice = 0f
                                                        averagePCF = 0f
                                                    }
                                                } else {
                                                    labRiceError = "Invalid number"
                                                }
                                            },
                                            label = { Text("Rice") },
                                            isError = labRiceError != null,
                                            supportingText = { labRiceError?.let { Text(it) } },
                                            modifier = Modifier.fillMaxWidth(.48f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        OutlinedTextField(
                                            value = labPCFText,
                                            onValueChange = { newValue ->
                                                if (isDecimalInput(newValue)) {
                                                    labPCFText = newValue
                                                    labPCFError = validateRequiredDecimal(newValue)
                                                    val p = newValue.toFloatOrNull()
                                                    if (p != null && p > 0f) {
                                                        val r = p / 62.4f
                                                        labRiceText = String.format(Locale.getDefault(), "%.3f", r)
                                                        averageRice = r
                                                        averagePCF = p
                                                    } else {
                                                        averageRice = 0f
                                                        averagePCF = 0f
                                                    }
                                                } else {
                                                    labPCFError = "Invalid number"
                                                }
                                            },
                                            label = { Text("PCF") },
                                            isError = labPCFError != null,
                                            supportingText = { labPCFError?.let { Text(it) } },
                                            modifier = Modifier.fillMaxWidth(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                        )
                                    }
                                }
                            }

                            // Save
                            item {
                                Button(
                                    onClick = {
                                        // Parse date first
                                        val dateMillis = try {
                                            if (testDate.isNotEmpty()) {
                                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                                sdf.isLenient = false
                                                sdf.parse(testDate)?.time ?: System.currentTimeMillis()
                                            } else System.currentTimeMillis()
                                        } catch (e: Exception) { System.currentTimeMillis() }

                                        if (isLabDataEntry) {
                                            val aDry = dryWeightA.toFloatOrNull()
                                            val aWet = wetWeightA.toFloatOrNull()
                                            val bDry = dryWeightB.toFloatOrNull()
                                            val bWet = wetWeightB.toFloatOrNull()
                                            val hasCal = selectedCalibration != null || (calA != 0f || calB != 0f)
                                            val singleModeOk = aDry != null && aWet != null
                                            val dualModeOk = singleModeOk && bDry != null && bWet != null
                                            val inputsOk = if (isAverageOfTwo) dualModeOk else singleModeOk
                                            if (!inputsOk || !hasCal) {
                                                return@Button
                                            }
                                            val cal = selectedCalibration ?: RiceCalibration(
                                                id = 0,
                                                weightA = calA,
                                                weightB = calB,
                                                date = dateMillis
                                            )
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
                                                dryWeightA = ""
                                                wetWeightA = ""
                                                if (isAverageOfTwo) {
                                                    dryWeightB = ""
                                                    wetWeightB = ""
                                                }
                                            }
                                        } else {
                                            val r = labRiceText.toFloatOrNull() ?: labPCFText.toFloatOrNull()?.div(62.4f)
                                            if (r == null || r <= 0f) return@Button
                                            val dry = 1f
                                            val wet = 1f - 1f / r
                                            val cal = RiceCalibration(id = 0, weightA = 0f, weightB = 0f, date = dateMillis)
                                            scope.launch {
                                                riceRepo.insertRice(
                                                    name = "HMA Rice Test",
                                                    date = dateMillis,
                                                    dryWeightA = dry,
                                                    dryWeightB = dry,
                                                    wetWeightA = wet,
                                                    wetWeightB = wet,
                                                    calibration = cal
                                                )
                                                labRiceText = ""
                                                labPCFText = ""
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
                },
                bottomContent = {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Previous Rice Tests",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(rices, key = { it.id }) { rice ->
                                    val isExpanded = expandedCards.contains(rice.id)
                                    val avgRice = rice.getAverageRice()
                                    val avgPcf = if (avgRice > 0f) RiceToPCF(avgRice) else 0f

                                    androidx.compose.foundation.layout.Box {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        expandedCards = if (isExpanded) {
                                                            expandedCards - rice.id
                                                        } else {
                                                            expandedCards + rice.id
                                                        }
                                                    },
                                                    onLongClick = {
                                                        selectedRiceId = rice.id
                                                        riceMenuExpanded = true
                                                    }
                                                ),
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
                                                        text = "Date: " + convertMillisToDate(rice.date, "yyyy-MM-dd")
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
                                        DropdownMenu(
                                            expanded = riceMenuExpanded && selectedRiceId == rice.id,
                                            onDismissRequest = { riceMenuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Delete") },
                                                onClick = {
                                                    val id = selectedRiceId
                                                    riceMenuExpanded = false
                                                    if (id != null) {
                                                        scope.launch { riceRepo.deleteRice(id) }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
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
                modifier = Modifier.align(CenterVertically)
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
