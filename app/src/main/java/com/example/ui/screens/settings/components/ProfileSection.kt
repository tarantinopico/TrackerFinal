package com.example.ui.screens.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppSettings
import com.example.ui.components.GlassCard

@Composable
fun ProfileSection(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = settings.userWeightKg.toString(),
                onValueChange = { s -> 
                    val w = s.toFloatOrNull()
                    if (w != null) onUpdate { it.copy(userWeightKg = w) }
                },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = settings.userAge.toString(),
                onValueChange = { s -> 
                    val a = s.toIntOrNull()
                    if (a != null) onUpdate { it.copy(userAge = a) }
                },
                label = { Text("Age (years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = settings.metabolismFactor.toString(),
                onValueChange = { s -> 
                    val m = s.toFloatOrNull()
                    if (m != null) onUpdate { it.copy(metabolismFactor = m) }
                },
                label = { Text("Metabolism Factor (1.0 default)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
