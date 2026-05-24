package com.example.ui.screens.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DonutChart(
    data: List<Pair<String, Float>>,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer
    ),
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    val total = data.sumOf { it.second.toDouble() }.toFloat()
    if (total == 0f) return

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val strokeWidth = size.minDimension * 0.2f
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        
        var startAngle = -90f
        
        data.forEachIndexed { index, pair ->
            val sweepAngle = (pair.second / total) * 360f
            val color = colors[index % colors.size]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun SimpleLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (dataPoints.isEmpty()) return
    val max = dataPoints.maxOrNull() ?: 1f
    val min = dataPoints.minOrNull() ?: 0f
    val range = if (max == min) 1f else (max - min)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)
        
        val path = Path()
        dataPoints.forEachIndexed { i, value ->
            val x = i * stepX
            val normY = (value - min) / range
            val y = h - (normY * h)
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
