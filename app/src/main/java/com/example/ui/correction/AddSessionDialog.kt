package com.example.ui.correction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    onDismiss: () -> Unit,
    onSave: (LocalTime, LocalTime) -> Unit
) {
    var bedTime by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var wakeTime by remember { mutableStateOf(LocalTime.of(6, 0)) }
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Sleep Session", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Simplified time picker UI using basic controls since full TimePicker takes up a lot of space
                // In a real app we'd open a TimePickerDialog for each
                Column {
                    Text("Bed Time", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { bedTime = bedTime.minusHours(1) }, modifier = Modifier.weight(1f)) { Text("-1h") }
                        Text(bedTime.format(timeFormatter), fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
                        Button(onClick = { bedTime = bedTime.plusHours(1) }, modifier = Modifier.weight(1f)) { Text("+1h") }
                    }
                }
                
                Column {
                    Text("Wake Time", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { wakeTime = wakeTime.minusHours(1) }, modifier = Modifier.weight(1f)) { Text("-1h") }
                        Text(wakeTime.format(timeFormatter), fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
                        Button(onClick = { wakeTime = wakeTime.plusHours(1) }, modifier = Modifier.weight(1f)) { Text("+1h") }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(bedTime, wakeTime) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}
