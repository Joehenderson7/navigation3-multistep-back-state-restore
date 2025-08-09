package com.example.navigator3example.navigation.rice

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
import com.example.navigator3example.data.rice.RiceDatabase
import com.example.navigator3example.data.rice.RiceRepository
import com.example.navigator3example.ui.components.MaterialDateTimePicker
import com.example.navigator3example.ui.components.convertMillisToDate
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RiceCalibrationScreen() {
    val context = LocalContext.current
    // Build repository from Room database
    val db = remember { RiceDatabase.getDatabase(context) }
    val repo = remember { RiceRepository(db.riceDao()) }
    val scope = rememberCoroutineScope()

    // Input states
    var dateMillis by rememberSaveable { mutableStateOf<Long?>(System.currentTimeMillis()) }
    var dateText by rememberSaveable { mutableStateOf(convertMillisToDate(dateMillis!!)) }
    var weightA by rememberSaveable { mutableStateOf("") }
    var weightB by rememberSaveable { mutableStateOf("") }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    // Load previous calibrations
    val calibrationsFlow = remember { repo.getAllCalibrations() }
    val calibrations by calibrationsFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top input card (scrollable content)
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
                    text = "New Rice Calibration",
                    style = MaterialTheme.typography.titleMedium
                )

                // Date picker using shared component
                MaterialDateTimePicker(
                    value = dateText,
                    onDateSelected = { selected ->
                        dateText = selected
                        // Parse back to millis for storage
                        runCatching {
                            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            dateMillis = sdf.parse(selected)?.time
                        }
                    },
                    label = "Calibration Date",
                    placeholder = "Select Date"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weightA,
                        onValueChange = { weightA = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Vessel A Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightB,
                        onValueChange = { weightB = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Vessel B Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val a = weightA.toFloatOrNull()
                        val b = weightB.toFloatOrNull()
                        val d = dateMillis
                        if (a == null || b == null || d == null) {
                            Toast.makeText(context, "Please enter valid weights and date", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        // Insert using repository in IO thread
                        scope.launch(Dispatchers.IO) {
                            runCatching { repo.insertCalibration(a, b, d) }
                                .onSuccess {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Calibration saved", Toast.LENGTH_SHORT).show()
                                        // Reset inputs
                                        weightA = ""
                                        weightB = ""
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
                    Text(if (isSaving) "Saving..." else "Save Calibration")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom list card (scrollable list)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Previous Calibrations",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    items(calibrations) { cal ->
                        val dateStr = convertMillisToDate(cal.date)
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)) {
                            Text(text = dateStr, style = MaterialTheme.typography.bodyMedium)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Vessel A: ${String.format(Locale.getDefault(), "%.3f", cal.weightA)}")
                                Text(text = "Vessel B: ${String.format(Locale.getDefault(), "%.3f", cal.weightB)}")
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
fun PreviewNewRiceCalibrationScreen() {
    RiceCalibrationScreen()
}

