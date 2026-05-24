package com.example.ui.screens.logger.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.QuickDose
import com.example.domain.model.Substance
import com.example.domain.model.Variant
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickDoseSheet(
    quickDoses: List<QuickDose>,
    substances: List<Substance>,
    variants: List<Variant>,
    onDismissRequest: () -> Unit,
    onQuickDoseSelected: (QuickDose) -> Unit,
    onSaveQuickDose: (QuickDose) -> Unit,
    onDeleteQuickDose: (String) -> Unit,
    sheetState: SheetState
) {
    var editingQuickDose by remember { mutableStateOf<QuickDose?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (isEditing) {
            QuickDoseEditor(
                quickDose = editingQuickDose,
                substances = substances,
                variants = variants,
                onSave = { 
                    onSaveQuickDose(it)
                    isEditing = false
                },
                onDelete = { id -> 
                    onDeleteQuickDose(id)
                    isEditing = false
                },
                onCancel = { isEditing = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick Doses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { 
                        editingQuickDose = null
                        isEditing = true
                    }) {
                        Text("Add New")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                if (quickDoses.isEmpty()) {
                    Text("No Quick Doses saved yet. Tap 'Add New' to create one.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    quickDoses.forEach { qd ->
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(qd.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${qd.defaultAmount} ${qd.defaultUnit} • ${qd.defaultRoute}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { 
                                            editingQuickDose = qd
                                            isEditing = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Quick Dose", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    FilledIconButton(
                                        onClick = { onQuickDoseSelected(qd) },
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = "Use Quick Dose", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickDoseEditor(
    quickDose: QuickDose?,
    substances: List<Substance>,
    variants: List<Variant>,
    onSave: (QuickDose) -> Unit,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var label by remember { mutableStateOf(quickDose?.label ?: "") }
    var amountStr by remember { mutableStateOf(quickDose?.defaultAmount?.toString() ?: "") }
    var selectedSubstanceId by remember { mutableStateOf(quickDose?.substanceId ?: substances.firstOrNull()?.id ?: "") }
    var selectedVariantId by remember { mutableStateOf(quickDose?.variantId ?: "") }
    var route by remember { mutableStateOf(quickDose?.defaultRoute ?: "Oral") }
    var priceStr by remember { mutableStateOf(quickDose?.defaultPrice?.toString() ?: "") }

    val activeSubstance = substances.find { it.id == selectedSubstanceId }
    val availableVariants = if (activeSubstance != null) variants.filter { it.substanceId == activeSubstance.id } else emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(if (quickDose == null) "New Quick Dose" else "Edit Quick Dose", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth()
        )

        SubstancePicker(
            substances = substances,
            selectedSubstance = activeSubstance,
            onSubstanceSelected = { 
                selectedSubstanceId = it.id 
                selectedVariantId = ""
            }
        )

        if (availableVariants.isNotEmpty()) {
            VariantPicker(
                variants = availableVariants,
                selectedVariant = availableVariants.find { it.id == selectedVariantId },
                onVariantSelected = { selectedVariantId = it?.id ?: "" }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = route,
                onValueChange = { route = it },
                label = { Text("Route") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text("Price (Optional)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (quickDose != null) {
                Button(
                    onClick = { onDelete(quickDose.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val amount = amountStr.toFloatOrNull() ?: 0f
                    val price = priceStr.toFloatOrNull()
                    val newQd = QuickDose(
                        id = quickDose?.id ?: java.util.UUID.randomUUID().toString(),
                        label = label.takeIf { it.isNotBlank() } ?: "Unnamed",
                        substanceId = selectedSubstanceId.takeIf { it.isNotEmpty() },
                        variantId = selectedVariantId.takeIf { it.isNotEmpty() },
                        defaultAmount = amount,
                        defaultUnit = activeSubstance?.defaultUnit ?: "mg",
                        defaultRoute = route,
                        defaultPrice = price
                    )
                    onSave(newQd)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}
