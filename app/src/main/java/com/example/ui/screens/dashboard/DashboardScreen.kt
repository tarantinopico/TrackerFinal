package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassButton
import com.example.ui.screens.dashboard.components.KineticGraph
import com.example.ui.screens.dashboard.components.RecentLogWidget
import com.example.ui.screens.dashboard.components.TodaySummaryCards
import com.example.ui.state.AppSettingsState

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    settings: AppSettingsState,
    onNavigateToLog: () -> Unit,
    onNavigateToSubstanceDetail: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle background gradient for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = 1000f,
                        center = androidx.compose.ui.geometry.Offset(0f, 0f)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp), // Space for floating bottom nav
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                GlassButton(
                    onClick = onNavigateToLog,
                    text = "Log",
                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
            }
            
            // Graph
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                KineticGraph(
                    lines = state.kineticLines,
                    mode = state.graphMode,
                    onModeToggle = { viewModel.toggleGraphMode() }
                )
            }
            
            // Summary Cards
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TodaySummaryCards(state = state, settings = settings)
            }
            
            // Recent Logs
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                RecentLogWidget(
                    logs = state.recentLogs, 
                    privacyMode = settings.privacyMode, 
                    compactMode = settings.compactMode
                )
            }
        }
    }
}

