package com.example.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SleepSessionRepository
import com.example.domain.model.DetectionSensitivity
import com.example.domain.model.Profile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class SettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SleepSessionRepository
) : ViewModel() {

    private val _currentProfileId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { list ->
                if (list.isNotEmpty() && _currentProfileId.value == null) {
                    _currentProfileId.value = list.first().id
                }
            }
        }
    }

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

    fun updateSensitivity(sensitivity: DetectionSensitivity) {
        val profile = currentProfile.value ?: return
        viewModelScope.launch {
            val updated = profile.copy(sensitivity = sensitivity)
            profileRepository.updateProfile(updated)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val profiles = profileRepository.getAllProfiles().firstOrNull() ?: emptyList()
            for (p in profiles) {
                profileRepository.deleteProfile(p.id)
                val sessions = sessionRepository.getSessionsForProfile(p.id).firstOrNull() ?: emptyList()
                for (s in sessions) {
                    sessionRepository.deleteSession(s.id)
                }
            }
            _currentProfileId.value = null
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: return@launch
            val sessions = sessionRepository.getSessionsForProfile(profile.id).firstOrNull() ?: emptyList()

            // Generate CSV
            val csvBuilder = StringBuilder()
            csvBuilder.append("Date,Bed Time,Wake Time,Duration (hrs),Session Type,Source,Quality Score,Tags\n")
            
            val zone = java.time.ZoneId.systemDefault()
            
            for (session in sessions) {
                val onset = session.sleepOnset.atZone(zone)
                val wake = session.wakeTime.atZone(zone)
                
                val dateStr = onset.toLocalDate().toString()
                val onsetTime = onset.toLocalTime().toString()
                val wakeTime = wake.toLocalTime().toString()
                val durHrs = session.durationMinutes / 60.0
                
                csvBuilder.append("$dateStr,$onsetTime,$wakeTime,${String.format("%.2f", durHrs)},${session.sessionType},${session.source},${session.qualityScore ?: ""},\"${session.tags.joinToString(", ")}\"\n")
            }

            try {
                val csvFile = File(context.cacheDir, "solace_export.csv")
                val writer = FileWriter(csvFile)
                writer.write(csvBuilder.toString())
                writer.close()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", csvFile)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Sleep Data").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class SettingsViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SleepSessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(profileRepository, sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
