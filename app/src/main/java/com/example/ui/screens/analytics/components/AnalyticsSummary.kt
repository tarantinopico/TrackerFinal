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
    val global = state.globalAnalytics ?: return

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Logs", style = MaterialTheme.typography.labelMedium)
                    Text(if (privacyMode) "***" else "${global.totalLogs}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Active Subs.", style = MaterialTheme.typography.labelMedium)
                    Text(if (privacyMode) "***" else "${global.activeSubstancesCount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Top Category", style = MaterialTheme.typography.labelMedium)
                    Text(if (privacyMode) "Hidden" else global.topCategory?.name ?: "N/A", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Most Frequent", style = MaterialTheme.typography.labelMedium)
                    Text(if (privacyMode) "Hidden" else global.topSubstanceName ?: "N/A", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        if (global.activityHeatmap.isNotEmpty()) {
            val dailyCounts = global.activityHeatmap.values.map { it.toFloat() }
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Activity Trend (All Logs)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    SimpleLineChart(
                        dataPoints = dailyCounts, 
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            }
        }
        
        if (global.roaDistribution.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ROA Distribution", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    SimpleBarChart(
                        data = global.roaDistribution.map { it.key to it.value * 100f },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}
