package com.example.ui.screens.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.screens.dashboard.GraphMode
import com.example.ui.screens.dashboard.KineticPoint

@Composable
fun KineticGraph(points: List<KineticPoint>, mode: GraphMode, onModeToggle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            SectionHeader(title = "Kinetic Profile", icon = Icons.Default.AvTimer)
            Text(
                text = if(mode == GraphMode.INFLUENCE) "INFLUENCE %" else "CONCENTRATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onModeToggle() }.padding(8.dp)
            )
        }

        GlassCard(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            if (points.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No active data for graph.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@GlassCard
            }
            
            val primaryColor = MaterialTheme.colorScheme.primary
            
            Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 16.dp)) {
                val maxVal = points.maxOf { it.value }.coerceAtLeast(10f)
                val minTime = points.first().timeMs
                val maxTime = points.last().timeMs
                val timeRange = maxTime - minTime
                if (timeRange <= 0L) return@Canvas
                
                val path = Path()
                points.forEachIndexed { i, p ->
                    val x = size.width * ((p.timeMs - minTime).toFloat() / timeRange)
                    val y = size.height - (size.height * (p.value / maxVal))
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
                
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                
                val now = System.currentTimeMillis()
                val nowX = size.width * ((now - minTime).toFloat() / timeRange)
                if(nowX in 0f..size.width) {
                    drawLine(
                        color = primaryColor.copy(alpha = 0.5f),
                        start = Offset(nowX, 0f),
                        end = Offset(nowX, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }
        }
    }
}
