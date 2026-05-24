package com.example.domain.analytics

import com.example.domain.model.*
import java.util.Calendar

data class GlobalAnalytics(
    val totalLogs: Int,
    val activeSubstancesCount: Int,
    val totalSpend: Float,
    val currencyCode: String,
    val currencySymbol: String,
    val topSubstanceId: String?,
    val topSubstanceName: String?,
    val topCategory: SubstanceCategory?,
    val topVariantId: String?,
    val topVariantName: String?,
    val roaDistribution: Map<String, Float>,
    val activityHeatmap: Map<String, Int> // "YYYY-MM-DD" -> count
)

data class FinanceAnalytics(
    val cumulativeSpend: Float,
    val currencySymbol: String,
    val spendBySubstance: Map<String, Float>,
    val spendByVariant: Map<String, Float>,
    val spendTrend: List<Pair<Long, Float>> // Timestamp to cumulative spend
)

object AnalyticsEngine {
    
    private val currencySymbols = mapOf(
        "USD" to "$",
        "EUR" to "€",
        "GBP" to "£",
        "CZK" to "Kč"
    )

    fun computeGlobalAnalytics(
        doses: List<Dose>,
        substances: List<Substance>,
        variants: List<Variant>,
        settings: AppSettings?
    ): GlobalAnalytics {
        val currencyCode = settings?.currency ?: "USD"
        val currencySymbol = currencySymbols[currencyCode] ?: "$"
        
        val totalSpend = doses.sumOf { (it.price ?: 0f).toDouble() }.toFloat()
        
        val subCountMap = doses.groupingBy { it.substanceId }.eachCount()
        val topSub = subCountMap.maxByOrNull { it.value }?.key
        val topSubName = substances.find { it.id == topSub }?.name
        
        val catCountMap = doses.mapNotNull { d -> substances.find { it.id == d.substanceId }?.category }.groupingBy { it }.eachCount()
        val topCat = catCountMap.maxByOrNull { it.value }?.key
        
        val varCountMap = doses.mapNotNull { it.variantId }.groupingBy { it }.eachCount()
        val topVar = varCountMap.maxByOrNull { it.value }?.key
        val topVarName = variants.find { it.id == topVar }?.name
        
        val roaDist = doses.groupingBy { it.route }.eachCount().mapValues { it.value.toFloat() / doses.size }
        
        val heatmap = mutableMapOf<String, Int>()
        val cal = Calendar.getInstance()
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        doses.forEach { d ->
            cal.timeInMillis = d.timestamp
            val dateStr = format.format(cal.time)
            heatmap[dateStr] = (heatmap[dateStr] ?: 0) + 1
        }
        
        return GlobalAnalytics(
            totalLogs = doses.size,
            activeSubstancesCount = substances.count { it.active },
            totalSpend = totalSpend,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            topSubstanceId = topSub,
            topSubstanceName = topSubName,
            topCategory = topCat,
            topVariantId = topVar,
            topVariantName = topVarName,
            roaDistribution = roaDist,
            activityHeatmap = heatmap
        )
    }
    
    fun computeFinanceAnalytics(
        doses: List<Dose>,
        substances: List<Substance>,
        variants: List<Variant>,
        settings: AppSettings?
    ): FinanceAnalytics {
        val currencyCode = settings?.currency ?: "USD"
        val currencySymbol = currencySymbols[currencyCode] ?: "$"
        var total = 0f
        val spendBySub = mutableMapOf<String, Float>()
        val spendByVar = mutableMapOf<String, Float>()
        
        val sortedDoses = doses.sortedBy { it.timestamp }
        val cumulative = mutableListOf<Pair<Long, Float>>()
        
        for (dose in sortedDoses) {
            val p = dose.price ?: 0f
            total += p
            
            val subName = substances.find { it.id == dose.substanceId }?.name ?: "Unknown"
            spendBySub[subName] = (spendBySub[subName] ?: 0f) + p
            
            val varName = variants.find { it.id == dose.variantId }?.name ?: "Unknown"
            spendByVar[varName] = (spendByVar[varName] ?: 0f) + p
            
            cumulative.add(dose.timestamp to total)
        }
        
        return FinanceAnalytics(
            cumulativeSpend = total,
            currencySymbol = currencySymbol,
            spendBySubstance = spendBySub,
            spendByVariant = spendByVar,
            spendTrend = cumulative
        )
    }
}
