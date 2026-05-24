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

data class DashboardState(
    val systemLoad: Float = 0f,
    val todayDoseCount: Int = 0,
    val todaySpend: Float = 0f,
    val activeSubstancesCount: Int = 0,
    val spendTrend7d: Float = 0f,
    val recentLogs: List<Pair<Dose, Substance>> = emptyList(),
    val graphPoints: List<KineticPoint> = emptyList(),
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
                repository.getActiveSubstances(),
                graphModeFlow
            ) { doses, substances, mode ->
                calculateState(doses, substances, mode)
            }.flowOn(Dispatchers.Default)
             .collect { newState -> _state.value = newState }
        }
    }

    fun toggleGraphMode() {
        graphModeFlow.value = if (graphModeFlow.value == GraphMode.INFLUENCE) GraphMode.CONCENTRATION else GraphMode.INFLUENCE
    }

    private fun calculateState(doses: List<Dose>, substances: List<Substance>, mode: GraphMode): DashboardState {
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
        val stepMs = 30 * 60 * 1000L
        
        val substanceMap = substances.associateBy { it.id }
        val points = mutableListOf<KineticPoint>()
        
        for (time in startTime..endTime step stepMs) {
            var valueAtTime = 0f
            for (dose in doses) {
                if (dose.timestamp > time || time - dose.timestamp > 48 * 60 * 60 * 1000L) continue 
                val sub = substanceMap[dose.substanceId]
                val focus = calculateConcentration(dose, sub, time)
                valueAtTime += if (mode == GraphMode.INFLUENCE) focus * getInfluenceFactor(sub) else focus
            }
            points.add(KineticPoint(time, valueAtTime))
        }
        
        var exactLoad = 0f
        for (dose in doses) {
            if (dose.timestamp > now || now - dose.timestamp > 48 * 60 * 60 * 1000L) continue
            val sub = substanceMap[dose.substanceId]
            val conc = calculateConcentration(dose, sub, now)
            exactLoad += if (mode == GraphMode.INFLUENCE) conc * getInfluenceFactor(sub) else conc
        }
        
        val visualLoad = exactLoad.coerceIn(0f, 100f)
        
        val recentLogs = doses.sortedByDescending { it.timestamp }.take(5).mapNotNull { dose ->
            substanceMap[dose.substanceId]?.let { sub -> Pair(dose, sub) }
        }
        
        return DashboardState(
            systemLoad = visualLoad,
            todayDoseCount = todayDoses.size,
            todaySpend = spendToday,
            activeSubstancesCount = activeCount,
            spendTrend7d = trend,
            recentLogs = recentLogs,
            graphPoints = points,
            graphMode = mode,
            isWarningHighLoad = visualLoad > 80f
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
    
    private fun calculateConcentration(dose: Dose, sub: Substance?, time: Long): Float {
        val dtHours = (time - dose.timestamp) / 3600000.0
        if (dtHours < 0) return 0f
        
        val halfLife = when (sub?.category) {
            SubstanceCategory.STIMULANT -> 5.0
            SubstanceCategory.DEPRESSANT -> 4.0
            SubstanceCategory.PSYCHEDELIC -> 12.0
            SubstanceCategory.SUPPLEMENT -> 24.0
            else -> 6.0
        }
        
        val peak = 0.5 
        return if (dtHours < peak) {
            (dose.doseAmount * (dtHours / peak)).toFloat()
        } else {
            val postPeak = dtHours - peak
            val decay = Math.pow(0.5, postPeak / halfLife)
            (dose.doseAmount * decay).toFloat()
        }
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
