package com.example.domain.analytics

import com.example.domain.model.Compound
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.domain.model.Variant
import com.example.domain.model.PharmacokineticEngine
import com.example.ui.screens.dashboard.KineticLine
import com.example.ui.screens.dashboard.KineticPoint
import com.example.ui.screens.detail.TimePeriod
import com.example.ui.screens.detail.TimePoint
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class SubstanceAnalyticsEngine {

    data class AnalyticsResult(
        val totalDoses: Int,
        val periodDoses: Int,
        val avgPerDay: Float,
        val totalCost: Float,
        val totalConsumption: Float,
        val periodCost: Float,
        val periodConsumption: Float,
        
        // Distribution
        val dayOfWeekDist: Map<String, Float>,
        val hourOfDayDist: Map<String, Float>,
        val variantUsage: Map<String, Float>,
        val roaUsage: Map<String, Float>,
        val spendByVariant: Map<String, Float>,
        val spendByRoa: Map<String, Float>,
        
        // Components
        val compoundContribution: Map<String, Float>,
        
        // Trends
        val spendTrend: List<TimePoint>,
        val rawConsumptionTrend: List<TimePoint>,
        val activeConsumptionTrend: List<TimePoint>,
        val activeCompoundLines: List<KineticLine>,
        val doseHistogram: Map<String, Float>,
        val toleranceTrend: List<TimePoint>
    )

    fun calculate(
        substance: Substance?,
        compounds: List<Compound>,
        variants: List<Variant>,
        doses: List<Dose>,
        timePeriod: TimePeriod
    ): AnalyticsResult {
        if (substance == null || doses.isEmpty()) {
            return emptyResult()
        }

        val allDoses = doses.sortedBy { it.timestamp }
        val now = System.currentTimeMillis()
        val periodMs = if (timePeriod == TimePeriod.ALL) Long.MAX_VALUE else timePeriod.days * 24L * 60 * 60 * 1000L
        val thresholdTime = now - periodMs

        var totalCost = 0f
        var totalConsumption = 0f
        var periodCost = 0f
        var periodConsumption = 0f
        var periodDosesCount = 0

        val dow = mutableMapOf<String, Float>()
        val hod = mutableMapOf<String, Float>()
        val varUsage = mutableMapOf<String, Float>()
        val roa = mutableMapOf<String, Float>()
        val spendByVariant = mutableMapOf<String, Float>()
        val spendByRoa = mutableMapOf<String, Float>()
        val cmpContribution = mutableMapOf<String, Float>()
        
        val spendMap = mutableMapOf<String, Float>()
        val rawConsumeMap = mutableMapOf<String, Float>()
        val activeConsumeMap = mutableMapOf<String, Float>()
        val doseSizes = mutableListOf<Float>()

        val formatter = SimpleDateFormat("MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        var earliest = Long.MAX_VALUE

        // Pre-fill trend maps
        if (timePeriod != TimePeriod.ALL) {
            for (i in (timePeriod.days - 1) downTo 0) {
                val d = Date(now - i * 24L * 60 * 60 * 1000L)
                val str = formatter.format(d)
                spendMap[str] = 0f
                rawConsumeMap[str] = 0f
                activeConsumeMap[str] = 0f
            }
        }

        allDoses.forEach { dose ->
            totalCost += dose.price ?: 0f
            totalConsumption += dose.doseAmount
            
            cal.timeInMillis = dose.timestamp
            if (dose.timestamp < earliest) earliest = dose.timestamp

            val variant = variants.find { it.id == dose.variantId }
            val doseInMg = if (dose.unit.equals("g", ignoreCase = true)) dose.doseAmount * 1000f else dose.doseAmount
            
            var activeDoseMg = 0f
            if (variant != null && variant.ratio.isNotEmpty()) {
                variant.ratio.forEach { (_, pct) -> 
                    activeDoseMg += doseInMg * pct.toFloat()
                }
            } else {
                activeDoseMg = doseInMg
            }

            if (dose.timestamp >= thresholdTime) {
                periodDosesCount++
                periodCost += dose.price ?: 0f
                periodConsumption += dose.doseAmount
                doseSizes.add(doseInMg)

                val dateStr = formatter.format(Date(dose.timestamp))
                spendMap[dateStr] = (spendMap[dateStr] ?: 0f) + (dose.price ?: 0f)
                rawConsumeMap[dateStr] = (rawConsumeMap[dateStr] ?: 0f) + doseInMg
                activeConsumeMap[dateStr] = (activeConsumeMap[dateStr] ?: 0f) + activeDoseMg

                // Heatmaps
                val dayName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""
                dow[dayName] = (dow[dayName] ?: 0f) + 1f
                
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val hLabel = "${hour}:00"
                hod[hLabel] = (hod[hLabel] ?: 0f) + 1f

                // Breakdown
                val varName = variant?.name ?: "Unknown"
                varUsage[varName] = (varUsage[varName] ?: 0f) + 1f
                spendByVariant[varName] = (spendByVariant[varName] ?: 0f) + (dose.price ?: 0f)
                
                roa[dose.route] = (roa[dose.route] ?: 0f) + 1f
                spendByRoa[dose.route] = (spendByRoa[dose.route] ?: 0f) + (dose.price ?: 0f)

                // Compounds
                if (variant != null && variant.ratio.isNotEmpty()) {
                    variant.ratio.forEach { (cmpId, pct) ->
                        val cmpName = compounds.find { it.id == cmpId }?.name ?: "Unknown"
                        cmpContribution[cmpName] = (cmpContribution[cmpName] ?: 0f) + (doseInMg * pct.toFloat())
                    }
                } else {
                    val fallbackName = compounds.firstOrNull()?.name ?: substance.name
                    cmpContribution[fallbackName] = (cmpContribution[fallbackName] ?: 0f) + doseInMg
                }
            }
        }

        val daysTotal = if (earliest != Long.MAX_VALUE && earliest < now) {
            ((now - earliest) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
        } else {
            1
        }

        // Kinetic Lines
        val startTime = now - 48 * 60 * 60 * 1000L
        val stepMs = 15 * 60 * 1000L
        val activeDoses = doses.filter { it.timestamp > startTime - 48 * 60 * 60 * 1000L && it.timestamp <= now }
        
        val compoundPoints = mutableMapOf<String, MutableList<KineticPoint>>()
        val variantMap = variants.associateBy { it.id }

        if (activeDoses.isNotEmpty()) {
            for (time in startTime..now step stepMs) {
                // Aggregate load per compound at this `time`
                val loadsAtTime = mutableMapOf<String, Double>()
                
                for (dose in activeDoses) {
                    if (dose.timestamp > time || time - dose.timestamp > 48 * 60 * 60 * 1000L) continue 
                    val currentLoads = PharmacokineticEngine.calculateCurrentLoad(dose, substance, compounds, variantMap[dose.variantId], time)
                    for (load in currentLoads) {
                        loadsAtTime[load.compoundId] = (loadsAtTime[load.compoundId] ?: 0.0) + load.concentration
                    }
                }

                if (compounds.isEmpty() && loadsAtTime.isNotEmpty()) {
                    val fallbackId = "default"
                    compoundPoints.getOrPut(fallbackId) { mutableListOf() }.add(KineticPoint(time, (loadsAtTime[fallbackId] ?: 0.0).toFloat()))
                } else {
                    for (cmp in compounds) {
                        val v = loadsAtTime[cmp.id] ?: 0.0
                        compoundPoints.getOrPut(cmp.id) { mutableListOf() }.add(KineticPoint(time, v.toFloat()))
                    }
                }
            }
        }

        val kineticLines = mutableListOf<KineticLine>()
        if (compounds.isEmpty()) {
            compoundPoints["default"]?.let { pts ->
                kineticLines.add(KineticLine(substance.id, substance.name, substance.colorHex, pts))
            }
        } else {
            for (cmp in compounds) {
                compoundPoints[cmp.id]?.let { pts ->
                    kineticLines.add(KineticLine(cmp.id, cmp.name, cmp.colorHex, pts))
                }
            }
        }

        // Dose Histogram
        val histogram = mutableMapOf<String, Float>()
        if (doseSizes.isNotEmpty()) {
            val avgDose = doseSizes.average().toFloat()
            doseSizes.forEach { sz ->
                val bucket = when {
                    sz < avgDose * 0.5f -> "Micro"
                    sz < avgDose * 1.5f -> "Normal"
                    sz < avgDose * 3.0f -> "Heavy"
                    else -> "Extreme"
                }
                histogram[bucket] = (histogram[bucket] ?: 0f) + 1f
            }
        }
        
        // Sorting trends
        fun sortTrend(map: Map<String, Float>): List<TimePoint> {
            return if (timePeriod == TimePeriod.ALL) {
                map.entries.sortedBy { it.key }.map { TimePoint(it.key, it.value) }
            } else {
                map.map { TimePoint(it.key, it.value) }
            }
        }

        // Tolerance Trend (Rolling SMA 3)
        val rawConsumeTrend = sortTrend(rawConsumeMap)
        val toleranceMap = mutableMapOf<String, Float>()
        for (i in rawConsumeTrend.indices) {
            var sum = 0f
            var count = 0
            for (j in maxOf(0, i - 2)..i) {
                sum += rawConsumeTrend[j].value
                count++
            }
            toleranceMap[rawConsumeTrend[i].label] = sum / count
        }

        return AnalyticsResult(
            totalDoses = doses.size,
            periodDoses = periodDosesCount,
            avgPerDay = doses.size.toFloat() / daysTotal,
            totalCost = totalCost,
            totalConsumption = totalConsumption,
            periodCost = periodCost,
            periodConsumption = periodConsumption,
            dayOfWeekDist = dow,
            hourOfDayDist = hod,
            variantUsage = varUsage,
            roaUsage = roa,
            spendByVariant = spendByVariant,
            spendByRoa = spendByRoa,
            compoundContribution = cmpContribution,
            spendTrend = sortTrend(spendMap),
            rawConsumptionTrend = rawConsumeTrend,
            activeConsumptionTrend = sortTrend(activeConsumeMap),
            activeCompoundLines = kineticLines,
            doseHistogram = histogram,
            toleranceTrend = sortTrend(toleranceMap)
        )
    }

    private fun emptyResult() = AnalyticsResult(
        0, 0, 0f, 0f, 0f, 0f, 0f, 
        emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(),
        emptyList(), emptyList(), emptyList(), emptyList(), emptyMap(), emptyList()
    )
}
