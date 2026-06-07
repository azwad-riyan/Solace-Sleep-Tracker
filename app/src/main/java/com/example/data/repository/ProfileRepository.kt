package com.example.data.repository

import com.example.data.local.dao.ProfileDao
import com.example.data.local.entity.ProfileEntity
import com.example.domain.model.DetectionSensitivity
import com.example.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalTime

class ProfileRepository(private val profileDao: ProfileDao) {
    fun getAllProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getProfileById(id: String): Profile? {
        return profileDao.getProfileById(id)?.toDomain()
    }

    suspend fun insertProfile(profile: Profile) {
        profileDao.insertProfile(profile.toEntity())
    }

    suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile.toEntity())
    }

    suspend fun deleteProfile(id: String) {
        profileDao.deleteProfile(id)
    }

    private fun ProfileEntity.toDomain() = Profile(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        sleepGoalMinutes = sleepGoalMinutes,
        detectionWindowStart = LocalTime.of(windowStartHour, windowStartMinute),
        detectionWindowEnd = LocalTime.of(windowEndHour, windowEndMinute),
        sensitivity = sensitivity,
        createdAt = Instant.ofEpochMilli(createdAt)
    )

    private fun Profile.toEntity() = ProfileEntity(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        sleepGoalMinutes = sleepGoalMinutes,
        windowStartHour = detectionWindowStart.hour,
        windowStartMinute = detectionWindowStart.minute,
        windowEndHour = detectionWindowEnd.hour,
        windowEndMinute = detectionWindowEnd.minute,
        sensitivity = sensitivity,
        createdAt = createdAt.toEpochMilli()
    )
}
