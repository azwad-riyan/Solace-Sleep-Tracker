package com.example.data.repository

import com.example.data.local.dao.SleepSessionDao
import com.example.data.local.entity.SleepSessionEntity
import com.example.domain.model.SessionSource
import com.example.domain.model.SessionType
import com.example.domain.model.SleepSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class SleepSessionRepository(private val sleepSessionDao: SleepSessionDao) {
    fun getSessionsForProfile(profileId: String): Flow<List<SleepSession>> {
        return sleepSessionDao.getSessionsForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getSessionById(id: String): SleepSession? {
        return sleepSessionDao.getSessionById(id)?.toDomain()
    }

    suspend fun insertSession(session: SleepSession) {
        sleepSessionDao.insertSession(session.toEntity())
    }

    suspend fun updateSession(session: SleepSession) {
        sleepSessionDao.updateSession(session.toEntity())
    }

    suspend fun deleteSession(id: String) {
        sleepSessionDao.deleteSession(id)
    }

    private fun SleepSessionEntity.toDomain() = SleepSession(
        id = id,
        profileId = profileId,
        sleepOnset = Instant.ofEpochMilli(sleepOnsetMs),
        wakeTime = Instant.ofEpochMilli(wakeTimeMs),
        durationMinutes = durationMinutes,
        sessionType = sessionType,
        source = source,
        confidenceScore = confidenceScore,
        correctionPending = correctionPending,
        qualityScore = qualityScore,
        notes = notes,
        createdAt = Instant.ofEpochMilli(createdAt),
        lastModifiedAt = Instant.ofEpochMilli(lastModifiedAt)
        // Ignoring interruptions and tags for brevity/serialization setup
    )

    private fun SleepSession.toEntity() = SleepSessionEntity(
        id = id,
        profileId = profileId,
        sleepOnsetMs = sleepOnset.toEpochMilli(),
        wakeTimeMs = wakeTime.toEpochMilli(),
        durationMinutes = durationMinutes,
        sessionType = sessionType,
        source = source,
        confidenceScore = confidenceScore,
        correctionPending = correctionPending,
        qualityScore = qualityScore,
        interruptionsJson = "[]",
        tagsJson = "[]",
        notes = notes,
        createdAt = createdAt.toEpochMilli(),
        lastModifiedAt = lastModifiedAt.toEpochMilli()
    )
}
