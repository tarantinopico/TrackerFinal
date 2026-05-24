package com.example.ui.screens.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.screens.dashboard.GraphMode
import com.example.ui.screens.dashboard.KineticLine


@Composable
fun KineticGraph(lines: List<KineticLine>, mode: GraphMode, onModeToggle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                "PHARMACOKINETICS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            GlassCard(
                padding = 6.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                onClick = onModeToggle
            ) {
                Text(
                    text = if(mode == GraphMode.INFLUENCE) "INFLUENCE %" else "CONCENTRATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth().height(260.dp), padding = 16.dp) {
            if (lines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No active data for graph.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                return@GlassCard
            }
            
            Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 40.dp)) {
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
                
                maxVal = if (maxVal > 0f) maxVal * 1.15f else 1f
                val timeRange = maxTime - minTime
                if (timeRange <= 0L) return@Canvas
                
                // Draw Y axis labels
                val labelPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#80FFFFFF") // subtle
                    textSize = 10.dp.toPx()
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                for (i in 0..3) {
                    val v = maxVal * (i / 3f)
                    val labelText = if (mode == GraphMode.INFLUENCE) {
                        "${v.toInt()}%"
                    } else {
                        if (maxVal < 1f && maxVal > 0f) {
                            String.format("%.3f", v)
                        } else if (maxVal < 10f) {
                            String.format("%.1f", v)
                        } else {
                            v.toInt().toString()
                        }
                    }
                    val y = size.height - (size.height * (i / 3f))
                    val drawY = if (i == 3) y + 10.dp.toPx() else if (i == 0) y - 2.dp.toPx() else y
                    drawContext.canvas.nativeCanvas.drawText(labelText, 0f, drawY, labelPaint)
                    
                    // Draw horizontal subtle grid line
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw X axis labels (times)
                val xSteps = 4
                for (i in 0..xSteps) {
                    val timeForLabel = minTime + (timeRange * (i.toFloat() / xSteps)).toLong()
                    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                    val timeStr = formatter.format(java.util.Date(timeForLabel))
                    
                    val xPos = size.width * (i.toFloat() / xSteps)
                    drawContext.canvas.nativeCanvas.drawText(
                        timeStr, 
                        if (i == 0) xPos else if (i == xSteps) xPos - 24.dp.toPx() else xPos - 12.dp.toPx(), 
                        size.height + 24.dp.toPx(), 
                        labelPaint
                    )
                }
                
                lines.forEach { line ->
                    val path = Path()
                    val color = try { Color(android.graphics.Color.parseColor(line.colorHex)) } catch (e: Exception) { Color.Green }
                    
                    var prevX = 0f
                    var prevY = 0f
                    
                    line.points.forEachIndexed { i, p ->
                        val x = size.width * ((p.timeMs - minTime).toFloat() / timeRange)
                        val y = size.height - (size.height * (p.value / maxVal))
                        
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            // Bezier smoothing
                            val cp1x = prevX + (x - prevX) / 2f
                            val cp1y = prevY
                            val cp2x = prevX + (x - prevX) / 2f
                            val cp2y = y
                            path.cubicTo(cp1x, cp1y, cp2x, cp2y, x, y)
                        }
                        prevX = x
                        prevY = y
                    }
                    
                    // Draw fill
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(prevX, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
                            startY = 0f,
                            endY = size.height
                        )
                    )
                    
                    // Draw stroke with neon glow
                    // Fake glow by drawing multiple larger semi-transparent strokes
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.3f),
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = path,
                        color = color.copy(alpha = 0.6f),
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                
                // Draw current time marker
                val now = System.currentTimeMillis()
                val nowX = size.width * ((now - minTime).toFloat() / timeRange)
                if (nowX in 0f..size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.8f),
                        start = Offset(nowX, 0f),
                        end = Offset(nowX, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                    )
                    // "NOW" label
                    drawContext.canvas.nativeCanvas.drawText(
                        "NOW",
                        nowX + 4.dp.toPx(),
                        20.dp.toPx(),
                        labelPaint.apply { color = android.graphics.Color.WHITE }
                    )
                }
            }
        }
        
        // Custom Legend under the graph
        if (lines.isNotEmpty()) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                lines.forEach { line ->
                    val color = try { Color(android.graphics.Color.parseColor(line.colorHex)) } catch (e: Exception) { Color.Green }
                    Row(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.15f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .border(1.dp, color.copy(alpha = 0.3f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(line.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

