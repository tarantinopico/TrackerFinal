package com.example.ui.screens.lab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.Substance
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScreen(
    substances: List<Substance>,
    onAddSubstance: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Laboratory") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSubstance) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(substances) { substance ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(substance.name, style = MaterialTheme.typography.titleLarge)
                        Text("Category: ${substance.category.name}", style = MaterialTheme.typography.bodyMedium)
                        Text(substance.notes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
