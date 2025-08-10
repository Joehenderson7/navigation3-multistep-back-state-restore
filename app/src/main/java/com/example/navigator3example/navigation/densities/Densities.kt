package com.example.navigator3example.navigation.densities

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
    var showList by rememberSaveable { mutableStateOf(false) }

    AnimatedContent(
        targetState = showList,
        transitionSpec = {
            if (targetState) {
                (slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300)) + fadeIn()) togetherWith
                        (slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(300)) + fadeOut())
            } else {
                (slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }, animationSpec = tween(300)) + fadeIn()) togetherWith
                        (slideOutHorizontally(targetOffsetX = { fullWidth -> fullWidth }, animationSpec = tween(300)) + fadeOut())
            }
        },
        label = "densities_nav"
    ) { isList ->
        if (!isList) {
            NuclearDensityInputScreen(onViewAll = { showList = true })
        } else {
            DensitiesListScreen(onBack = { showList = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuclearDensityInputScreen(onViewAll: () -> Unit) {
    // Inputs per requirement
    var location by rememberSaveable { mutableStateOf("") }
    var wet1 by rememberSaveable { mutableStateOf("") }
    var wet2 by rememberSaveable { mutableStateOf("") }
    var wet3 by rememberSaveable { mutableStateOf("") }
    var wet4 by rememberSaveable { mutableStateOf("") }
    var correctionFactor by rememberSaveable { mutableStateOf("1.000") }

    // Rice dropdown state
    var riceExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedRiceId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Load rice list from Room
    val context = androidx.compose.ui.platform.LocalContext.current
    val riceDb = remember { com.example.navigator3example.data.rice.RiceDatabase.getDatabase(context) }
    val riceRepo = remember { com.example.navigator3example.data.rice.RiceRepository(riceDb.riceDao()) }
    val rices by riceRepo.getAllRices().collectAsState(initial = emptyList())
    val selectedRice = rices.firstOrNull { it.id == selectedRiceId }

    // Calculations
    val wetValues = listOf(wet1, wet2, wet3, wet4).mapNotNull { it.toDoubleOrNull() }
    val averageWet = remember(wetValues) {
        if (wetValues.isNotEmpty()) wetValues.average() else null
    }
    val cf = correctionFactor.toDoubleOrNull()
    val correctedAverage = remember(averageWet, cf) {
        if (averageWet != null && cf != null) averageWet * cf else null
    }
    val riceAvg = selectedRice?.let { riceAverage(it) }
    val ricePcf = riceAvg?.let { com.example.navigator3example.calculations.RiceToPCF(it.toFloat()).toDouble() }
    val percentCompaction = remember(correctedAverage, ricePcf) {
        if (correctedAverage != null && ricePcf != null && ricePcf > 0.0) correctedAverage / ricePcf * 100.0 else null
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onViewAll) {
                Icon(Icons.Default.List, contentDescription = "View all tests")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary display (similar to Standards’ header card)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Average: ${averageWet?.let { String.format("%.2f", it) } ?: "—"}")
                    Text(text = "Corrected Avg: ${correctedAverage?.let { String.format("%.2f", it) } ?: "—"}")
                    val riceLabel = ricePcf?.let { String.format("%.1f PCF", it) } ?: "—"
                    Text(text = "% Compaction (vs Rice $riceLabel): ${percentCompaction?.let { String.format("%.1f%%", it) } ?: "—"}")
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location / Station") },
                modifier = Modifier.fillMaxWidth()
            )

            // Rice dropdown
            androidx.compose.material3.ExposedDropdownMenuBox(
                expanded = riceExpanded,
                onExpandedChange = { riceExpanded = !riceExpanded }
            ) {
                OutlinedTextField(
                    value = selectedRice?.let { "${it.name}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rice") },
                    trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = riceExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = riceExpanded,
                    onDismissRequest = { riceExpanded = false }
                ) {
                    rices.forEach { rice ->
                        val pcf = riceAverage(rice)?.let { com.example.navigator3example.calculations.RiceToPCF(it.toFloat()) }
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(text = "${rice.name} • ${String.format("%.1f PCF", pcf)}") },
                            onClick = {
                                selectedRiceId = rice.id
                                riceExpanded = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = wet1,
                    onValueChange = { wet1 = it },
                    label = { Text("Wet Density 1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = wet2,
                    onValueChange = { wet2 = it },
                    label = { Text("Wet Density 2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = wet3,
                    onValueChange = { wet3 = it },
                    label = { Text("Wet Density 3") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = wet4,
                    onValueChange = { wet4 = it },
                    label = { Text("Wet Density 4") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = correctionFactor,
                onValueChange = { correctionFactor = it },
                label = { Text("Correction Factor") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            AssistChip(
                onClick = onViewAll,
                label = { Text("View all tests") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.List, contentDescription = null)
                }
            )

            Text(
                text = "Enter four wet densities, select Rice, and a Correction Factor to see calculations above.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DensitiesListScreen(onBack: () -> Unit) {
    val items = remember {
        // Placeholder list of tests; could be backed by Room later
        List(20) { i -> "Density Test #${i + 1}" }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(text = "Densities", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items) { item ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Dry —", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

