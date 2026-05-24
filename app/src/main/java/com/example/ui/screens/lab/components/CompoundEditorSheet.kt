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
import com.example.domain.model.Compound

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompoundEditorSheet(
    compound: Compound,
    isNew: Boolean,
    onUpdate: (Compound) -> Unit,
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
                if (isNew) "New Compound" else "Edit Compound",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = compound.name,
                onValueChange = { onUpdate(compound.copy(name = it)) },
                label = { Text("Compound Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = compound.halfLifeHours?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(halfLifeHours = it.toFloatOrNull())) },
                    label = { Text("Half-life (h)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = compound.molecularWeight?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(molecularWeight = it.toFloatOrNull())) },
                    label = { Text("Mol. Weight (g/mol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = compound.potencyMultiplier.toString(),
                    onValueChange = { onUpdate(compound.copy(potencyMultiplier = it.toDoubleOrNull() ?: 1.0)) },
                    label = { Text("Potency Multiplier (1.0 = base)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = compound.colorHex,
                    onValueChange = { onUpdate(compound.copy(colorHex = it)) },
                    label = { Text("Color Hex") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Text("Kinetic Profile", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = compound.onsetMin?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(onsetMin = it.toIntOrNull())) },
                    label = { Text("Onset (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = compound.peakMin?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(peakMin = it.toIntOrNull())) },
                    label = { Text("Peak (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = compound.durationHours?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(durationHours = it.toFloatOrNull())) },
                    label = { Text("Duration (h)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Dosage Profile", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = compound.thresholdDose?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(thresholdDose = it.toFloatOrNull())) },
                    label = { Text("Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = compound.commonDose?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(commonDose = it.toFloatOrNull())) },
                    label = { Text("Common") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = compound.strongDose?.toString() ?: "",
                    onValueChange = { onUpdate(compound.copy(strongDose = it.toFloatOrNull())) },
                    label = { Text("Strong") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = compound.name.isNotBlank()
            ) {
                Text("Save Compound")
            }
        }
    }
}
