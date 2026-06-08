package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.AppState
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val darkThemeOverride by AppState.isDarkTheme.collectAsState()
      val darkTheme = darkThemeOverride ?: isSystemInDarkTheme()
      
      MyApplicationTheme(darkTheme = darkTheme) {
        AppNavGraph()
      }
    }
  }
}
