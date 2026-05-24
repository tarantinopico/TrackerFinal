package com.example.ui.screens.analytics.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.screens.analytics.AnalyticsState

@Composable
fun SubstanceAnalyticsList(
    state: AnalyticsState, 
    privacyMode: Boolean,
    onSubstanceClick: (String) -> Unit
) {
    if (state.substances.isEmpty()) {
        Text("No substances tracked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val doseCountBySubstance = state.doses.groupBy { it.substanceId }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.substances) { sub ->
            val logs = doseCountBySubstance[sub.id]?.size ?: 0
            GlassCard(modifier = Modifier.fillMaxWidth().clickable { onSubstanceClick(sub.id) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(if (privacyMode) "Hidden" else sub.name, fontWeight = FontWeight.Bold)
                        Text(sub.category.name, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(if (privacyMode) "***" else "$logs logs", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
