package com.example.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.calendar.CalendarViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(viewModel: CalendarViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    
    val targetGoalMinutes = currentProfile?.sleepGoalMinutes ?: 480
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now()

    // 1. Calculate Average Duration
    val avgMinutes = if (sessions.isNotEmpty()) {
        sessions.map { it.durationMinutes }.average().toInt()
    } else {
        0
    }
    val avgHrs = avgMinutes / 60
    val avgMins = avgMinutes % 60

    // 2. Calculate Current Streak
    val sessionDates = sessions.map { it.sleepOnset.atZone(zone).toLocalDate() }.toSet()
    var streak = 0
    var checkDate = today
    // If no sleep session today, try starting from yesterday (user might not have slept yet today)
    if (!sessionDates.contains(checkDate) && sessionDates.contains(checkDate.minusDays(1))) {
        checkDate = checkDate.minusDays(1)
    }
    while (sessionDates.contains(checkDate)) {
        streak++
        checkDate = checkDate.minusDays(1)
    }

    // 3. Calculate Sleep Debt (for the last 7 recorded days)
    val last7Days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
    val totalGoalMinutes = last7Days.size * targetGoalMinutes
    val actualSleepMinutesLast7Days = last7Days.sumOf { date ->
        sessions.filter { it.sleepOnset.atZone(zone).toLocalDate() == date }.sumOf { it.durationMinutes }
    }
    val sleepDebtMinutes = (totalGoalMinutes - actualSleepMinutesLast7Days).coerceAtLeast(0)
    val debtHrs = sleepDebtMinutes / 60
    val debtMins = sleepDebtMinutes % 60

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights", fontSize = 24.sp, fontWeight = FontWeight.Normal) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main average sleep widget
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Average Sleep Session",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (avgMinutes > 0) "${avgHrs}h ${avgMins}m" else "No data",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val progress = if (targetGoalMinutes > 0) {
                            (avgMinutes.toFloat() / targetGoalMinutes.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Goal is ${targetGoalMinutes / 60}h (${(progress * 100).toInt()}% achieved)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Streak & Debt Stats Grid
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Current Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$streak Days", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = if (sleepDebtMinutes > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Sleep Debt (7d)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${debtHrs}h ${debtMins}m", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 7-day Historical Sleep Trend Chart (F-07)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Weekly Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7Days.forEach { date ->
                                val dateSessions = sessions.filter { it.sleepOnset.atZone(zone).toLocalDate() == date }
                                val dayMinutes = dateSessions.sumOf { it.durationMinutes }
                                
                                // Max target for scaling chart limit is 10 hours (600 minutes)
                                val scaleLimit = 600f
                                val fillWeight = (dayMinutes.toFloat() / scaleLimit).coerceIn(0.05f, 1f)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Visual Bar
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(0.35f),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight(fillWeight)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(
                                                    if (dayMinutes >= targetGoalMinutes) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                                )
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Day Initial Label
                                    Text(
                                        text = date.format(DateTimeFormatter.ofPattern("E")).take(1),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
