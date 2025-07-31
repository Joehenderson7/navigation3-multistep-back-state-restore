package com.example.navigator3example.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.navigator3example.ui.theme.topAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
                onClick = { menuExpanded = !menuExpanded }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            // Example constraint for icons/actions
            IconButton(modifier = Modifier.wrapContentSize(), onClick = { /* TODO */ }) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        colors = topAppBarColors()
    )
}