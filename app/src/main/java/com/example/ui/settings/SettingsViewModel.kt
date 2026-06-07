package com.example.ui.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SleepSessionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class SettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionRepository: SleepSessionRepository
) : ViewModel() {

    fun exportData(context: Context) {
        viewModelScope.launch {
            val profiles = profileRepository.getAllProfiles().firstOrNull()
            val profile = profiles?.firstOrNull() ?: return@launch
            
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
