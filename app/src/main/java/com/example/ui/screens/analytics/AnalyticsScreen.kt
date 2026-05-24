package com.example.ui.screens.analytics

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.analytics.components.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val state by viewModel.state.collectAsState()
    val settings = state.settings
    val privacyMode = settings?.privacyMode ?: false
    val financeMode = settings?.financeMode ?: true
    val hideFinance = settings?.hideFinanceMode ?: false
    
    val shouldHideFinance = hideFinance || privacyMode || !financeMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Deep background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 1200f,
                        center = androidx.compose.ui.geometry.Offset(100f, 100f)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp), // Nav padding
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // Custom Large Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.detailSubstanceId != null) {
                    IconButton(onClick = { viewModel.viewSubstanceDetail(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (state.detailSubstanceId != null) "Details" else "Analytics",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (state.detailSubstanceId == null) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    val sections = AnalyticsSection.values().toList().filter { 
                        if (it == AnalyticsSection.FINANCE) !shouldHideFinance else true 
                    }
                    items(sections) { section ->
                        FilterChip(
                            selected = state.selectedSubsubsection == section,
                            onClick = { viewModel.selectSection(section) },
                            label = { Text(section.name.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true, selected = state.selectedSubsubsection == section,
                                borderColor = if(state.selectedSubsubsection == section) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                    }
                }
                
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Crossfade(targetState = state.selectedSubsubsection, label = "AnalyticsSection") { section ->
                        when (section) {
                            AnalyticsSection.SUMMARY -> AnalyticsSummary(state, privacyMode)
                            AnalyticsSection.SUBSTANCES -> SubstanceAnalyticsList(state, privacyMode, onSubstanceClick = { viewModel.viewSubstanceDetail(it) })
                            AnalyticsSection.CATEGORIES -> CategoryAnalytics(state, privacyMode)
                            AnalyticsSection.TIMING -> TimingAnalytics(state, privacyMode)
                            AnalyticsSection.HISTORY_CALENDAR -> HistoryCalendarView(state, privacyMode)
                            AnalyticsSection.FINANCE -> FinanceAnalyticsView(state, privacyMode, shouldHideFinance)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    val detailSubstance = state.substances.find { it.id == state.detailSubstanceId }
                    if (detailSubstance != null) {
                        SubstanceAnalyticsDetail(
                            substance = detailSubstance,
                            state = state,
                            privacyMode = privacyMode,
                            shouldHideFinance = shouldHideFinance
                        )
                    } else {
                        Text("Substance not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

