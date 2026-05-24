package com.example.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.Substance
import com.example.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstanceDetailScreen(
    viewModel: SubstanceDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val substance = state.substance

    var selectedTabIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    val tabs = listOf("Overview", "Composition", "Analytics")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(substance?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (substance != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> OverviewTab(state)
                    1 -> CompositionTab(state)
                    2 -> AnalyticsTab(state)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun OverviewTab(state: SubstanceDetailState) {
    val substance = state.substance ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Text(substance.category.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Notes", style = MaterialTheme.typography.labelMedium)
                Text(substance.notes.ifEmpty { "No description available." }, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Default Unit", style = MaterialTheme.typography.labelMedium)
                Text(substance.defaultUnit, style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard("Doses", state.totalDoses.toString(), Modifier.weight(1f))
            MetricCard("Avg / Day", String.format("%.1f", state.avgPerDay), Modifier.weight(1f))
            MetricCard("Total Cost", String.format("$%.2f", state.totalCost), Modifier.weight(1f))
        }
        
        Text("Recent Doses", style = MaterialTheme.typography.titleLarge)
        val recent = state.doses.sortedByDescending { it.timestamp }.take(10)
        if (recent.isEmpty()) {
            Text("No recent doses", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        } else {
            recent.forEach { dose ->
                val varName = state.variants.find { it.id == dose.variantId }?.name ?: "Unknown"
                GlassCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(varName, style = MaterialTheme.typography.titleSmall)
                            val formatter = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                            Text(formatter.format(java.util.Date(dose.timestamp)), style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text("${dose.doseAmount} ${dose.unit}", fontWeight = FontWeight.Bold)
                            Text(dose.route, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun CompositionTab(state: SubstanceDetailState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Compounds", style = MaterialTheme.typography.titleLarge)
        if (state.compounds.isEmpty()) {
            Text("No compounds defined.")
        } else {
            state.compounds.forEach { cmp ->
                GlassCard {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(cmp.name, fontWeight = FontWeight.SemiBold)
                        Text("Potency: ${cmp.potencyMultiplier}x")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Variants & Ratios", style = MaterialTheme.typography.titleLarge)
        if (state.variants.isEmpty()) {
            Text("No variants defined.")
        } else {
            state.variants.forEach { vrt ->
                GlassCard {
                    Column {
                        Text(vrt.name, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(vrt.colorHex)))
                        Text("${vrt.pricePerUnit ?: 0} / ${vrt.unitLabel}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (vrt.ratio.isEmpty()) {
                            Text("Standard composition.")
                        } else {
                            vrt.ratio.forEach { (cId, pct) ->
                                val cName = state.compounds.find { it.id == cId }?.name ?: "Unknown"
                                Text("- $cName: ${String.format("%.3f", pct * 100)}%")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsTab(state: SubstanceDetailState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.kineticLines.isNotEmpty()) {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pharmacokinetics (48h)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                        com.example.ui.screens.dashboard.components.KineticGraph(
                            lines = state.kineticLines,
                            mode = com.example.ui.screens.dashboard.GraphMode.CONCENTRATION,
                            onModeToggle = {}
                        )
                    }
                }
            }
        }
        
        SimpleBarChart(
            title = "Variant Usage",
            data = state.variantUsage,
            colorList = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
        )
        
        SimpleBarChart(
            title = "Route of Administration",
            data = state.roaUsage,
            colorList = listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.primaryContainer)
        )
        
        SimpleBarChart(
            title = "Day of Week Distribution",
            data = state.dayOfWeekDist
        )
        
        SimpleBarChart(
            title = "Circadian Rhythm (Hour of Day)",
            data = state.hourOfDayDist,
            colorList = listOf(MaterialTheme.colorScheme.secondary)
        )
    }
}
