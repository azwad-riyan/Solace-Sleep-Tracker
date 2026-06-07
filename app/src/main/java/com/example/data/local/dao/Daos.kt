package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)
}

@Dao
interface SleepSessionDao {
    @Query("SELECT * FROM sleep_sessions WHERE profile_id = :profileId ORDER BY sleep_onset_ms DESC")
    fun getSessionsForProfile(profileId: String): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions WHERE profile_id = :profileId AND sleep_onset_ms >= :startTimestamp AND sleep_onset_ms <= :endTimestamp ORDER BY sleep_onset_ms DESC")
    fun getSessionsBetweenDates(profileId: String, startTimestamp: Long, endTimestamp: Long): Flow<List<SleepSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSessionEntity)

    @Update
    suspend fun updateSession(session: SleepSessionEntity)

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}
