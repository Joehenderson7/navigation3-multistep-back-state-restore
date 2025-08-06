import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.navigator3example.navigation.TopBar
import com.example.navigator3example.navigation.StandardsScreen
import com.example.navigator3example.navigation.Standard
import kotlinx.serialization.Serializable
import com.example.navigator3example.ui.theme.topAppBarColors


// Tab data class for Material 3 tabs
data class TabItem(
    val title: String,
    val icon: ImageVector,
    val screen: @Composable () -> Unit
)

@Composable
fun NavHost() {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var title by rememberSaveable { mutableStateOf("Densities") }

    // Define the tabs
    val tabs = listOf(
        TabItem(
            title = "Rice",
            icon = Icons.Default.Home,
            screen = { RiceTests() }
        ),
        TabItem(
            title = "Middle",
            icon = Icons.Default.Info,
            screen = { DensityTests() }
        ),
        TabItem(
            title = "Standards",
            icon = Icons.Default.Settings,
            screen = { StandardsScreen(Standard(null, 0, 0)) }
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(title)
        // Content area with animated transitions
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth ->
                        if (targetState > initialState) fullWidth else -fullWidth
                    },
                    animationSpec = tween(300)
                ) + fadeIn() togetherWith slideOutHorizontally(
                    targetOffsetX = { fullWidth ->
                        if (targetState > initialState) -fullWidth else fullWidth
                    },
                    animationSpec = tween(300)
                ) + fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "tab_content"
        ) { tabIndex ->
            tabs[tabIndex].screen()
        }

        // Material 3 TabRow with animated outline - positioned at bottom
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    val currentTabPosition = tabPositions[selectedTabIndex]
                    val animatedWidth by animateDpAsState(
                        targetValue = currentTabPosition.width,
                        animationSpec = tween(durationMillis = 300),
                        label = "tab_width"
                    )
                    val animatedOffset by animateDpAsState(
                        targetValue = currentTabPosition.left,
                        animationSpec = tween(durationMillis = 300),
                        label = "tab_offset"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = animatedOffset)
                            .width(animatedWidth)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun DensityTests() {
    val textState = rememberSaveable { mutableStateOf("") }

    // Sample data for density screen cards
    val densityItems = remember {
        (1..20).map { index ->
            "Density Item $index" to "This is the description for density item $index"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header card with input and navigation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🏠 Density", style = MaterialTheme.typography.headlineLarge)
                    OutlinedTextField(
                        value = textState.value,
                        onValueChange = { textState.value = it },
                        label = { Text("State Protected Input") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // List of density items
        items(densityItems.size) { index ->
            val (title, description) = densityItems[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDensityScreen() {
    DensityTests()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiceTests() {
    // State for HMA rice test input
    var selectedCalibrate by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var testDate by rememberSaveable { mutableStateOf("") }
    var dryWeightA by rememberSaveable { mutableStateOf("") }
    var dryWeightB by rememberSaveable { mutableStateOf("") }
    var wetWeightA by rememberSaveable { mutableStateOf("") }
    var wetWeightB by rememberSaveable { mutableStateOf("") }
    
    // Calibrate options for dropdown
    val calibrateOptions = listOf("Calibrate 1", "Calibrate 2", "Calibrate 3", "Calibrate 4", "Calibrate 5")
    
    // Sample data for previous rice tests
    val previousTests = remember {
        (1..10).map { index ->
            "Rice Test #$index" to "Moisture: ${12 + index}%, Temp: ${20 + index}°C, Duration: ${30 + index}min"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Panel - HMA Rice Test Input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "🌾 HMA Rice Test",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                item {
                    // Calibrate Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCalibrate,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Select Calibrate") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            calibrateOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedCalibrate = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = testDate,
                        onValueChange = { testDate = it },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = dryWeightA,
                        onValueChange = { dryWeightA = it },
                        label = { Text("Dry Weight A (g)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = dryWeightB,
                        onValueChange = { dryWeightB = it },
                        label = { Text("Dry Weight B (g)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = wetWeightA,
                        onValueChange = { wetWeightA = it },
                        label = { Text("Wet Weight A (g)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = wetWeightB,
                        onValueChange = { wetWeightB = it },
                        label = { Text("Wet Weight B (g)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    Button(
                        onClick = { /* TODO: Save test */ },
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
            elevation = CardDefaults.cardElevation(8.dp)
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
                    items(previousTests.size) { index ->
                        val (title, description) = previousTests[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
