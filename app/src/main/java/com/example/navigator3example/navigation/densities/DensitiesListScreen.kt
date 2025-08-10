package com.example.navigator3example.navigation.densities

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DensitiesListScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val densityDb = remember { com.example.navigator3example.data.densities.DensityTestDatabase.getDatabase(context) }
    val densityRepo = remember { com.example.navigator3example.data.densities.DensityTestRepository(densityDb.densityTestDao()) }
    val tests by densityRepo.getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }

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
                items(tests, key = { it.id }) { test ->
                    Box {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
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
                                Text(text = "Density Test #${test.testNumber}", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(text = "Date: ${test.testDate}")
                                if (test.location.isNotBlank()) {
                                    Text(text = "Location: ${test.location}")
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