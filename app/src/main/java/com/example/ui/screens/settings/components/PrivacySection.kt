package com.example.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppSettings
import com.example.ui.components.GlassCard

@Composable
fun PrivacySection(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "App Behavior",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            SwitchRow(
                label = "Privacy Mode (Mask names/amounts)",
                checked = settings.privacyMode,
                onCheckedChange = { c -> onUpdate { it.copy(privacyMode = c) } }
            )
            
            SwitchRow(
                label = "Finance Mode",
                checked = settings.financeMode,
                onCheckedChange = { c -> onUpdate { it.copy(financeMode = c) } }
            )
            
            SwitchRow(
                label = "Hide Finance Data (Analytics)",
                checked = settings.hideFinanceMode,
                onCheckedChange = { c -> onUpdate { it.copy(hideFinanceMode = c) } }
            )

            SwitchRow(
                label = "Compact Mode (Smaller lists)",
                checked = settings.compactMode,
                onCheckedChange = { c -> onUpdate { it.copy(compactMode = c) } }
            )
            
            SwitchRow(
                label = "Warnings Enabled",
                checked = settings.warningsEnabled,
                onCheckedChange = { c -> onUpdate { it.copy(warningsEnabled = c) } }
            )
            
            OutlinedTextField(
                value = settings.currency,
                onValueChange = { s -> onUpdate { it.copy(currency = s) } },
                label = { Text("Currency Symbol") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
