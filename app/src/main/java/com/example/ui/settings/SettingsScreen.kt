package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Medium) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("App Preferences", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                ListItem(
                    headlineContent = { Text("Dark Theme") },
                    trailingContent = { Switch(checked = false, onCheckedChange = {}) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Notifications") },
                    trailingContent = { Switch(checked = true, onCheckedChange = {}) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                Text("Detection Engine", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                ListItem(
                    headlineContent = { Text("Detection Sensitivity") },
                    supportingContent = { Text("Medium (Recommended)") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                Text("Data & Privacy", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                ListItem(
                    headlineContent = { Text("Export Data") },
                    supportingContent = { Text("Download your data as CSV") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Clear All Data") },
                    supportingContent = { Text("Erase local DB entirely") },
                    leadingContent = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
