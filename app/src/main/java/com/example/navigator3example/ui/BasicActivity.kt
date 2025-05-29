import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable


import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay


@kotlinx.serialization.Serializable
sealed interface ScreenKey : NavKey {
    @kotlinx.serialization.Serializable
    object Home : ScreenKey
    @kotlinx.serialization.Serializable
    object Middle : ScreenKey
    @Serializable
    object Detail : ScreenKey
}



@Composable
fun AppNavigator() {
    val backStack = rememberNavBackStack(ScreenKey.Home)
    val navTo: (ScreenKey) -> Unit = { backStack.add(it) }
    val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeLastOrNull() }
    val jumpBackTwo: () -> Unit = { repeat(2) { goBack() } }

    NavDisplay(
        backStack = backStack,
        onBack = { steps -> repeat(steps) { goBack() } },
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),  // 📌 State saver
        ),
        transitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn()) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(300)
                    ) + fadeOut())
        },
        popTransitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeIn()) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(300)
                    ) + fadeOut())
        },
        entryProvider = entryProvider {
            entry<ScreenKey.Home> { HomeScreen(onNext = { navTo(ScreenKey.Middle) }) }
            entry<ScreenKey.Middle> { MiddleScreen(onNext = { navTo(ScreenKey.Detail) }) }
            entry<ScreenKey.Detail> { DetailScreen(onJumpBack = jumpBackTwo) }
        }
    )
}

@Composable
fun HomeScreen(onNext: () -> Unit) {
    val textState = rememberSaveable { mutableStateOf("") } // doğru kullanım

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
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
                Button(
                    onClick = onNext,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go to Middle")
                }
            }
        }
    }
}


@Composable
fun MiddleScreen(onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
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
                Text("📍 Middle", style = MaterialTheme.typography.headlineLarge)
                Button(
                    onClick = onNext,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go to Detail")
                }
            }
        }
    }
}


@Composable
fun DetailScreen(onJumpBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
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
                Text("🔎 Detail", style = MaterialTheme.typography.headlineLarge)
                Button(
                    onClick = onJumpBack,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go back 2 steps")
                }
            }
        }
    }
}