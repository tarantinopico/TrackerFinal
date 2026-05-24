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

    val finance = state.financeAnalytics ?: return

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CUMULATIVE SPENDING", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (privacyMode) "***" else String.format("%s%.2f", finance.currencySymbol, finance.cumulativeSpend),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (finance.spendTrend.isNotEmpty()) {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spend Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!privacyMode) {
                        SimpleLineChart(
                            dataPoints = finance.spendTrend.map { it.second },
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    } else {
                        Text("Hidden", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (finance.spendBySubstance.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spending by Substance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!privacyMode) {
                        SimpleBarChart(
                            data = finance.spendBySubstance.map { it.key to it.value }.sortedByDescending { it.second },
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    } else {
                         Text("Hidden", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}
