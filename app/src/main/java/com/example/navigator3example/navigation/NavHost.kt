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
            title = "Home",
            icon = Icons.Default.Home,
            screen = { DensityTests() }
        ),
        TabItem(
            title = "Middle",
            icon = Icons.Default.Info,
            screen = { RiceTests() }
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

    // Sample data for home screen cards
    val homeItems = remember {
        (1..20).map { index ->
            "Home Item $index" to "This is the description for home item $index"
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
                    Text("🏠 Home", style = MaterialTheme.typography.headlineLarge)
                    OutlinedTextField(
                        value = textState.value,
                        onValueChange = { textState.value = it },
                        label = { Text("State Protected Input") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // List of home items
        items(homeItems.size) { index ->
            val (title, description) = homeItems[index]
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
fun PreviewHomeScreen1() {
    DensityTests()
}



