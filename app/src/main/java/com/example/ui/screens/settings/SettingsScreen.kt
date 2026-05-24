package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.ui.state.AppSettingsState
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettingsState,
    substances: List<Substance>,
    logs: List<Dose>,
    onTogglePrivacy: () -> Unit,
    onUpdateAccent: (String) -> Unit,
    onUpdateTheme: (String) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("System", "Light", "Dark").forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onUpdateTheme(mode) },
                            label = { Text(mode) }
                        )
                    }
                }
            }

            Column {
                Text("Accent Palette", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                com.example.ui.components.AccentSelector(
                    selectedAccent = settings.accentPalette,
                    onAccentSelected = onUpdateAccent
                )
            }
            
            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Privacy Mode", style = MaterialTheme.typography.titleMedium)
                    Text("Hide critical values in UI", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.privacyMode, onCheckedChange = { onTogglePrivacy() })
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Finance Mode", style = MaterialTheme.typography.titleMedium)
                    Text("Track cost of consumption", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.financeMode, onCheckedChange = { /* TO DO in ViewModel */ })
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            val showImportDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { exportJson(context, substances, logs) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Text("Export JSON")
                }
                Button(
                    onClick = { showImportDialog.value = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("Import JSON")
                }
            }
            
            if (showImportDialog.value) {
                val jsonInput = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showImportDialog.value = false },
                    title = { Text("Import JSON Data") },
                    text = {
                        OutlinedTextField(
                            value = jsonInput.value,
                            onValueChange = { jsonInput.value = it },
                            label = { Text("Paste JSON here") },
                            modifier = Modifier.fillMaxWidth().height(150.dp)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { 
                            showImportDialog.value = false 
                        }) {
                            Text("Import")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImportDialog.value = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

private fun exportJson(context: Context, substances: List<Substance>, logs: List<Dose>) {
    try {
        val root = JSONObject()
        
        val subsArray = JSONArray()
        substances.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("category", it.category.name)
            subsArray.put(obj)
        }
        
        val logsArray = JSONArray()
        logs.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("substanceId", it.substanceId)
            obj.put("amount", it.doseAmount)
            obj.put("timestamp", it.timestamp)
            logsArray.put(obj)
        }
        
        root.put("substances", subsArray)
        root.put("logs", logsArray)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, root.toString(2))
            putExtra(Intent.EXTRA_TITLE, "BioTrack Export")
        }
        context.startActivity(Intent.createChooser(intent, "Share JSON"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
