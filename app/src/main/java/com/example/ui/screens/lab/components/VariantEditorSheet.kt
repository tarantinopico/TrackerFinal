package com.example.ui.screens.lab.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.Variant

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VariantEditorSheet(
    variant: Variant,
    isNew: Boolean,
    onUpdate: (Variant) -> Unit,
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
                if (isNew) "New Variant" else "Edit Variant",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = variant.name,
                onValueChange = { onUpdate(variant.copy(name = it)) },
                label = { Text("Variant Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = variant.pricePerUnit?.toString() ?: "",
                    onValueChange = { onUpdate(variant.copy(pricePerUnit = it.toFloatOrNull())) },
                    label = { Text("Price Per Unit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = variant.unitLabel,
                    onValueChange = { onUpdate(variant.copy(unitLabel = it)) },
                    label = { Text("Unit (e.g. mg)") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Text("Default Route of Administration", style = MaterialTheme.typography.labelLarge)
            val routes = listOf("Oral", "Sublingual", "Intranasal", "Inhalation", "Intravenous")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                routes.forEach { roa ->
                    FilterChip(
                        selected = variant.roaDefault == roa,
                        onClick = { onUpdate(variant.copy(roaDefault = roa)) },
                        label = { Text(roa) }
                    )
                }
            }
            
            OutlinedTextField(
                value = variant.colorHex,
                onValueChange = { onUpdate(variant.copy(colorHex = it)) },
                label = { Text("Color (Hex code, e.g. #FF0000)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = variant.name.isNotBlank() && variant.unitLabel.isNotBlank()
            ) {
                Text("Save Variant")
            }
        }
    }
}
