package com.example.navigator3example.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.navigator3example.ui.theme.topAppBarColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth() // Ensure Title respects constraints
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { onNavigationClick?.invoke() }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            val context = LocalContext.current
            val prefs = com.example.navigator3example.data.preferences.PreferencesManager.get(context)
            val isDark by prefs.darkThemeEnabled.collectAsState(initial = false)
            val scope = rememberCoroutineScope()
            var actionsExpanded by remember { mutableStateOf(false) }

            IconButton(modifier = Modifier.wrapContentSize(), onClick = { actionsExpanded = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Dark theme") },
                    trailingIcon = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = { checked ->
                                scope.launch { prefs.setDarkThemeEnabled(checked) }
                            }
                        )
                    },
                    onClick = {
                        scope.launch { prefs.setDarkThemeEnabled(!isDark) }
                    }
                )
            }
        },
        colors = topAppBarColors()
    )
}