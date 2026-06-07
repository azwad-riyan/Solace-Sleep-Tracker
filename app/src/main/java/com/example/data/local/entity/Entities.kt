package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.DetectionSensitivity
import com.example.domain.model.SessionSource
import com.example.domain.model.SessionType

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "avatar_emoji")
    val avatarEmoji: String,
    @ColumnInfo(name = "sleep_goal_minutes")
    val sleepGoalMinutes: Int,
    @ColumnInfo(name = "window_start_hour")
    val windowStartHour: Int,
    @ColumnInfo(name = "window_start_minute")
    val windowStartMinute: Int,
    @ColumnInfo(name = "window_end_hour")
    val windowEndHour: Int,
    @ColumnInfo(name = "window_end_minute")
    val windowEndMinute: Int,
    val sensitivity: DetectionSensitivity,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

@Entity(
    tableName = "sleep_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profile_id", "sleep_onset_ms"])]
)
data class SleepSessionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "profile_id")
    val profileId: String,
    @ColumnInfo(name = "sleep_onset_ms")
    val sleepOnsetMs: Long,
    @ColumnInfo(name = "wake_time_ms")
    val wakeTimeMs: Long,
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    @ColumnInfo(name = "session_type")
    val sessionType: SessionType,
    val source: SessionSource,
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Int?,
    @ColumnInfo(name = "correction_pending")
    val correctionPending: Boolean,
    @ColumnInfo(name = "quality_score")
    val qualityScore: Int?,
    @ColumnInfo(name = "interruptions_json")
    val interruptionsJson: String,
    @ColumnInfo(name = "tags_json")
    val tagsJson: String,
    val notes: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "last_modified_at")
    val lastModifiedAt: Long
)

@Entity(
    tableName = "sleep_tags",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profile_id"])]
)
data class SleepTagEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "profile_id")
    val profileId: String,
    val label: String,
    val emoji: String,
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean
)
