package com.example.ui.screens.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard

@Composable
fun SimpleBarChart(
    title: String,
    data: Map<String, Float>,
    colorList: List<Color> = listOf(MaterialTheme.colorScheme.primary),
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (data.isEmpty()) {
                Text("No data available", style = MaterialTheme.typography.bodyMedium)
                return@GlassCard
            }
            
            val maxVal = data.values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var idx = 0
                data.forEach { (label, value) ->
                    val color = colorList[idx % colorList.size]
                    idx++
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.3f))
                        LinearProgressIndicator(
                            progress = { value / maxVal },
                            modifier = Modifier.weight(0.5f).height(8.dp),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = String.format("%.1f", value),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.2f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
