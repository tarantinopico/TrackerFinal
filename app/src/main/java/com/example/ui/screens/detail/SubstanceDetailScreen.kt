package com.example.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
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

                OverviewTab(state, onPeriodChange = { viewModel.setTimePeriod(it) }, onDeleteDose = { viewModel.deleteDose(it) })
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewTab(state: SubstanceDetailState, onPeriodChange: (TimePeriod) -> Unit, onDeleteDose: (String) -> Unit) {
    val substance = state.substance ?: return
    val a = state.analytics
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val periods = TimePeriod.values().toList()
        ScrollableTabRow(
            selectedTabIndex = periods.indexOf(state.timePeriod),
            edgePadding = 0.dp
        ) {
            periods.forEachIndexed { index, period ->
                Tab(
                    selected = state.timePeriod == period,
                    onClick = { onPeriodChange(period) },
                    text = { Text(period.label) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard("Doses", a.periodDoses.toString(), Modifier.weight(1f))
            MetricCard("Cons.", "${String.format("%.1f", a.periodConsumption)} ${state.substance?.defaultUnit ?: ""}", Modifier.weight(1f))
            MetricCard("Cost", String.format("$%.2f", a.periodCost), Modifier.weight(1f))
        }

        SimpleLineChartCard(
            title = "Raw Consumption Trend (${state.substance?.defaultUnit ?: ""})",
            data = a.rawConsumptionTrend,
            color = MaterialTheme.colorScheme.primary
        )

        SimpleLineChartCard(
            title = "Active Consumption Trend (mg)",
            data = a.activeConsumptionTrend,
            color = MaterialTheme.colorScheme.secondary
        )

        SimpleLineChartCard(
            title = "Spend Trend ($)",
            data = a.spendTrend,
            color = MaterialTheme.colorScheme.tertiary
        )

        Text("Recent Doses (Swipe to delete)", style = MaterialTheme.typography.titleLarge)
        val recent = state.doses.sortedByDescending { it.timestamp }.take(15)
        if (recent.isEmpty()) {
            Text("No recent doses", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        } else {
            recent.forEach { dose ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onDeleteDose(dose.id)
                            true
                        } else false
                    }
                )
                
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.error, MaterialTheme.shapes.medium)
                                .padding(horizontal = 20.dp),
                            contentAlignment = androidx.compose.ui.Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onError)
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    val varName = state.variants.find { it.id == dose.variantId }?.name ?: "Unknown"
                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column {
                                Text(varName, style = MaterialTheme.typography.titleSmall)
                                val formatter = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                Text(formatter.format(java.util.Date(dose.timestamp)), style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Lifetime Metrics", style = MaterialTheme.typography.titleLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard("Total Doses", a.totalDoses.toString(), Modifier.weight(1f))
            MetricCard("Avg / Day", String.format("%.1f", a.avgPerDay), Modifier.weight(1f))
            MetricCard("Total Cost", String.format("$%.2f", a.totalCost), Modifier.weight(1f))
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
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
fun AnalyticsTab(state: SubstanceDetailState, onPeriodChange: (TimePeriod) -> Unit) {
    val a = state.analytics
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val periods = TimePeriod.values().toList()
        ScrollableTabRow(
            selectedTabIndex = periods.indexOf(state.timePeriod),
            edgePadding = 0.dp
        ) {
            periods.forEachIndexed { index, period ->
                Tab(
                    selected = state.timePeriod == period,
                    onClick = { onPeriodChange(period) },
                    text = { Text(period.label) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard("Period Doses", a.periodDoses.toString(), Modifier.weight(1f))
            MetricCard("Period Cons.", "${String.format("%.1f", a.periodConsumption)} ${state.substance?.defaultUnit ?: ""}", Modifier.weight(1f))
            MetricCard("Period Cost", String.format("$%.2f", a.periodCost), Modifier.weight(1f))
        }

        if (a.activeCompoundLines.isNotEmpty()) {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Compound Load (48h)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                        com.example.ui.screens.dashboard.components.KineticGraph(
                            lines = a.activeCompoundLines,
                            mode = com.example.ui.screens.dashboard.GraphMode.CONCENTRATION,
                            onModeToggle = {}
                        )
                    }
                }
            }
        }
        
        SimpleLineChartCard(
            title = "Tolerance Trend (3-Day SMA)",
            data = a.toleranceTrend,
            color = MaterialTheme.colorScheme.error
        )
        
        SimpleBarChart(
            title = "Dose Size Distribution",
            data = a.doseHistogram,
            colorList = listOf(MaterialTheme.colorScheme.tertiary)
        )
        
        SimpleBarChart(
            title = "Variant Usage",
            data = a.variantUsage,
            colorList = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
        )
        
        SimpleBarChart(
            title = "Route of Administration",
            data = a.roaUsage,
            colorList = listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.primaryContainer)
        )
        
        SimpleBarChart(
            title = "Compound Contribution (${state.substance?.defaultUnit ?: ""})",
            data = a.compoundContribution,
            colorList = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary)
        )
        
        SimpleBarChart(
            title = "Day of Week Distribution",
            data = a.dayOfWeekDist
        )
        
        SimpleBarChart(
            title = "Circadian Rhythm (Hour of Day)",
            data = a.hourOfDayDist,
            colorList = listOf(MaterialTheme.colorScheme.secondary)
        )
    }
}

@Composable
fun SimpleLineChartCard(title: String, data: List<TimePoint>, color: androidx.compose.ui.graphics.Color) {
    if (data.isEmpty()) return
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.height(150.dp).fillMaxWidth()) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxVal = data.maxOfOrNull { it.value } ?: 1f
                    val yLabelSteps = 4
                    val labelPaint = android.graphics.Paint().apply {
                        this.color = android.graphics.Color.GRAY
                        textSize = 10.dp.toPx()
                    }
                    
                    // Draw Y axis labels
                    for (i in 0..yLabelSteps) {
                        val v = maxVal * (i / yLabelSteps.toFloat())
                        val yPos = size.height - (size.height * (i / yLabelSteps.toFloat()))
                        val drawY = if (i == yLabelSteps) yPos + 10.dp.toPx() else if (i == 0) yPos - 2.dp.toPx() else yPos
                        drawContext.canvas.nativeCanvas.drawText(String.format("%.1f", v), 0f, drawY, labelPaint)
                    }

                    if (data.size > 1) {
                        val path = androidx.compose.ui.graphics.Path()
                        val stepX = size.width / (data.size - 1)
                        data.forEachIndexed { index, point ->
                            val drawMax = if (maxVal > 0) maxVal else 1f
                            val x = index * stepX
                            val y = size.height - (point.value / drawMax * size.height)
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                            
                            // Draw X axis labels (skip some to avoid crowding if too many)
                            val drawLabel = if (data.size > 15) index % (data.size / 5) == 0 else true
                            if (drawLabel || index == data.size - 1) {
                                val txPos = if (index == data.size - 1) x - 20.dp.toPx() else x
                                drawContext.canvas.nativeCanvas.drawText(point.label, txPos, size.height + 15.dp.toPx(), labelPaint)
                            }
                        }
                        drawPath(path, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                    } else if (data.size == 1) {
                        drawContext.canvas.nativeCanvas.drawText(data.first().label, size.width / 2, size.height + 15.dp.toPx(), labelPaint)
                    }
                }
            }
        }
    }
}
