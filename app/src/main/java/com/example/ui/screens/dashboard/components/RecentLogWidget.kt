package com.example.ui.screens.dashboard.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader

@Composable
fun RecentLogWidget(logs: List<Pair<Dose, Substance>>, privacyMode: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Recent Logs", icon = Icons.Default.History, modifier = Modifier.padding(bottom = 8.dp))
        
        if (logs.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("No recent logs.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            logs.forEach { (dose, sub) ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween, 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(sub.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                SimpleDateFormat("HH:mm - dd.MM", Locale.getDefault()).format(Date(dose.timestamp)), 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val amountText = if(privacyMode) "••" else "${dose.doseAmount} ${dose.unit}"
                        Text(
                            text = amountText, 
                            style = MaterialTheme.typography.titleMedium, 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
