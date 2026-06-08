package com.example

import kotlinx.coroutines.flow.MutableStateFlow

object AppState {
    val isDarkTheme = MutableStateFlow<Boolean?>(null) // null = system default, true = hold dark, false = hold light
    val notificationEnabled = MutableStateFlow(true)
}
