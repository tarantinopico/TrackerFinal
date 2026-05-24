package com.example.ui.screens.analytics

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.SectionHeader
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

    Scaffold(
        topBar = {
            if (state.detailSubstanceId != null) {
                TopAppBar(
                    title = { Text("Substance Detail") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.viewSubstanceDetail(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.detailSubstanceId == null) {
                SectionHeader(title = "Analytics", icon = Icons.Default.Analytics)
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val sections = AnalyticsSection.values().toList().filter { 
                        if (it == AnalyticsSection.FINANCE) !shouldHideFinance else true 
                    }
                    items(sections) { section ->
                        FilterChip(
                            selected = state.selectedSubsubsection == section,
                            onClick = { viewModel.selectSection(section) },
                            label = { Text(section.name.replace("_", " ")) }
                        )
                    }
                }
                
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
            } else {
                val detailSubstance = state.substances.find { it.id == state.detailSubstanceId }
                if (detailSubstance != null) {
                    SubstanceAnalyticsDetail(
                        substance = detailSubstance,
                        state = state,
                        privacyMode = privacyMode,
                        shouldHideFinance = shouldHideFinance
                    )
                } else {
                    Text("Substance not found")
                }
            }
        }
    }
}
