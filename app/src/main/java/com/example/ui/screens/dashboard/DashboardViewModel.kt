package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class GraphMode {
    INFLUENCE, CONCENTRATION
}

data class KineticPoint(val timeMs: Long, val value: Float)

data class KineticLine(
    val substanceId: String,
    val name: String,
    val colorHex: String,
    val points: List<KineticPoint>
)

data class DashboardState(
    val todayDoseCount: Int = 0,
    val todaySpend: Float = 0f,
    val activeSubstancesCount: Int = 0,
    val spendTrend7d: Float = 0f,
    val recentLogs: List<Pair<Dose, Substance>> = emptyList(),
    val kineticLines: List<KineticLine> = emptyList(),
    val aggregatedPoints: List<KineticPoint> = emptyList(),
    val graphMode: GraphMode = GraphMode.INFLUENCE,
    val isWarningHighLoad: Boolean = false
)

class DashboardViewModel(
    private val repository: BioTrackRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    private val graphModeFlow = MutableStateFlow(GraphMode.INFLUENCE)

    init {
        viewModelScope.launch {
            combine(
                repository.getAllDoses(),
                repository.getAllSubstances(),
                repository.getAllCompounds(),
                repository.getAllVariants(),
                graphModeFlow
            ) { doses, substances, compounds, variants, mode ->
                calculateState(doses, substances, compounds, variants, mode)
            }.flowOn(Dispatchers.Default)
             .collect { newState -> _state.value = newState }
        }
    }

    fun toggleGraphMode() {
        graphModeFlow.value = if (graphModeFlow.value == GraphMode.INFLUENCE) GraphMode.CONCENTRATION else GraphMode.INFLUENCE
    }

    private fun calculateState(
        doses: List<Dose>, 
        substances: List<Substance>, 
        compounds: List<Compound>,
        variants: List<Variant>,
        mode: GraphMode
    ): DashboardState {
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDay(now)
        val startOf7d = startOfDay - (7 * 24 * 60 * 60 * 1000L)
        
        val todayDoses = doses.filter { it.timestamp >= startOfDay }
        val spendToday = todayDoses.mapNotNull { it.price }.sum()
        val activeCount = todayDoses.map { it.substanceId }.toSet().size
        
        val spend7d = doses.filter { it.timestamp in startOf7d until startOfDay }.mapNotNull { it.price }.sum()
        val trend = if (spend7d > 0) ((spendToday - (spend7d/7)) / (spend7d/7)) * 100 else 0f
        
        val twelveHours = 12 * 60 * 60 * 1000L
        val startTime = now - twelveHours
        val endTime = now + twelveHours
        val stepMs = 15 * 60 * 1000L
        
        val substanceMap = substances.associateBy { it.id }
        val variantMap = variants.associateBy { it.id }
        val compoundMap = compounds.groupBy { it.substanceId }
        
        val activeDoses = doses.filter { it.timestamp > startTime - 48 * 60 * 60 * 1000L && it.timestamp <= endTime }
        val lines = mutableListOf<KineticLine>()
        val aggPoints = mutableListOf<KineticPoint>()
        
        val activeSubDoses = activeDoses.groupBy { it.substanceId }
        
        for ((subId, subDoses) in activeSubDoses) {
            val sub = substanceMap[subId] ?: continue
            val subCompounds = compoundMap[subId] ?: emptyList()
            
            val pts = mutableListOf<KineticPoint>()
            for (time in startTime..endTime step stepMs) {
                var valueAtTime = 0f
                for (dose in subDoses) {
                    if (dose.timestamp > time || time - dose.timestamp > 48 * 60 * 60 * 1000L) continue 
                    val focus = calculateDoseConcentration(dose, sub, subCompounds, variantMap[dose.variantId], time)
                    valueAtTime += if (mode == GraphMode.INFLUENCE) focus * getInfluenceFactor(sub) else focus
                }
                pts.add(KineticPoint(time, valueAtTime))
            }
            if (pts.any { it.value > 0.01f }) {
                lines.add(KineticLine(
                    substanceId = subId,
                    name = sub.name,
                    colorHex = sub.colorHex,
                    points = pts
                ))
            }
        }
        
        var isWarn = false
        for (time in startTime..endTime step stepMs) {
            var sum = 0f
            for (line in lines) {
                val pt = line.points.find { it.timeMs == time }
                if (pt != null) sum += pt.value
            }
            aggPoints.add(KineticPoint(time, sum))
            if (time == now && sum > 80f) isWarn = true
        }
        
        val recentLogs = doses.sortedByDescending { it.timestamp }.take(5).mapNotNull { dose ->
            substanceMap[dose.substanceId]?.let { sub -> Pair(dose, sub) }
        }
        
        return DashboardState(
            todayDoseCount = todayDoses.size,
            todaySpend = spendToday,
            activeSubstancesCount = activeCount,
            spendTrend7d = trend,
            recentLogs = recentLogs,
            kineticLines = lines,
            aggregatedPoints = aggPoints,
            graphMode = mode,
            isWarningHighLoad = isWarn
        )
    }
    
    private fun getStartOfDay(now: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    
    private fun getInfluenceFactor(sub: Substance?): Float {
        if (sub == null) return 1f
        return when (sub.category) {
            SubstanceCategory.STIMULANT -> 0.5f 
            SubstanceCategory.DEPRESSANT -> 1f   
            SubstanceCategory.PSYCHEDELIC -> 5f  
            SubstanceCategory.SUPPLEMENT -> 0.05f 
            else -> 0.2f
        }
    }
    
    private fun calculateDoseConcentration(dose: Dose, sub: Substance?, compounds: List<Compound>, variant: Variant?, time: Long): Float {
        return com.example.domain.model.PharmacokineticEngine.calculateTotalConcentration(dose, sub, compounds, variant, time).toFloat()
    }
}

class DashboardViewModelFactory(
    private val repository: BioTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
