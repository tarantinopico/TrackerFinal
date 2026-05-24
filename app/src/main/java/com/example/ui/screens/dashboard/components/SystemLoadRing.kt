package com.example.ui.screens.dashboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange

@Composable
fun SystemLoadRing(loadPercent: Float, isWarning: Boolean) {
    val animatedLoad by animateFloatAsState(
        targetValue = loadPercent, 
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "loadAnimation"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val arcColor = when {
        animatedLoad > 80f -> DangerRed
        animatedLoad > 50f -> WarningOrange
        else -> SuccessGreen
    }
    
    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = arcColor,
                startAngle = 135f,
                sweepAngle = 270f * (animatedLoad / 100f).coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedLoad.toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "System Load",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isWarning) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "CRITICAL LIMIT",
                    color = DangerRed,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
