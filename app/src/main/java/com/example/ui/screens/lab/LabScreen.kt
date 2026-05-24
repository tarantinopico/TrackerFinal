package com.example.ui.screens.lab

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.screens.lab.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabScreen(viewModel: LabViewModel) {
    val state by viewModel.state.collectAsState()
    
    // Bottom Sheets
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    if (state.isEditingNewSubstance || state.editingSubstance != null) {
        val sub = state.editingSubstance 
        if (sub != null) {
            SubstanceEditorSheet(
                substance = sub,
                isNew = state.isEditingNewSubstance,
                onUpdate = { viewModel.updateEditingSubstance(it) },
                onSave = { viewModel.saveSubstance() },
                onDismissRequest = { viewModel.closeSubstanceEditor() },
                sheetState = sheetState
            )
        }
    }
    
    if (state.isEditingNewCompound || state.editingCompound != null) {
        val cmp = state.editingCompound
        if (cmp != null) {
            CompoundEditorSheet(
                compound = cmp,
                isNew = state.isEditingNewCompound,
                onUpdate = { viewModel.updateEditingCompound(it) },
                onSave = { viewModel.saveCompound() },
                onDismissRequest = { viewModel.closeCompoundEditor() },
                sheetState = sheetState
            )
        }
    }
    
    if (state.isEditingNewVariant || state.editingVariant != null) {
        val vrt = state.editingVariant
        if (vrt != null) {
            VariantEditorSheet(
                variant = vrt,
                isNew = state.isEditingNewVariant,
                onUpdate = { viewModel.updateEditingVariant(it) },
                onSave = { viewModel.saveVariant() },
                onDismissRequest = { viewModel.closeVariantEditor() },
                sheetState = sheetState
            )
        }
    }
    
    // Delete Confirmations
    if (state.deleteConfirmSubstance != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteSubstance() },
            title = { Text("Delete Substance") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.performDeleteSubstance() }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDeleteSubstance() }) { Text("Cancel") } }
        )
    }
    if (state.deleteConfirmCompound != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteCompound() },
            title = { Text("Delete Compound") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.performDeleteCompound() }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDeleteCompound() }) { Text("Cancel") } }
        )
    }
    if (state.deleteConfirmVariant != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteVariant() },
            title = { Text("Delete Variant") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.performDeleteVariant() }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDeleteVariant() }) { Text("Cancel") } }
        )
    }

    Scaffold(
        floatingActionButton = {
            if (state.viewingSubstanceId == null) {
                FloatingActionButton(onClick = { viewModel.openNewSubstanceEditor() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Substance")
                }
            }
        }
    ) { padding ->
        Crossfade(targetState = state.viewingSubstanceId != null, label = "LabViewMode") { isDetail ->
            if (isDetail) {
                // Detail Mode
                val substance = state.selectedSubstance
                if (substance != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.viewSubstance(null) }, modifier = Modifier.padding(end = 8.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            SubstanceDetailHeader(
                                substance = substance,
                                onEdit = { viewModel.openSubstanceEditor(substance) },
                                onArchiveToggle = { viewModel.archiveSubstance(substance, archive = substance.active) },
                                onDelete = { viewModel.confirmDeleteSubstance(substance) }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                            // Compounds
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Compounds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { viewModel.openNewCompoundEditor() }) { Text("+ Add") }
                                }
                            }
                            if (state.selectedCompounds.isEmpty()) {
                                item { Text("No compounds defined.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            } else {
                                items(state.selectedCompounds) { cmp ->
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(cmp.name, fontWeight = FontWeight.Bold)
                                                Text("Half-life: ${cmp.halfLifeHours ?: "?"} h", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row {
                                                TextButton(onClick = { viewModel.openCompoundEditor(cmp) }) { Text("Edit") }
                                                TextButton(onClick = { viewModel.confirmDeleteCompound(cmp) }) { Text("Del") }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Variants
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Variants (Preparations)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    TextButton(onClick = { viewModel.openNewVariantEditor() }) { Text("+ Add") }
                                }
                            }
                            if (state.selectedVariants.isEmpty()) {
                                item { Text("No variants defined.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            } else {
                                items(state.selectedVariants) { varnt ->
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(varnt.name, fontWeight = FontWeight.Bold)
                                                Text("${varnt.roaDefault} • ${varnt.pricePerUnit ?: "?"} / ${varnt.unitLabel}", style = MaterialTheme.typography.bodySmall)
                                            }
                                            Row {
                                                TextButton(onClick = { viewModel.openVariantEditor(varnt) }) { Text("Edit") }
                                                TextButton(onClick = { viewModel.confirmDeleteVariant(varnt) }) { Text("Del") }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            item { Spacer(modifier = Modifier.height(60.dp)) }
                        }
                    }
                }
            } else {
                // List Mode
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeader(title = "Laboratory", icon = Icons.Default.Science)
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabFilter.values().forEach { flt ->
                            FilterChip(
                                selected = state.filter == flt,
                                onClick = { viewModel.setFilter(flt) },
                                label = { Text(flt.name) }
                            )
                        }
                    }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        SubstanceGrid(
                            substances = state.substances,
                            onSubstanceClick = { viewModel.viewSubstance(it.id) }
                        )
                    }
                }
            }
        }
    }
}
