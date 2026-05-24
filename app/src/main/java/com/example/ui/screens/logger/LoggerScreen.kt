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
            onDismissRequest = { viewModel.toggleQuickDoseSheet(false) },
            onQuickDoseSelected = { viewModel.applyQuickDose(it) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Substance
            Column {
                Text("Select Substance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                SubstancePicker(
                    substances = state.substances,
                    selectedSubstance = state.selectedSubstance,
                    onSubstanceSelected = { viewModel.selectSubstance(it) }
                )
            }
            
            // Variant (if present)
            if (state.variants.isNotEmpty()) {
                Column {
                    Text("Select Variant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    VariantPicker(
                        variants = state.variants,
                        selectedVariant = state.selectedVariant,
                        onVariantSelected = { viewModel.selectVariant(it) }
                    )
                }
            }
            
            // Amount
            Column {
                Text("Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                AmountStepper(
                    amount = state.amount,
                    unit = state.unit,
                    onAmountChange = { viewModel.updateAmount(it) }
                )
            }
            
            // Route
            Column {
                Text("Route of Administration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                RouteSelector(
                    selectedRoute = state.route,
                    onRouteSelected = { viewModel.updateRoute(it) }
                )
            }
            
            // Time
            Column {
                Text("Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                com.example.ui.components.GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    placeholder = { Text("Calculated automatically if variant has price") }
                )
            }
            
            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes & Context") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
