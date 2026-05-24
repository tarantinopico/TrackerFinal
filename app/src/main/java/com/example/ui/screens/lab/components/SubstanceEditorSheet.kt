package com.example.ui.screens.lab.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Substance
import com.example.domain.model.SubstanceCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstanceEditorSheet(
    substance: Substance,
    isNew: Boolean,
    onUpdate: (Substance) -> Unit,
    onSave: () -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (isNew) "New Substance" else "Edit Substance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = substance.name,
                onValueChange = { onUpdate(substance.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = substance.alias,
                onValueChange = { onUpdate(substance.copy(alias = it)) },
                label = { Text("Alias (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubstanceCategory.values().forEach { cat ->
                    FilterChip(
                        selected = substance.category == cat,
                        onClick = { onUpdate(substance.copy(category = cat)) },
                        label = { Text(cat.name) }
                    )
                }
            }

            OutlinedTextField(
                value = substance.defaultUnit,
                onValueChange = { onUpdate(substance.copy(defaultUnit = it)) },
                label = { Text("Default Unit (e.g., mg, µg)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = substance.notes,
                onValueChange = { onUpdate(substance.copy(notes = it)) },
                label = { Text("Notes & Target Effects") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = substance.name.isNotBlank() && substance.defaultUnit.isNotBlank()
            ) {
                Text("Save Substance")
            }
        }
    }
}
