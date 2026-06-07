package com.example.di

import android.content.Context
import com.example.data.local.SolaceDatabase
import com.example.data.repository.ProfileRepository
import com.example.data.repository.SleepSessionRepository

interface AppContainer {
    val database: SolaceDatabase
    val profileRepository: ProfileRepository
    val sleepSessionRepository: SleepSessionRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: SolaceDatabase by lazy {
        SolaceDatabase.getDatabase(context)
    }
    
    override val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.profileDao())
    }
    
    override val sleepSessionRepository: SleepSessionRepository by lazy {
        SleepSessionRepository(database.sleepSessionDao())
    }
}
