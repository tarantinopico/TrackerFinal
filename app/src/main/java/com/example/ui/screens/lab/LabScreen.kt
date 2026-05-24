package com.example.ui.screens.lab

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
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
                availableCompounds = state.selectedCompounds,
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
            dismissButton = { TextButton(onClick = { viewModel.cancelDeleteSubstance() }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (state.deleteConfirmCompound != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteCompound() },
            title = { Text("Delete Compound") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.performDeleteCompound() }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDeleteCompound() }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (state.deleteConfirmVariant != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteVariant() },
            title = { Text("Delete Variant") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.performDeleteVariant() }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDeleteVariant() }) { Text("Cancel") } },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = 1200f,
                        center = androidx.compose.ui.geometry.Offset(100f, 100f)
                    )
                )
        )

        Crossfade(targetState = state.viewingSubstanceId != null, label = "LabViewMode") { isDetail ->
            if (isDetail) {
                // Detail Mode
                val substance = state.selectedSubstance
                if (substance != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.viewSubstance(null) }, modifier = Modifier.padding(end = 8.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                            }
                            SubstanceDetailHeader(
                                substance = substance,
                                onEdit = { viewModel.openSubstanceEditor(substance) },
                                onArchiveToggle = { viewModel.archiveSubstance(substance, archive = substance.active) },
                                onDelete = { viewModel.confirmDeleteSubstance(substance) }
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp)) {
                            // Compounds
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Compounds", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    GlassButton(onClick = { viewModel.openNewCompoundEditor() }, text = "Add", icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) })
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
                                                Text(cmp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                                Text("Half-life: ${cmp.halfLifeHours ?: "?"} h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row {
                                                TextButton(onClick = { viewModel.openCompoundEditor(cmp) }) { Text("Edit") }
                                                TextButton(onClick = { viewModel.confirmDeleteCompound(cmp) }) { Text("Del", color = MaterialTheme.colorScheme.error) }
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
                                    Text("Preparations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    GlassButton(onClick = { viewModel.openNewVariantEditor() }, text = "Add", icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) })
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
                                                Text(varnt.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = try { Color(android.graphics.Color.parseColor(varnt.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary })
                                                Text("${varnt.roaDefault} • ${varnt.pricePerUnit ?: "?"} / ${varnt.unitLabel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Row {
                                                TextButton(onClick = { viewModel.openVariantEditor(varnt) }) { Text("Edit") }
                                                TextButton(onClick = { viewModel.confirmDeleteVariant(varnt) }) { Text("Del", color = MaterialTheme.colorScheme.error) }
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
                    modifier = Modifier.fillMaxSize().padding(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Laboratory",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        GlassButton(
                            onClick = { viewModel.openNewSubstanceEditor() },
                            text = "New",
                            icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) }
                        )
                    }
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                        LabFilter.values().forEach { flt ->
                            FilterChip(
                                selected = state.filter == flt,
                                onClick = { viewModel.setFilter(flt) },
                                label = { Text(flt.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true, selected = state.filter == flt,
                                    borderColor = if(state.filter == flt) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                            )
                        }
                    }
                    
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
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

