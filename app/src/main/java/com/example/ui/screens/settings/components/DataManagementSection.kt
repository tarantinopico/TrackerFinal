package com.example.ui.screens.settings.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataManagementSection(
    onExport: (Uri) -> Unit,
    onImport: (Uri, Boolean) -> Unit
) {
    var showImportDialog by remember { mutableStateOf<Uri?>(null) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { onExport(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { showImportDialog = it }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Data Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Text("Export your entire database to a JSON file, or import from an existing backup. Import can either merge or completely overwrite your current data.", style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
                        val filename = "biotrack_backup_${sdf.format(Date())}.json"
                        exportLauncher.launch(filename)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export")
                }
                
                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Import")
                }
            }
        }
    }
    
    if (showImportDialog != null) {
        var overwrite by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showImportDialog = null },
            title = { Text("Import Database") },
            text = {
                Column {
                    Text("How would you like to import data from this backup?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = !overwrite, onClick = { overwrite = false })
                        Text("Merge (Keep existing, add new)")
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = overwrite, onClick = { overwrite = true })
                        Text("Overwrite (Delete all existing data)", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showImportDialog?.let { uri -> onImport(uri, overwrite) }
                    showImportDialog = null
                }) {
                    Text("Proceed")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
