package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DetectionSensitivity
import com.example.domain.model.Profile
import com.example.ui.calendar.CalendarViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: CalendarViewModel) {
    val currentProfile by viewModel.currentProfile.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog && currentProfile != null) {
        EditProfileDialog(
            profile = currentProfile!!,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.updateProfile(updated)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontSize = 24.sp, fontWeight = FontWeight.Normal) },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentProfile?.avatarEmoji ?: "😴", fontSize = 56.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentProfile?.name ?: "Julian",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                val minutes = currentProfile?.sleepGoalMinutes ?: 480
                Text(
                    text = "Sleep Goal: ${minutes / 60}h ${minutes % 60}m",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Detection Window (F-29)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Start Time")
                            val sHour = currentProfile?.detectionWindowStart?.hour ?: 22
                            val sMin = currentProfile?.detectionWindowStart?.minute ?: 0
                            Text(String.format("%02d:%02d PM", if (sHour > 12) sHour - 12 else sHour, sMin), fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("End Time")
                            val eHour = currentProfile?.detectionWindowEnd?.hour ?: 8
                            val eMin = currentProfile?.detectionWindowEnd?.minute ?: 0
                            Text(String.format("%02d:%02d AM", eHour, eMin), fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sensitivity (F-33)")
                            Text(currentProfile?.sensitivity?.name ?: "MEDIUM", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var avatarEmoji by remember { mutableStateOf(profile.avatarEmoji) }
    var sleepGoalHours by remember { mutableStateOf(profile.sleepGoalMinutes / 60) }
    var startHour by remember { mutableStateOf(profile.detectionWindowStart.hour) }
    var endHour by remember { mutableStateOf(profile.detectionWindowEnd.hour) }
    var sensitivity by remember { mutableStateOf(profile.sensitivity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            profile.copy(
                                name = name,
                                avatarEmoji = avatarEmoji,
                                sleepGoalMinutes = sleepGoalHours * 60,
                                detectionWindowStart = LocalTime.of(startHour, 0),
                                detectionWindowEnd = LocalTime.of(endHour, 0),
                                sensitivity = sensitivity
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Edit Profile Settings") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profile Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Avatar Emoji", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("😴", "🌙", "🦉", "⭐️", "🧸", "💤").forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (avatarEmoji == emoji) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (avatarEmoji == emoji) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable { avatarEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Sleep Goal Hours: $sleepGoalHours", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = sleepGoalHours.toFloat(),
                    onValueChange = { sleepGoalHours = it.toInt() },
                    valueRange = 5f..12f,
                    steps = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Starts at", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { startHour = (startHour + 23) % 24 }) { Text("-") }
                            Text(String.format("%02d:00", startHour), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = { startHour = (startHour + 1) % 24 }) { Text("+") }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Ends at", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { endHour = (endHour + 23) % 24 }) { Text("-") }
                            Text(String.format("%02d:00", endHour), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            IconButton(onClick = { endHour = (endHour + 1) % 24 }) { Text("+") }
                        }
                    }
                }

                Text("Sensitivity Level", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    DetectionSensitivity.values().forEach { sens ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (sensitivity == sens) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { sensitivity = sens }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                sens.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sensitivity == sens) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    )
}
