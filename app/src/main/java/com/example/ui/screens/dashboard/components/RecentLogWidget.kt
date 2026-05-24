package com.example.ui.screens.dashboard.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.ui.components.GlassCard

@Composable
fun RecentLogWidget(logs: List<Pair<Dose, Substance>>, privacyMode: Boolean, compactMode: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compactMode) 6.dp else 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
        
        if (logs.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No recent activity.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), padding = 0.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    logs.forEachIndexed { index, (dose, sub) ->
                        val subColor = try { Color(android.graphics.Color.parseColor(sub.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = if (compactMode) 12.dp else 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(subColor)
                                )
                                Column {
                                    Text(
                                        if (privacyMode) "Hidden Target" else sub.name, 
                                        fontWeight = FontWeight.SemiBold, 
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        SimpleDateFormat("HH:mm • dd MMM", Locale.getDefault()).format(Date(dose.timestamp)), 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            val amountText = if(privacyMode) "••" else "${dose.doseAmount} ${dose.unit}"
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = amountText, 
                                    style = MaterialTheme.typography.labelLarge, 
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (index < logs.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 44.dp), 
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}

