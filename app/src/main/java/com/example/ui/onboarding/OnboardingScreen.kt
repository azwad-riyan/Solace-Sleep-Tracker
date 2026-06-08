package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DetectionSensitivity
import com.example.domain.model.Profile
import java.time.LocalTime

@Composable
fun OnboardingScreen(
    onComplete: (Profile) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    // Profile Fields
    var name by remember { mutableStateOf("") }
    var avatarEmoji by remember { mutableStateOf("😴") }
    var sleepGoalHours by remember { mutableStateOf(8) }
    var startHour by remember { mutableStateOf(22) }
    var endHour by remember { mutableStateOf(8) }
    var sensitivity by remember { mutableStateOf(DetectionSensitivity.MEDIUM) }
    
    // Mock permission state for interactive feedback
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    var activityPermissionGranted by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Intro
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                when (step) {
                    1 -> {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("😴", fontSize = 72.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Welcome to Solace",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Automatic, passive sleep tracking designed to run in the background. Effortless sleep insights, starting tonight.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    2 -> {
                        Text(
                            text = "Create Your Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Avatar Switcher
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("😴", "🌙", "🦉", "⭐️", "🧸", "💤").forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (avatarEmoji == emoji) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = if (avatarEmoji == emoji) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { avatarEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 24.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Name") },
                            placeholder = { Text("E.g. Julian") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Target Sleep: $sleepGoalHours hours",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = sleepGoalHours.toFloat(),
                            onValueChange = { sleepGoalHours = it.toInt() },
                            valueRange = 5f..12f,
                            steps = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    3 -> {
                        Text(
                            text = "Required Permissions",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Solace relies on passive sensors to infer sleep patterns without draining battery.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Permission Card 1: Notifications
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notificationPermissionGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Post Notifications", fontWeight = FontWeight.SemiBold)
                                    Text("To show active tracking status & sleep correction updates.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { notificationPermissionGranted = true },
                                    colors = if (notificationPermissionGranted) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.filledTonalButtonColors()
                                ) {
                                    if (notificationPermissionGranted) Icon(Icons.Default.Check, contentDescription = "Granted")
                                    else Text("Allow")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Permission Card 2: Physical/Activity Recognition
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activityPermissionGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Activity Recognition", fontWeight = FontWeight.SemiBold)
                                    Text("Used to filter out periods when your phone shifts in your pocket vs. bed.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { activityPermissionGranted = true },
                                    colors = if (activityPermissionGranted) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.filledTonalButtonColors()
                                ) {
                                    if (activityPermissionGranted) Icon(Icons.Default.Check, contentDescription = "Granted")
                                    else Text("Allow")
                                }
                            }
                        }
                    }
                    4 -> {
                        Text(
                            text = "Detection Engine Setup",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Guard Active Window (F-12)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Detection will only active during this interval to maximize device battery resource.", fontSize = 12.sp)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Starts monitoring at", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { startHour = (startHour + 23) % 24 }) { Text("-") }
                                            Text(String.format("%02d:00", startHour), fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { startHour = (startHour + 1) % 24 }) { Text("+") }
                                        }
                                    }
                                    Column {
                                        Text("Ends monitoring at", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { endHour = (endHour + 23) % 24 }) { Text("-") }
                                            Text(String.format("%02d:00", endHour), fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { endHour = (endHour + 1) % 24 }) { Text("+") }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text("Sensitivity Tuning (F-33)", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetectionSensitivity.values().forEach { sens ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (sensitivity == sens) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { sensitivity = sens }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        sens.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sensitivity == sens) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(onClick = { step-- }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                
                Button(
                    onClick = {
                        if (step < 4) {
                            if (step == 2 && name.isBlank()) {
                                name = "User"
                            }
                            step++
                        } else {
                            // Perfect implementation of profile builder!
                            val finalProfile = Profile(
                                name = name.ifBlank { "Julian" },
                                avatarEmoji = avatarEmoji,
                                sleepGoalMinutes = sleepGoalHours * 60,
                                detectionWindowStart = LocalTime.of(startHour, 0),
                                detectionWindowEnd = LocalTime.of(endHour, 0),
                                sensitivity = sensitivity
                            )
                            onComplete(finalProfile)
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (step == 4) "Get Started" else "Continue")
                }
            }
        }
    }
}
