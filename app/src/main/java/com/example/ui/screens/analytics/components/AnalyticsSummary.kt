package com.example.ui.screens.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.screens.analytics.AnalyticsState

@Composable
fun AnalyticsSummary(state: AnalyticsState, privacyMode: Boolean) {
    val totalDoses = state.doses.size
    val activeSubstancesCount = state.substances.count { it.active }
    
    // Most popular substance
    val topSubstanceId = state.doses.groupBy { it.substanceId }
        .maxByOrNull { it.value.size }?.key
    val topSubstanceName = state.substances.find { it.id == topSubstanceId }?.name ?: "N/A"
    
    // Distribution for donut chart (by category)
    val categoryCounts = state.doses.mapNotNull { dose ->
        state.substances.find { it.id == dose.substanceId }?.category?.name
    }.groupBy { it }.mapValues { it.value.size.toFloat() }.toList()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Logs", style = MaterialTheme.typography.labelMedium)
                    Text(if (privacyMode) "***" else "$totalDoses", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Active Subs.", style = MaterialTheme.typography.labelMedium)
                    Text(if (privacyMode) "***" else "$activeSubstancesCount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Most Frequent Substance", style = MaterialTheme.typography.labelMedium)
                Text(if (privacyMode) "Hidden" else topSubstanceName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
        
        if (categoryCounts.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Logs by Category", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(16.dp))
                    DonutChart(data = categoryCounts, modifier = Modifier.size(200.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    // Legend
                    categoryCounts.forEach { (cat, count) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(cat)
                            Text(if (privacyMode) "***" else "${count.toInt()}")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}
