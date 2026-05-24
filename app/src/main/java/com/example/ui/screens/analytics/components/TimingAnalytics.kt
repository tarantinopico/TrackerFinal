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
import java.util.Calendar

@Composable
fun TimingAnalytics(state: AnalyticsState, privacyMode: Boolean) {
    if (state.doses.isEmpty()) {
        Text("No data available.")
        return
    }

    val cal = Calendar.getInstance()
    val hourCounts = IntArray(24)
    val dayCounts = IntArray(8) // 1-7 for day of week
    
    state.doses.forEach { dose ->
        cal.timeInMillis = dose.timestamp
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val d = cal.get(Calendar.DAY_OF_WEEK)
        hourCounts[h]++
        dayCounts[d]++
    }

    val maxHour = hourCounts.maxOrNull() ?: 1
    val floatsHour = hourCounts.map { it.toFloat() }

    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Circadian Rhythm (Hour of Day)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                if (!privacyMode) {
                    SimpleLineChart(
                        dataPoints = floatsHour,
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                } else {
                    Text("Hidden in privacy mode", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Day of Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                daysOfWeek.forEachIndexed { i, day ->  
                    val idx = i + 1
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day, fontWeight = FontWeight.Bold)
                        Text(if (privacyMode) "***" else "${dayCounts[idx]}")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}
