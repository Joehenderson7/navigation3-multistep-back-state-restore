import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.navigator3example.navigation.rice.RiceTests
import com.example.navigator3example.ui.components.TopBar
import com.example.navigator3example.navigation.standards.StandardsScreen
import com.example.navigator3example.navigation.standards.Standard


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

    // Theme preference
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = com.example.navigator3example.data.preferences.PreferencesManager.get(context)
    val isDarkTheme by prefs.darkThemeEnabled.collectAsState(initial = false)

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
            screen = { com.example.navigator3example.navigation.densities.Densities() }
        ),
        TabItem(
            title = "Standards",
            icon = Icons.Default.Settings,
            screen = { StandardsScreen(Standard(null, 0, 0)) }
        )
    )

    // Hold saveable state for each tab so switching tabs preserves inputs
    val saveableStateHolder = rememberSaveableStateHolder()

    com.example.navigator3example.ui.theme.Navigator3ExampleTheme(darkTheme = isDarkTheme) {
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
            // Use a stable key per tab (e.g., title) to preserve its subtree state
            saveableStateHolder.SaveableStateProvider(key = tabs[tabIndex].title) {
                tabs[tabIndex].screen()
            }
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
}



