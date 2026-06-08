package com.example.ui.correction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SessionSource
import com.example.domain.model.SleepSession
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CorrectionSheet(
    session: SleepSession,
    onConfirm: (SleepSession) -> Unit,
    onDismiss: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    var bedTime by remember { mutableStateOf(session.sleepOnset.atZone(zone).toLocalTime()) }
    var wakeTime by remember { mutableStateOf(session.wakeTime.atZone(zone).toLocalTime()) }
    var qualityScore by remember { mutableStateOf(4) }
    var notes by remember { mutableStateOf("") }
    
    // Choose sleep tags
    val availableTags = listOf("Caffeine ☕️", "Exercised 🏃‍♂️", "Stress 🤯", "Device Free 📵", "Alcohol 🍷", "Heavy Meal 🍔")
    val selectedTags = remember { mutableStateListOf<String>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Confirm Your Sleep", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("We auto-detected a sleep session last night.", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text("Is this correct? You can adjust the times below.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Adjust times
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("BED TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { bedTime = bedTime.minusMinutes(15) }) { Text("-", fontWeight = FontWeight.Bold) }
                                Text(bedTime.format(DateTimeFormatter.ofPattern("hh:mm a")), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                IconButton(onClick = { bedTime = bedTime.plusMinutes(15) }) { Text("+", fontWeight = FontWeight.Bold) }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("WAKE TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { wakeTime = wakeTime.minusMinutes(15) }) { Text("-", fontWeight = FontWeight.Bold) }
                                Text(wakeTime.format(DateTimeFormatter.ofPattern("hh:mm a")), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                IconButton(onClick = { wakeTime = wakeTime.plusMinutes(15) }) { Text("+", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }

            // Quality rating
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Select Sleep Quality Rating", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= qualityScore) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Rating $i",
                            tint = if (i <= qualityScore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { qualityScore = i }
                        )
                    }
                }
            }

            // Tags selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Sleep Factors / Tags", fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    availableTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Private sleep journal / notes") },
                placeholder = { Text("How do you feel after waking up?") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Submit button
            Button(
                onClick = {
                    val today = session.sleepOnset.atZone(zone).toLocalDate()
                    var bedDateTime = today.atTime(bedTime)
                    val wakeDateTime = today.atTime(wakeTime)
                    
                    if (bedTime.isAfter(wakeTime)) {
                        bedDateTime = bedDateTime.minusDays(1)
                    }
                    
                    val sleepOnset = bedDateTime.atZone(zone).toInstant()
                    val wakeInstant = wakeDateTime.atZone(zone).toInstant()
                    val duration = java.time.Duration.between(sleepOnset, wakeInstant).toMinutes().toInt()

                    val correctedSession = session.copy(
                        sleepOnset = sleepOnset,
                        wakeTime = wakeInstant,
                        durationMinutes = duration,
                        qualityScore = qualityScore,
                        source = SessionSource.AUTO_CORRECTED,
                        correctionPending = false,
                        tags = selectedTags.toList(),
                        notes = notes.ifBlank { null }
                    )
                    onConfirm(correctedSession)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm & Save Sleep Session", fontWeight = FontWeight.Bold)
            }
        }
    }
}
