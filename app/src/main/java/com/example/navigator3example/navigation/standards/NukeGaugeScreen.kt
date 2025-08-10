package com.example.navigator3example.navigation.standards

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.navigator3example.ui.components.MaterialDateTimePicker
import com.example.navigator3example.ui.components.convertMillisToDate
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.navigator3example.data.nuke.NukeGaugeDatabase
import com.example.navigator3example.data.nuke.NukeGaugeRepository
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Box

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NukeGaugeScreen() {
    val context = LocalContext.current
    val db = remember { NukeGaugeDatabase.getDatabase(context) }
    val repo = remember { NukeGaugeRepository(db.nukeGaugeDao()) }
    val scope = rememberCoroutineScope()

    var dateMillis by rememberSaveable { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var dateText by rememberSaveable { mutableStateOf(convertMillisToDate(dateMillis!!)) }
    var serialNumber by rememberSaveable { mutableStateOf("") }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    val calibrationsFlow = remember { repo.getAllCalibrations() }
    val calibrations by calibrationsFlow.collectAsState(initial = emptyList())

    var gaugeMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedGaugeId by rememberSaveable { mutableStateOf<Long?>(null) }

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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "New Nuke Gauge",
                    style = MaterialTheme.typography.titleMedium
                )

                MaterialDateTimePicker(
                    value = dateText,
                    onDateSelected = { selected ->
                        dateText = selected
                        runCatching {
                            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            dateMillis = sdf.parse(selected)?.time
                        }
                    },
                    label = "Date",
                    placeholder = "Select Date"
                )

                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { newValue ->
                        // Allow digits only to stay consistent with Standards screen for now
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            serialNumber = newValue
                        }
                    },
                    label = { Text("Gauge Serial Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val d = dateMillis
                        if (d == null || serialNumber.isBlank()) {
                            Toast.makeText(context, "Please enter date and serial number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        scope.launch(Dispatchers.IO) {
                            runCatching { repo.insertGauge(serialNumber, d) }
                                .onSuccess {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Gauge saved", Toast.LENGTH_SHORT).show()
                                        serialNumber = ""
                                        isSaving = false
                                    }
                                }
                                .onFailure { e ->
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                                        isSaving = false
                                    }
                                }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSaving) "Saving..." else "Save Gauge")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Existing Nuke Gauges",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    items(calibrations, key = { it.id }) { cal ->
                        val dateStr = convertMillisToDate(cal.date)
                        Box {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            selectedGaugeId = cal.id
                                            gaugeMenuExpanded = true
                                        }
                                    )
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = dateStr, style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "SN: ${cal.serialNumber}")
                                }
                            }

                            DropdownMenu(
                                expanded = gaugeMenuExpanded && selectedGaugeId == cal.id,
                                onDismissRequest = { gaugeMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        val id = selectedGaugeId
                                        gaugeMenuExpanded = false
                                        if (id != null) {
                                            scope.launch { repo.deleteCalibration(id) }
                                        }
                                    }
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewNukeGaugeScreen() {
    NukeGaugeScreen()
}