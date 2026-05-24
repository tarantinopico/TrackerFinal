package com.example.ui.screens.logger

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.screens.logger.components.*
import com.example.domain.model.*

@Composable
fun ActiveCompoundsPreview(
    amount: Float,
    unit: String,
    compounds: List<Compound>,
    variant: Variant?
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Active Compounds Extraction", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val isMacroUnit = unit.equals("g", ignoreCase = true) || unit.equals("kg", ignoreCase = true)
            val doseMg = if (isMacroUnit) UnitConverter.toMg(amount.toDouble(), unit) else amount.toDouble()

            compounds.forEach { cmp ->
                val ratio = variant?.ratio?.get(cmp.id) ?: (1.0 / compounds.size)
                val activeMg = doseMg * ratio
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${String.format("%.2f", ratio * 100)}% ${cmp.name}", 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${String.format("%.2f", activeMg)} mg", 
                        style = MaterialTheme.typography.bodyMedium,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
                .verticalScroll(scrollState),
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
                    text = "Log Entry",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                GlassButton(
                    onClick = { viewModel.toggleQuickDoseSheet(true) },
                    text = "Quick",
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp)) }
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Substance
                Column {
                    Text("Target", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Preparation", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Dose Amount", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    AmountStepper(
                        amount = state.amount,
                        unit = state.unit,
                        onAmountChange = { viewModel.updateAmount(it) }
                    )
                    if (state.amount > 0 && state.compounds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
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
                    Text("Route of Administration", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    RouteSelector(
                        selectedRoute = state.route,
                        onRouteSelected = { viewModel.updateRoute(it) }
                    )
                }
                
                // Time
                Column {
                    Text("Administration Time", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.updateTimestamp(state.timestamp - 15 * 60 * 1000L) }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape)) {
                                Icon(Icons.Default.Remove, contentDescription = "-15m", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            
                            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            Text(
                                text = formatter.format(java.util.Date(state.timestamp)),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            IconButton(onClick = { viewModel.updateTimestamp(state.timestamp + 15 * 60 * 1000L) }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), androidx.compose.foundation.shape.CircleShape)) {
                                Icon(Icons.Default.Add, contentDescription = "+15m", tint = MaterialTheme.colorScheme.onSurface)
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
                        placeholder = { Text("Calculated automatically") },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                
                // Notes
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text("Notes & Context") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.saveLog(onSaveSuccess) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    enabled = state.selectedSubstance != null && state.amount > 0f,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("Commit Record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

