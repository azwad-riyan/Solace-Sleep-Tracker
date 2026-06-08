package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AppState
import com.example.domain.model.DetectionSensitivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val currentProfile by viewModel.currentProfile.collectAsState()
    
    val isDarkThemeOverride by AppState.isDarkTheme.collectAsState()
    val notificationEnabled by AppState.notificationEnabled.collectAsState()
    
    var showSensitivityMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Erase All Data?") },
            text = { Text("This will permanently delete all created user profiles and sleep records from local database storage. This operation is irreversible.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.clearAllData()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontSize = 24.sp, fontWeight = FontWeight.Normal) },
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
                Text(
                    text = "App Preferences",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Dark Theme") },
                    supportingContent = { Text("Manually lock dark mode theme") },
                    trailingContent = {
                        Switch(
                            checked = isDarkThemeOverride == true,
                            onCheckedChange = { AppState.isDarkTheme.value = if (it) true else false }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Notifications") },
                    supportingContent = { Text("Receive active sensor monitoring alerts") },
                    trailingContent = {
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { AppState.notificationEnabled.value = it }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                Text(
                    text = "Detection Engine",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                Box {
                    ListItem(
                        modifier = Modifier.clickable { showSensitivityMenu = true },
                        headlineContent = { Text("Detection Sensitivity") },
                        supportingContent = { Text("Current: ${currentProfile?.sensitivity?.name ?: "MEDIUM"}") },
                        trailingContent = { Text("Configure", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                    )
                    
                    DropdownMenu(
                        expanded = showSensitivityMenu,
                        onDismissRequest = { showSensitivityMenu = false }
                    ) {
                        DetectionSensitivity.values().forEach { sensitivity ->
                            DropdownMenuItem(
                                text = { Text(sensitivity.name) },
                                onClick = {
                                    viewModel.updateSensitivity(sensitivity)
                                    showSensitivityMenu = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                Text(
                    text = "Data & Privacy",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable { viewModel.exportData(context) },
                    headlineContent = { Text("Export Data") },
                    supportingContent = { Text("Download your data as CSV") },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable { showDeleteConfirmDialog = true },
                    headlineContent = { Text("Clear All Data", color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text("Erase local DB entirely") },
                    leadingContent = { Icon(Icons.Default.Warning, contentDescription = "Erase DB", tint = MaterialTheme.colorScheme.error) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
