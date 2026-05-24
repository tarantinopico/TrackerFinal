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
import com.example.domain.model.Substance
import com.example.ui.components.GlassCard
import com.example.ui.screens.analytics.AnalyticsState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubstanceAnalyticsDetail(
    substance: Substance,
    state: AnalyticsState,
    privacyMode: Boolean,
    shouldHideFinance: Boolean
) {
    val logs = state.doses.filter { it.substanceId == substance.id }.sortedBy { it.timestamp }
    val totalLogs = logs.size
    
    val totalCost = if (!shouldHideFinance) {
        logs.sumOf { it.price?.toDouble() ?: 0.0 }
    } else 0.0

    val amounts = logs.map { it.doseAmount }
    val trendData = amounts.takeLast(20) // last 20

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (privacyMode) "Hidden" else substance.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Logs", style = MaterialTheme.typography.labelMedium)
                    Text(if (privacyMode) "***" else "$totalLogs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            if (!shouldHideFinance) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Cost", style = MaterialTheme.typography.labelMedium)
                        Text(if (privacyMode) "***" else String.format("%.2f %s", totalCost, state.settings?.currency ?: "USD"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (trendData.size > 2) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dose Trend (Last ${trendData.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!privacyMode) {
                        SimpleLineChart(
                            dataPoints = trendData,
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    } else {
                        Text("Hidden in privacy mode", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recent Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (logs.isEmpty()) {
                    Text("No logs yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    logs.takeLast(5).reversed().forEach { dose ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(sdf.format(Date(dose.timestamp)), style = MaterialTheme.typography.bodyMedium)
                            Text(if (privacyMode) "***" else "${dose.doseAmount} ${dose.unit} (${dose.route})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}
