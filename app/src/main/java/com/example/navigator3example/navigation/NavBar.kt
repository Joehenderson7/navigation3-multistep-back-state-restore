package com.example.navigator3example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.example.navigator3example.ui.theme.topAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {}
    )
}