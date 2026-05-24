package com.example.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppSettings
import com.example.ui.components.GlassCard

@Composable
fun AppearanceSection(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    val themes = listOf("System", "Light", "Dark")
    val palettes = listOf("Cosmic", "Forest", "Ocean", "Sunset", "Lavender")
    
    var expandedTheme by remember { mutableStateOf(false) }
    var expandedPalette by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Theme Mode
            Box {
                OutlinedTextField(
                    value = settings.themeMode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Theme Mode") },
                    modifier = Modifier.fillMaxWidth().clickable { expandedTheme = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                DropdownMenu(
                    expanded = expandedTheme,
                    onDismissRequest = { expandedTheme = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    themes.forEach { tm ->
                        DropdownMenuItem(
                            text = { Text(tm) },
                            onClick = {
                                onUpdate { it.copy(themeMode = tm) }
                                expandedTheme = false
                            }
                        )
                    }
                }
            }

            // Accent Palette
            Box {
                OutlinedTextField(
                    value = settings.accentPalette,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Accent Palette") },
                    modifier = Modifier.fillMaxWidth().clickable { expandedPalette = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                DropdownMenu(
                    expanded = expandedPalette,
                    onDismissRequest = { expandedPalette = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    palettes.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p) },
                            onClick = {
                                onUpdate { it.copy(accentPalette = p) }
                                expandedPalette = false
                            }
                        )
                    }
                }
            }
        }
    }
}
