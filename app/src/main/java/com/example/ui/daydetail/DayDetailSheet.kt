package com.example.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SleepSession
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(
    session: SleepSession,
    onDismiss: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Sleep Record?") },
            text = { Text("Are you sure you want to delete this sleep session? This record will be permanently removed from your dashboard.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        onDeleteClick(session.id)
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val zone = ZoneId.systemDefault()
        val onset = session.sleepOnset.atZone(zone)
        val wake = session.wakeTime.atZone(zone)
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd")
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(onset.format(dateFormatter), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            // Overview card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BED TIME", fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(onset.format(timeFormatter), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(modifier = Modifier.height(35.dp).width(1.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WAKE TIME", fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(wake.format(timeFormatter), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(modifier = Modifier.height(35.dp).width(1.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DURATION", fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val hrs = session.durationMinutes / 60
                        val mins = session.durationMinutes % 60
                        Text("${hrs}h ${mins}m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            // Quality Rating
            if (session.qualityScore != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quality Rating", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val score = session.qualityScore
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= score) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = null,
                                tint = if (i <= score) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Tags Display
            if (session.tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sleep Conditions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        session.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tag, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }
            }

            // Notes Display
            if (!session.notes.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Sleep Journal / Notes", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(session.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            // Source & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Inference: ${session.source.name.replace("_", " ")}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                if (session.confidenceScore != null) {
                    val confColor = if(session.confidenceScore > 80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(confColor))
                        Text("${session.confidenceScore}% Confidence", fontSize = 11.sp, color = confColor, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Discard completely button
            Button(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Sleep Session Record", fontWeight = FontWeight.Bold)
            }
        }
    }
}
