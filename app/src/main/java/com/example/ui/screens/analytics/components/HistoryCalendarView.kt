package com.example.ui.screens.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.screens.analytics.AnalyticsState
import java.util.Calendar

@Composable
fun HistoryCalendarView(state: AnalyticsState, privacyMode: Boolean) {
    if (state.doses.isEmpty()) {
        Text("No data available.")
        return
    }

    val firstDay = state.settings?.firstDayOfWeek ?: Calendar.SUNDAY
    
    // We want to show the last 12 weeks.
    // Calculate the start of the week for 12 weeks ago.
    val startDay = Calendar.getInstance().apply {
        firstDayOfWeek = firstDay
        add(Calendar.WEEK_OF_YEAR, -11)
        set(Calendar.DAY_OF_WEEK, firstDay)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    
    val doseCountByDayOfYear = state.doses.map {
        val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        val y = c.get(Calendar.YEAR)
        val d = c.get(Calendar.DAY_OF_YEAR)
        Pair(y, d)
    }.groupBy { it }.mapValues { it.value.size }

    val cColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Activity Heatmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                if (privacyMode) {
                    Text("Hidden in privacy mode", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val cellSize = 12.dp.toPx()
                        val padding = 4.dp.toPx()
                        val totalW = (cellSize + padding) * 12 // columns (weeks)
                        val totalH = (cellSize + padding) * 7  // rows (days)
                        
                        val startX = (size.width - totalW) / 2
                        val startY = (size.height - totalH) / 2
                        
                        // Fake a quick grid iteration
                        for (w in 0..11) {
                            for (d in 0..6) {
                                val cal = startDay.clone() as Calendar
                                cal.add(Calendar.DAY_OF_YEAR, (w * 7) + d)
                                
                                val key = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
                                val count = doseCountByDayOfYear[key] ?: 0
                                
                                val alpha = when {
                                    count == 0 -> 0.1f
                                    count < 3 -> 0.4f
                                    count < 5 -> 0.7f
                                    else -> 1.0f
                                }
                                
                                drawRoundRect(
                                    color = cColor.copy(alpha = alpha),
                                    topLeft = Offset(startX + (w * (cellSize + padding)), startY + (d * (cellSize + padding))),
                                    size = Size(cellSize, cellSize),
                                    cornerRadius = CornerRadius(2.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
