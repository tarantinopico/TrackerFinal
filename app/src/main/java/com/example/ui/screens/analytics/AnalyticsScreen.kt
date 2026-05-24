package com.example.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.domain.model.Dose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(logs: List<Dose>) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Analytics & History") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Text("Recent Usage Frequency", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple Canvas bar chart
            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val barWidth = 30f
                val spacing = size.width / 10
                
                // Draw random bars to simulate chart
                for (i in 0..6) {
                    val h = (Math.random() * size.height).toFloat()
                    drawLine(
                        color = Color(0xFF00E676),
                        start = Offset(i * spacing + spacing, size.height),
                        end = Offset(i * spacing + spacing, size.height - h),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Calendar History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(31) { day ->
                    val hasLog = Math.random() > 0.7
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (hasLog) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${day + 1}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
