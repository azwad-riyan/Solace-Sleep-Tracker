package com.example.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SleepSessionRepository
import com.example.domain.model.DetectionSensitivity
import com.example.domain.model.Profile
import com.example.domain.model.SessionSource
import com.example.domain.model.SessionType
import com.example.domain.model.SleepSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

class CalendarViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SleepSessionRepository
) : ViewModel() {

    private val _currentProfileId = MutableStateFlow<String?>(null)
    val currentProfileId: StateFlow<String?> = _currentProfileId

    init {
        viewModelScope.launch {
            // Observe profiles list and pick the first available if none is selected
            profileRepository.getAllProfiles().collect { profilesList ->
                if (profilesList.isNotEmpty() && _currentProfileId.value == null) {
                    _currentProfileId.value = profilesList.first().id
                }
            }
        }
    }

    val profiles: StateFlow<List<Profile>> = profileRepository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentProfile: StateFlow<Profile?> = _currentProfileId
        .flatMapLatest { id ->
            if (id != null) {
                profileRepository.getAllProfiles().map { list -> list.find { it.id == id } }
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<SleepSession>> = _currentProfileId
        .flatMapLatest { profileId ->
            if (profileId != null) {
                sessionRepository.getSessionsForProfile(profileId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectProfile(profileId: String) {
        _currentProfileId.value = profileId
    }

    fun insertProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.insertProfile(profile)
            _currentProfileId.value = profile.id
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.updateProfile(profile)
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profileId)
            if (_currentProfileId.value == profileId) {
                _currentProfileId.value = profiles.value.firstOrNull { it.id != profileId }?.id
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }

    fun updateSession(session: SleepSession) {
        viewModelScope.launch {
            sessionRepository.insertSession(session) // Room uses OnConflictStrategy.REPLACE
        }
    }

    fun addManualSession(
        bedTime: LocalTime,
        wakeTime: LocalTime,
        rating: Int = 4,
        tags: List<String> = emptyList(),
        notes: String? = null
    ) {
        val profileId = _currentProfileId.value ?: return
        viewModelScope.launch {
            val now = Instant.now()
            val zone = ZoneId.systemDefault()
            val today = now.atZone(zone).toLocalDate()
            
            var bedDateTime = today.atTime(bedTime)
            val wakeDateTime = today.atTime(wakeTime)
            
            if (bedTime.isAfter(wakeTime)) {
                bedDateTime = bedDateTime.minusDays(1)
            }
            
            val sleepOnset = bedDateTime.atZone(zone).toInstant()
            val wakeInstant = wakeDateTime.atZone(zone).toInstant()
            
            val durationMinutes = java.time.Duration.between(sleepOnset, wakeInstant).toMinutes().toInt()
            
            val session = SleepSession(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                sleepOnset = sleepOnset,
                wakeTime = wakeInstant,
                durationMinutes = durationMinutes,
                sessionType = SessionType.NIGHT_SLEEP,
                source = SessionSource.MANUAL,
                confidenceScore = 100,
                correctionPending = false,
                qualityScore = rating,
                tags = tags,
                notes = notes
            )
            sessionRepository.insertSession(session)
        }
    }
}

class CalendarViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SleepSessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(profileRepository, sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
