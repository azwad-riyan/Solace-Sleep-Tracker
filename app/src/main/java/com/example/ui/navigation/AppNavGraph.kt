package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.SolaceApplication
import com.example.ui.calendar.CalendarScreen
import com.example.ui.calendar.CalendarViewModel
import com.example.ui.calendar.CalendarViewModelFactory
import com.example.ui.insights.InsightsScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.settings.SettingsViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val appContainer = (context.applicationContext as SolaceApplication).container
    val scope = rememberCoroutineScope()

    // Observe Profiles flow to trigger onboarding on first launch (F-28)
    val profilesState by appContainer.profileRepository.getAllProfiles().collectAsState(initial = null)

    if (profilesState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (profilesState!!.isEmpty()) {
        OnboardingScreen(
            onComplete = { newProfile ->
                scope.launch {
                    appContainer.profileRepository.insertProfile(newProfile)
                }
            }
        )
        return
    }

    // Shared ViewModel setup to ensure real-time consistency
    val calendarViewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(
            appContainer.profileRepository,
            appContainer.sleepSessionRepository
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == CalendarRoute::class.qualifiedName } == true,
                    onClick = {
                        navController.navigate(CalendarRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == InsightsRoute::class.qualifiedName } == true,
                    onClick = {
                        navController.navigate(InsightsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Insights") },
                    label = { Text("Insights") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == ProfileRoute::class.qualifiedName } == true,
                    onClick = {
                        navController.navigate(ProfileRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == SettingsRoute::class.qualifiedName } == true,
                    onClick = {
                        navController.navigate(SettingsRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CalendarRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<CalendarRoute> {
                CalendarScreen(calendarViewModel)
            }
            composable<InsightsRoute> {
                InsightsScreen(calendarViewModel)
            }
            composable<ProfileRoute> {
                ProfileScreen(calendarViewModel)
            }
            composable<SettingsRoute> {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(
                        appContainer.profileRepository,
                        appContainer.sleepSessionRepository
                    )
                )
                SettingsScreen(settingsViewModel)
            }
        }
    }
}
