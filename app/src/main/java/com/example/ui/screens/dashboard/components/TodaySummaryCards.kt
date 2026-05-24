package com.example.ui.screens.dashboard.components
    
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.state.AppSettingsState
import com.example.ui.screens.dashboard.DashboardState
import com.example.ui.theme.DangerRed

@Composable
fun TodaySummaryCards(state: DashboardState, settings: AppSettingsState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Doses Today",
                value = state.todayDoseCount.toString(),
                icon = Icons.Default.Science
            )
            val spendStr = if (settings.privacyMode || !settings.financeMode) "•••" else "$ ${"%.2f".format(state.todaySpend)}"
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Spent Today",
                value = spendStr,
                icon = Icons.Default.AttachMoney,
                subtitle = if(state.spendTrend7d != 0f && !settings.privacyMode) "${if(state.spendTrend7d > 0) "+" else ""}${"%.1f".format(state.spendTrend7d)}% (7d)" else null
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Active Subs",
                value = state.activeSubstancesCount.toString(),
                icon = Icons.Default.Category
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                title = "Load Status",
                value = if(state.isWarningHighLoad && settings.warningsEnabled) "High" else "Optimal",
                icon = if(state.isWarningHighLoad && settings.warningsEnabled) Icons.Default.Warning else Icons.Default.CheckCircle,
                valueColor = if(state.isWarningHighLoad && settings.warningsEnabled) DangerRed else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    subtitle: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    GlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = valueColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
