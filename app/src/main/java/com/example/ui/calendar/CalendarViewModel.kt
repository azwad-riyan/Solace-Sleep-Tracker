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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class CalendarViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SleepSessionRepository
) : ViewModel() {

    private val _currentProfileId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { profiles ->
                if (profiles.isEmpty()) {
                    val newProfile = Profile(
                        name = "Julian",
                        avatarEmoji = "😴",
                        sleepGoalMinutes = 480,
                        detectionWindowStart = LocalTime.of(22, 0),
                        detectionWindowEnd = LocalTime.of(8, 0),
                        sensitivity = DetectionSensitivity.MEDIUM
                    )
                    profileRepository.insertProfile(newProfile)
                    _currentProfileId.value = newProfile.id
                } else if (_currentProfileId.value == null) {
                    _currentProfileId.value = profiles.first().id
                }
            }
        }
    }

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

    fun addManualSession(bedTime: LocalTime, wakeTime: LocalTime) {
        val profileId = _currentProfileId.value ?: return
        viewModelScope.launch {
            // Assume bedTime is on previous day if it's after wakeTime
            val now = Instant.now()
            val zone = java.time.ZoneId.systemDefault()
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
                profileId = profileId,
                sleepOnset = sleepOnset,
                wakeTime = wakeInstant,
                durationMinutes = durationMinutes,
                sessionType = SessionType.NIGHT_SLEEP,
                source = SessionSource.MANUAL,
                confidenceScore = 100,
                correctionPending = false,
                qualityScore = 5
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
