package com.example.ui.screens.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            GlassCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Logs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (privacyMode) "***" else "$totalLogs", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (!shouldHideFinance) {
                GlassCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (privacyMode) "***" else String.format("%.2f %s", totalCost, state.settings?.currency ?: "USD"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }

        if (trendData.size > 2) {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Dose Trend (Last ${trendData.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(24.dp))
                    if (!privacyMode) {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            SimpleLineChart(
                                dataPoints = trendData,
                                modifier = Modifier.fillMaxWidth().height(180.dp)
                            )
                        }
                    } else {
                        Text("Hidden in privacy mode", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), padding = 0.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Recent Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(20.dp))
                if (logs.isEmpty()) {
                    Text("No logs yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                } else {
                    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    logs.takeLast(7).reversed().forEachIndexed { index, dose ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(sdf.format(Date(dose.timestamp)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (privacyMode) "***" else "${dose.doseAmount} ${dose.unit} (${dose.route})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        if (index < minOf(logs.size, 7) - 1) {
                            androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(start = 20.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

