package com.example.ui.screens.logger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.theme.DangerRed

@Composable
fun LoggerWarnings(
    warningMessage: String?,
    estimatedOnset: Int?,
    estimatedPeak: Int?,
    estimatedDuration: Float?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        if (warningMessage != null) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Warning", tint = DangerRed)
                    Text(
                        text = warningMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = DangerRed
                    )
                }
            }
        }
        
        if (estimatedOnset != null || estimatedDuration != null) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    if (estimatedOnset != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Onset", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("~$estimatedOnset m", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (estimatedPeak != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Peak", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("~$estimatedPeak m", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (estimatedDuration != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Duration", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("~${"%.1f".format(estimatedDuration)} h", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
