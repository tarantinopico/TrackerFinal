package com.example.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.SectionHeader
import com.example.ui.screens.dashboard.components.KineticGraph
import com.example.ui.screens.dashboard.components.RecentLogWidget
import com.example.ui.screens.dashboard.components.SystemLoadRing
import com.example.ui.screens.dashboard.components.TodaySummaryCards
import com.example.ui.state.AppSettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    settings: AppSettingsState,
    onNavigateToLog: () -> Unit,
    onNavigateToSubstanceDetail: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToLog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Action")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Header
            SectionHeader(title = "Dashboard", icon = Icons.Default.Dashboard)
            
            // Ring
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SystemLoadRing(
                    loadPercent = state.systemLoad,
                    isWarning = state.isWarningHighLoad && settings.warningsEnabled
                )
            }
            
            // Graph
            KineticGraph(
                points = state.graphPoints,
                mode = state.graphMode,
                onModeToggle = { viewModel.toggleGraphMode() }
            )
            
            // Summary Cards
            TodaySummaryCards(state = state, settings = settings)
            
            // Recent Logs
            RecentLogWidget(logs = state.recentLogs, privacyMode = settings.privacyMode)
            
            Spacer(modifier = Modifier.height(60.dp)) // Nav bar padding
        }
    }
}
