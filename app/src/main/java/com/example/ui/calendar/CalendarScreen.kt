package com.example.ui.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SessionSource
import com.example.domain.model.SessionType
import com.example.domain.model.SleepSession
import com.example.ui.correction.AddSessionDialog
import com.example.ui.correction.CorrectionSheet
import com.example.ui.daydetail.DayDetailSheet
import com.example.ui.profile.ProfileSwitcherSheet
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<SleepSession?>(null) }
    var showProfileSwitcher by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All Sessions") }

    // Automatically check if there is an unconfirmed session pending review (F-05 Correction Flow)
    val pendingSession = sessions.firstOrNull { it.correctionPending }

    // Dialog components
    if (showAddDialog) {
        AddSessionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { bedTime, wakeTime ->
                viewModel.addManualSession(bedTime, wakeTime)
                showAddDialog = false
            }
        )
    }
    
    selectedSession?.let { session ->
        DayDetailSheet(
            session = session,
            onDismiss = { selectedSession = null },
            onDeleteClick = { id ->
                viewModel.deleteSession(id)
                selectedSession = null
            }
        )
    }

    if (showProfileSwitcher) {
        ProfileSwitcherSheet(
            profiles = profiles,
            currentProfile = currentProfile,
            onProfileSelected = { id -> viewModel.selectProfile(id) },
            onProfileDelete = { id -> viewModel.deleteProfile(id) },
            onProfileCreated = { prof -> viewModel.insertProfile(prof) },
            onDismiss = { showProfileSwitcher = false }
        )
    }

    // Modal popup correction sheet (F-05 / F-19 / F-20)
    pendingSession?.let { unconfirmed ->
        CorrectionSheet(
            session = unconfirmed,
            onConfirm = { corrected -> viewModel.updateSession(corrected) },
            onDismiss = { viewModel.updateSession(unconfirmed.copy(correctionPending = false)) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable { showProfileSwitcher = true }) {
                        Text(
                            text = "Hi, ${currentProfile?.name ?: "User"}", 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to switch profile", 
                            fontSize = 11.sp, 
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { showProfileSwitcher = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentProfile?.avatarEmoji ?: "😴",
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add sleep record manually", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        
        // Filter the displayed sessions dynamically based on chosen filter chip (F-03/F-04)
        val filteredSessions = remember(sessions, selectedFilter) {
            when (selectedFilter) {
                "Night Sleep" -> sessions.filter { it.sessionType == SessionType.NIGHT_SLEEP }
                "Naps" -> sessions.filter { it.sessionType == SessionType.NAP }
                else -> sessions
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header Filter Row (F-03/F-04)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All Sessions", "Night Sleep", "Naps").forEach { option ->
                        item {
                            FilterChip(
                                selected = selectedFilter == option,
                                onClick = { selectedFilter = option },
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }

            // Hero Sleep Display Card (Last night details or goal progress)
            item {
                val latestSession = sessions.firstOrNull { !it.correctionPending }
                val targetMin = currentProfile?.sleepGoalMinutes ?: 480
                
                val durationTitle = if (latestSession != null) {
                    val hrs = latestSession.durationMinutes / 60
                    val mins = latestSession.durationMinutes % 60
                    "${hrs}h ${mins}m"
                } else {
                    "No sessions"
                }
                
                val progress = if (latestSession != null && targetMin > 0) {
                    (latestSession.durationMinutes.toFloat() / targetMin.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                "LAST RECORDED SLEEP",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                durationTitle,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Goal progress: ${(progress * 100).toInt()}%",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Goal ${targetMin / 60}h",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }

            // Quick Passive Sleep Engine Mock Simulation Card (Testing Trigger) (F-01/F-02/F-05)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Simulate auto passive sleep detection event
                                val profileId = currentProfile?.id ?: return@clickable
                                val zone = ZoneId.systemDefault()
                                
                                // Last night sample: 11:15 PM to 7:30 AM
                                val yest = Instant.now().minus(java.time.Duration.ofHours(12))
                                val onsetTime = yest.atZone(zone).toLocalDate().atTime(23, 15).atZone(zone).toInstant()
                                val wakeTime = yest.atZone(zone).toLocalDate().plusDays(1).atTime(7, 30).atZone(zone).toInstant()
                                
                                val calculatedDuration = java.time.Duration.between(onsetTime, wakeTime).toMinutes().toInt()

                                val mockSession = SleepSession(
                                    id = UUID.randomUUID().toString(),
                                    profileId = profileId,
                                    sleepOnset = onsetTime,
                                    wakeTime = wakeTime,
                                    durationMinutes = calculatedDuration,
                                    sessionType = SessionType.NIGHT_SLEEP,
                                    source = SessionSource.AUTO_DETECTED,
                                    confidenceScore = 88,
                                    correctionPending = true, // Triggers Post-Sleep Correction Dialog!
                                    qualityScore = null,
                                    tags = emptyList(),
                                    notes = null
                                )
                                viewModel.updateSession(mockSession)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡️", fontSize = 18.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Simulate Passive Auto-Detection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Fires passive sleep inference to test review banner", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Trigger", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Recent Sessions heading
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Session History", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Total: ${filteredSessions.size}", 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Display historical card feeds
            if (filteredSessions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No recorded sleep sessions", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            } else {
                items(filteredSessions.size) { index ->
                    val session = filteredSessions[index]
                    val onset = session.sleepOnset.atZone(ZoneId.systemDefault())
                    val wake = session.wakeTime.atZone(ZoneId.systemDefault())
                    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd")
                    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSession = session },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(true)
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(onset.format(dayFormatter), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "${onset.format(timeFormatter)} - ${wake.format(timeFormatter)}  •  ${session.source.name.replace("_", " ")}", 
                                    fontSize = 12.sp, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Displays star quality score
                            if (session.qualityScore != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.primary, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${session.qualityScore}", 
                                        fontWeight = FontWeight.Bold, 
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // FloatingAction button bottom paddings
            }
        }
    }
}
