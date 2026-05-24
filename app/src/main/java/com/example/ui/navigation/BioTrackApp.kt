package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.viewmodel.BioTrackViewModel

@Composable
fun BioTrackApp(viewModel: BioTrackViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.DASHBOARD

    val logs by viewModel.logs.collectAsState()
    val substances by viewModel.substances.collectAsState()
    val settings by viewModel.settingsState.collectAsState()

    val showBottomNav = currentRoute in listOf(
        Routes.DASHBOARD, Routes.LOGGING, Routes.LAB, Routes.ANALYTICS, Routes.SETTINGS
    )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(bottom = if (showBottomNav) paddingValues.calculateBottomPadding() else 0.dp)
        ) {
            composable(Routes.DASHBOARD) {
                val dashboardViewModel: com.example.ui.screens.dashboard.DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.example.ui.screens.dashboard.DashboardViewModelFactory(viewModel.repository)
                )
                com.example.ui.screens.dashboard.DashboardScreen(
                    viewModel = dashboardViewModel,
                    settings = settings,
                    onNavigateToLog = { navController.navigate(Routes.LOGGING) },
                    onNavigateToSubstanceDetail = { id -> 
                        navController.navigate(Routes.SUBSTANCE_DETAIL.replace("{substanceId}", id))
                    }
                )
            }
            composable(Routes.LOGGING) {
                com.example.ui.screens.log.LogScreen(
                    substances = substances,
                    onSave = { id, amount, unit, notes, cost ->
                        viewModel.addLog(id, amount, unit, notes, cost)
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.LAB) {
               com.example.ui.screens.lab.LabScreen(
                   substances = substances,
                   onAddSubstance = { /* Handled in lab screen in future */ }
               )
            }
            composable(Routes.ANALYTICS) {
                com.example.ui.screens.analytics.AnalyticsScreen(logs = logs)
            }
            composable(Routes.SETTINGS) {
               com.example.ui.screens.settings.SettingsScreen(
                   settings = settings,
                   substances = substances,
                   logs = logs,
                   onTogglePrivacy = { viewModel.togglePrivacyMode() },
                   onUpdateAccent = { accent -> viewModel.updateAccentPalette(accent) },
                   onUpdateTheme = { theme -> viewModel.updateThemeMode(theme) }
               )
            }
            composable(Routes.SUBSTANCE_DETAIL) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("substanceId")
                val substance = substances.find { it.id == id }
                com.example.ui.screens.detail.SubstanceDetailScreen(
                    substance = substance,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
