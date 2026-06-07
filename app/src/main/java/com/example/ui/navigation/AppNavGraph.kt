package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.profile.ProfileScreen
import com.example.ui.settings.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val appContainer = (context.applicationContext as SolaceApplication).container

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
                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = CalendarViewModelFactory(
                        appContainer.profileRepository,
                        appContainer.sleepSessionRepository
                    )
                )
                CalendarScreen(calendarViewModel)
            }
            composable<InsightsRoute> {
                InsightsScreen()
            }
            composable<ProfileRoute> {
                ProfileScreen()
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
        }
    }
}
