package com.example.ui.screens.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb

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
    lineColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (dataPoints.isEmpty()) return
    val max = dataPoints.maxOrNull() ?: 1f
    val min = dataPoints.minOrNull() ?: 0f
    val range = if (max == min) 1f else (max - min)

    val textColor = labelColor.toArgb()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padding = 40.dp.toPx()
        val graphW = w - padding
        val graphH = h - padding
        
        val stepX = if (dataPoints.size > 1) graphW / (dataPoints.size - 1) else graphW

        val path = Path()
        dataPoints.forEachIndexed { i, value ->
            val x = padding + i * stepX
            val normY = (value - min) / range
            val y = graphH - (normY * graphH)
            
            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }
        
        // Draw Axes
        drawLine(
            color = labelColor.copy(alpha = 0.5f),
            start = Offset(padding, 0f),
            end = Offset(padding, graphH),
            strokeWidth = 2f
        )
        drawLine(
            color = labelColor.copy(alpha = 0.5f),
            start = Offset(padding, graphH),
            end = Offset(w, graphH),
            strokeWidth = 2f
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw Labels
        val textPaint = Paint().apply {
            color = textColor
            textSize = 10.dp.toPx()
            textAlign = Paint.Align.RIGHT
        }
        drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", max), padding - 10, 20f, textPaint)
        drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", min), padding - 10, graphH, textPaint)
    }
}

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (data.isEmpty()) return
    val max = data.maxOfOrNull { it.second } ?: 1f
    val textColor = labelColor.toArgb()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingBottom = 40.dp.toPx()
        val paddingLeft = 40.dp.toPx()
        val graphW = w - paddingLeft
        val graphH = h - paddingBottom
        
        val availableBarPaddedW = graphW / data.size
        val barW = availableBarPaddedW * 0.7f
        val spaceW = availableBarPaddedW * 0.3f
        
        val textPaint = Paint().apply {
            color = textColor
            textSize = 10.dp.toPx()
            textAlign = Paint.Align.CENTER
        }
        
        val labelPaint = Paint().apply {
            color = textColor
            textSize = 10.dp.toPx()
            textAlign = Paint.Align.RIGHT
        }
        
        drawContext.canvas.nativeCanvas.drawText(String.format("%.0f", max), paddingLeft - 10f, 20f, labelPaint)

        data.forEachIndexed { i, pair ->
            val barH = (pair.second / max) * graphH
            val x = paddingLeft + (i * availableBarPaddedW) + (spaceW / 2)
            val y = graphH - barH
            
            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barW, barH)
            )
            
            // X label
            drawContext.canvas.nativeCanvas.drawText(
                pair.first.take(5), 
                x + (barW / 2), 
                h - 10f, 
                textPaint
            )
        }
    }
}
