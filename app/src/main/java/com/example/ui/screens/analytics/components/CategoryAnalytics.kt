package com.example.ui.screens.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.screens.analytics.AnalyticsState

@Composable
fun CategoryAnalytics(state: AnalyticsState, privacyMode: Boolean) {
    if (state.doses.isEmpty()) {
        Text("No data available.")
        return
    }

    val categoryCounts = state.doses.mapNotNull { dose ->
        state.substances.find { it.id == dose.substanceId }?.category?.name
    }.groupBy { it }.mapValues { it.value.size }.toList().sortedByDescending { it.second }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (categoryCounts.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Top Mechanisms / Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    categoryCounts.forEachIndexed { i, (cat, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${i + 1}. $cat", fontWeight = FontWeight.Bold)
                            Text(if (privacyMode) "***" else "$count logs")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
