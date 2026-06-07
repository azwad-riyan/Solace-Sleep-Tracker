package com.example.domain.model

import java.time.Instant
import java.time.LocalTime
import java.util.UUID

data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarEmoji: String,
    val sleepGoalMinutes: Int,
    val detectionWindowStart: LocalTime,
    val detectionWindowEnd: LocalTime,
    val sensitivity: DetectionSensitivity,
    val createdAt: Instant = Instant.now()
)

enum class DetectionSensitivity { LOW, MEDIUM, HIGH }

data class SleepSession(
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val sleepOnset: Instant,
    val wakeTime: Instant,
    val durationMinutes: Int,
    val sessionType: SessionType,
    val source: SessionSource,
    val confidenceScore: Int?,
    val correctionPending: Boolean,
    val qualityScore: Int?,
    val interruptions: List<SleepInterruption> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val lastModifiedAt: Instant = Instant.now()
)

enum class SessionType { NIGHT_SLEEP, NAP }
enum class SessionSource { AUTO_DETECTED, MANUAL, AUTO_CORRECTED }

data class SleepInterruption(
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int
)

data class SleepTag(
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val label: String,
    val emoji: String,
    val isDefault: Boolean
)

data class InsightsData(
    val avgSleepDurationMinutes: Int = 0,
    val sleepDebtMinutes: Int = 0,
    val sleepConsistencyScore: Int = 0,
    val streak: Int = 0
)
