package com.example.navigator3example.navigation.densities

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.navigator3example.calculations.RiceToPCF
import com.example.navigator3example.data.rice.RiceDatabase
import com.example.navigator3example.data.rice.RiceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DensitiesListScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val densityDb = remember { com.example.navigator3example.data.densities.DensityTestDatabase.getDatabase(context) }
    val densityRepo = remember { com.example.navigator3example.data.densities.DensityTestRepository(densityDb.densityTestDao()) }
    val tests by densityRepo.getAll().collectAsState(initial = emptyList())

    // Load rices to compute Relative Compaction
    val riceDb = remember { RiceDatabase.getDatabase(context) }
    val riceRepo = remember { RiceRepository(riceDb.riceDao()) }
    val rices by riceRepo.getAllRices().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var expandedTestId by rememberSaveable { mutableStateOf<Long?>(null) }

    // Helper to compute average Rice -> PCF
    fun ricePcfFor(id: Long?): Double? {
        if (id == null) return null
        val rice = rices.firstOrNull { it.id == id } ?: return null
        val a = com.example.navigator3example.calculations.CalculateRice(
            rice.dryWeightA, rice.wetWeightA, rice.calibration.weightA
        )
        val b = com.example.navigator3example.calculations.CalculateRice(
            rice.dryWeightB, rice.wetWeightB, rice.calibration.weightB
        )
        if (a.isNaN() || b.isNaN()) return null
        val avg = (a + b) / 2f
        return RiceToPCF(avg).toDouble()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Saved Density Tests", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (tests.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No density tests yet.")
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tests.size) { index ->
                            val test = tests[index]
                            val wetValues = listOfNotNull(test.wet1, test.wet2, test.wet3, test.wet4)
                            val avgWet = if (wetValues.isNotEmpty()) wetValues.average() else null
                            val correctedAvg = if (avgWet != null && test.correctionFactor != null) avgWet + test.correctionFactor else avgWet
                            val ricePcf = ricePcfFor(test.riceId)
                            val relComp = if (correctedAvg != null && ricePcf != null && ricePcf > 0.0) correctedAvg / ricePcf * 100.0 else null

                            Box {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                expandedTestId = if (expandedTestId == test.id) null else test.id
                                            },
                                            onLongClick = {
                                                selectedId = test.id
                                                menuExpanded = true
                                            }
                                        ),
                                    colors = CardDefaults.elevatedCardColors()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = "Density Test #${test.testNumber}", style = MaterialTheme.typography.titleMedium)
                                                Spacer(Modifier.height(4.dp))
                                                Text(text = "Date: ${test.testDate}")
                                                if (test.location.isNotBlank()) {
                                                    Text(text = "Location: ${test.location}")
                                                }
                                            }
                                            Text(text = relComp?.let { String.format("%.1f%%", it) } ?: "—",
                                                style = MaterialTheme.typography.titleMedium)
                                        }

                                        if (expandedTestId == test.id) {
                                            Spacer(Modifier.height(8.dp))
                                            Divider()
                                            Spacer(Modifier.height(8.dp))
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(text = "Offset: ${test.offset}")
                                                Text(text = "Correction Factor: ${test.correctionFactor?.let { String.format("%.2f", it) } ?: "—"}")
                                                Text(text = "Wet Densities: " +
                                                        (if (wetValues.isEmpty()) "—" else wetValues.joinToString(", ") { String.format("%.2f", it) }))
                                                Text(text = "Average Wet: ${avgWet?.let { String.format("%.2f", it) } ?: "—"}")
                                                Text(text = "Corrected Avg: ${correctedAvg?.let { String.format("%.2f", it) } ?: "—"}")
                                                val rice = test.riceId?.let { id -> rices.firstOrNull { it.id == id } }
                                                val riceLabel = rice?.name ?: "No Rice"
                                                val ricePcfLabel = ricePcf?.let { String.format("%.1f PCF", it) } ?: "—"
                                                Text(text = "Rice: $riceLabel • $ricePcfLabel")
                                                Text(text = "Relative Compaction: ${relComp?.let { String.format("%.1f%%", it) } ?: "—"}")
                                            }
                                        }
                                    }
                                }

                                DropdownMenu(
                                    expanded = menuExpanded && selectedId == test.id,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            val id = selectedId
                                            menuExpanded = false
                                            if (id != null) {
                                                scope.launch {
                                                    densityRepo.deleteById(id)
                                                }
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
    }
}