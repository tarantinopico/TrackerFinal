package com.example.ui.screens.logger

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.SectionHeader
import com.example.ui.screens.logger.components.*
import com.example.domain.model.*

@Composable
fun ActiveCompoundsPreview(
    amount: Float,
    unit: String,
    compounds: List<Compound>,
    variant: Variant?
) {
    com.example.ui.components.GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "Active Compounds Extraction", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val isMacroUnit = unit.equals("g", ignoreCase = true) || unit.equals("kg", ignoreCase = true)
            val doseMg = if (isMacroUnit) UnitConverter.toMg(amount.toDouble(), unit) else amount.toDouble()

            compounds.forEach { cmp ->
                val ratio = variant?.ratio?.get(cmp.id) ?: (1.0 / compounds.size)
                val activeMg = doseMg * ratio
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${String.format("%.2f", ratio * 100)}% ${cmp.name}", 
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "${String.format("%.2f", activeMg)} mg", 
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerScreen(
    viewModel: LoggerViewModel,
    hideFinanceMode: Boolean,
    onSaveSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    
    if (state.showQuickDoseSheet) {
        QuickDoseSheet(
            quickDoses = state.quickDoses,
            substances = state.substances,
            variants = state.variants,
            onDismissRequest = { viewModel.toggleQuickDoseSheet(false) },
            onQuickDoseSelected = { viewModel.applyQuickDose(it, onSuccess = onSaveSuccess) },
            onSaveQuickDose = { viewModel.saveQuickDose(it) },
            onDeleteQuickDose = { viewModel.deleteQuickDose(it) },
            sheetState = sheetState
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Entry") },
                actions = {
                    IconButton(onClick = { viewModel.toggleQuickDoseSheet(true) }) {
                        Icon(Icons.Default.Bolt, contentDescription = "Quick Doses", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Substance
            Column {
                Text("Substance", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                SubstancePicker(
                    substances = state.substances,
                    selectedSubstance = state.selectedSubstance,
                    onSubstanceSelected = { viewModel.selectSubstance(it) }
                )
            }
            
            // Variant (if present)
            if (state.variants.isNotEmpty()) {
                Column {
                    Text("Variant", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    VariantPicker(
                        variants = state.variants,
                        selectedVariant = state.selectedVariant,
                        onVariantSelected = { viewModel.selectVariant(it) }
                    )
                }
            }
            
            // Amount
            Column {
                Text("Amount", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                AmountStepper(
                    amount = state.amount,
                    unit = state.unit,
                    onAmountChange = { viewModel.updateAmount(it) }
                )
                if (state.amount > 0 && state.compounds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ActiveCompoundsPreview(
                        amount = state.amount,
                        unit = state.unit,
                        compounds = state.compounds,
                        variant = state.selectedVariant
                    )
                }
            }
            
            // Route
            Column {
                Text("Route of Administration", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                RouteSelector(
                    selectedRoute = state.route,
                    onRouteSelected = { viewModel.updateRoute(it) }
                )
            }
            
            // Time
            Column {
                Text("Time", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                com.example.ui.components.GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.updateTimestamp(state.timestamp - 15 * 60 * 1000L) }) {
                            Icon(Icons.Default.Remove, contentDescription = "-15m")
                        }
                        
                        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        Text(
                            text = formatter.format(java.util.Date(state.timestamp)),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = { viewModel.updateTimestamp(state.timestamp + 15 * 60 * 1000L) }) {
                            Icon(Icons.Default.Add, contentDescription = "+15m")
                        }
                    }
                }
            }
            
            // Warnings & Estimates
            LoggerWarnings(
                warningMessage = state.warningMessage,
                estimatedOnset = state.estimatedOnset,
                estimatedPeak = state.estimatedPeak,
                estimatedDuration = state.estimatedDuration
            )
            
            // Price (Optional depending on settings)
            if (!hideFinanceMode) {
                OutlinedTextField(
                    value = state.computedPrice.takeIf { it > 0f }?.toString() ?: "",
                    onValueChange = { viewModel.updatePrice(it.toFloatOrNull()) },
                    label = { Text("Price (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Calculated automatically") }
                )
            }
            
            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes & Context") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { viewModel.saveLog(onSaveSuccess) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = state.selectedSubstance != null && state.amount > 0f
            ) {
                Text("Save Entry", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
            
            Spacer(modifier = Modifier.height(40.dp)) // Nav bar padding area
        }
    }
}
