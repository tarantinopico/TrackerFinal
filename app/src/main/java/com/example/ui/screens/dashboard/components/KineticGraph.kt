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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.screens.dashboard.GraphMode
import com.example.ui.screens.dashboard.KineticLine

@Composable
fun KineticGraph(lines: List<KineticLine>, mode: GraphMode, onModeToggle: () -> Unit) {
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

        GlassCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            if (lines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No active data for graph.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                return@GlassCard
            }
            
            Canvas(modifier = Modifier.fillMaxSize().padding(top = 24.dp, bottom = 24.dp)) {
                var maxVal = 0f
                var minTime = Long.MAX_VALUE
                var maxTime = Long.MIN_VALUE
                
                for (line in lines) {
                    for (p in line.points) {
                        if (p.value > maxVal) maxVal = p.value
                        if (p.timeMs < minTime) minTime = p.timeMs
                        if (p.timeMs > maxTime) maxTime = p.timeMs
                    }
                }
                
                maxVal = if (maxVal > 0f) maxVal * 1.1f else 1f
                val timeRange = maxTime - minTime
                if (timeRange <= 0L) return@Canvas
                
                // Draw Y axis labels
                val labelPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY // text color
                    textSize = 10.dp.toPx()
                }
                for (i in 0..4) {
                    val v = maxVal * (i / 4f)
                    val labelText = if (mode == GraphMode.INFLUENCE) "${v.toInt()}%" else String.format("%.1f", v)
                    val y = size.height - (size.height * (i / 4f))
                    // Ensure the top label is slightly pushed down to be fully visible
                    val drawY = if (i == 4) y + 10.dp.toPx() else if (i == 0) y - 2.dp.toPx() else y
                    drawContext.canvas.nativeCanvas.drawText(labelText, 0f, drawY, labelPaint)
                }

                // Draw X axis labels (times)
                val xSteps = 6
                for (i in 0..xSteps) {
                    val timeForLabel = minTime + (timeRange * (i.toFloat() / xSteps)).toLong()
                    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    val timeStr = formatter.format(java.util.Date(timeForLabel))
                    
                    val xPos = size.width * (i.toFloat() / xSteps)
                    // Draw text label at the bottom
                    drawContext.canvas.nativeCanvas.drawText(
                        timeStr, 
                        if (i == 0) xPos else if (i == xSteps) xPos - 20.dp.toPx() else xPos - 10.dp.toPx(), 
                        size.height + 16.dp.toPx(), 
                        labelPaint
                    )
                }
                
                lines.forEach { line ->
                    val path = Path()
                    val color = Color(android.graphics.Color.parseColor(line.colorHex))
                    
                    line.points.forEachIndexed { i, p ->
                        val x = size.width * ((p.timeMs - minTime).toFloat() / timeRange)
                        val y = size.height - (size.height * (p.value / maxVal))
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    
                    // Draw fill
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    
                    // Draw stroke
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                
                // Draw current time marker
                val now = System.currentTimeMillis()
                val nowX = size.width * ((now - minTime).toFloat() / timeRange)
                if(nowX in 0f..size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(nowX, 0f),
                        end = Offset(nowX, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }
            }
        }
        
        // Custom Legend under the graph
        if (lines.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                lines.forEach { line ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(Color(android.graphics.Color.parseColor(line.colorHex)))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(line.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
