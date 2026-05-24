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
fun FinanceAnalyticsView(state: AnalyticsState, privacyMode: Boolean, shouldHideFinance: Boolean) {
    if (shouldHideFinance) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text("Finance Mode is hidden", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    
    if (state.doses.isEmpty()) {
        Text("No data available.")
        return
    }

    val currency = state.settings?.currency ?: "USD"
    val totalSpent = state.doses.sumOf { it.price?.toDouble() ?: 0.0 }
    val spentBySubstance = state.doses.groupBy { it.substanceId }
        .mapValues { it.value.sumOf { d -> d.price?.toDouble() ?: 0.0 } }
        .toList().sortedByDescending { it.second }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Spending", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (privacyMode) "*** $currency" else String.format("%.2f %s", totalSpent, currency),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Spending by Substance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                spentBySubstance.forEachIndexed { i, (subId, cost) ->
                    val name = state.substances.find { it.id == subId }?.name ?: "Unknown"
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (privacyMode) "Hidden" else name, fontWeight = FontWeight.Bold)
                        Text(if (privacyMode) "***" else String.format("%.2f %s", cost, currency))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}
