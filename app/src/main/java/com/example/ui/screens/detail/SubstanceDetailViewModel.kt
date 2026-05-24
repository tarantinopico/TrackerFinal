package com.example.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.BioTrackRepository
import com.example.domain.model.Compound
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.domain.model.Variant
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.ui.screens.dashboard.KineticLine

enum class TimePeriod(val days: Int, val label: String) {
    DAYS_7(7, "7D"),
    DAYS_14(14, "14D"),
    MONTH_1(30, "1M"),
    ALL(-1, "ALL")
}

data class TimePoint(val label: String, val value: Float)

data class SubstanceDetailState(
    val substance: Substance? = null,
    val compounds: List<Compound> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val doses: List<Dose> = emptyList(),
    // Analytics
    val totalDoses: Int = 0,
    val avgPerDay: Float = 0f,
    val totalCost: Float = 0f,
    val totalConsumption: Float = 0f,
    val periodDoses: Int = 0,
    val periodCost: Float = 0f,
    val periodConsumption: Float = 0f,
    val dayOfWeekDist: Map<String, Float> = emptyMap(),
    val hourOfDayDist: Map<String, Float> = emptyMap(),
    val variantUsage: Map<String, Float> = emptyMap(),
    val roaUsage: Map<String, Float> = emptyMap(),
    val kineticLines: List<KineticLine> = emptyList(),
    val timePeriod: TimePeriod = TimePeriod.DAYS_14,
    val spendTrend: List<TimePoint> = emptyList(),
    val consumptionTrend: List<TimePoint> = emptyList()
)

class SubstanceDetailViewModel(
    private val repository: BioTrackRepository,
    private val substanceId: String
) : ViewModel() {

    private val _state = MutableStateFlow(SubstanceDetailState())
    val state: StateFlow<SubstanceDetailState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getSubstanceByIdFlow(substanceId),
                repository.getCompoundsForSubstance(substanceId),
                repository.getVariantsForSubstance(substanceId),
                repository.getDosesForSubstance(substanceId)
            ) { substance, compounds, variants, doses ->
                calculateState(substance, compounds, variants, doses, _state.value.timePeriod)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
    
    fun setTimePeriod(period: TimePeriod) {
        _state.update { calculateState(it.substance, it.compounds, it.variants, it.doses, period) }
    }
    
    fun deleteDose(doseId: String) {
        viewModelScope.launch {
            repository.deleteDose(doseId)
        }
    }
    
    private fun calculateState(substance: Substance?, compounds: List<Compound>, variants: List<Variant>, doses: List<Dose>, timePeriod: TimePeriod): SubstanceDetailState {
        val totalDoses = doses.size
        var totalCost = 0f
        var totalConsumption = 0f
        
        val dow = mutableMapOf<String, Float>()
        val hod = mutableMapOf<String, Float>()
        val varUsage = mutableMapOf<String, Float>()
        val roa = mutableMapOf<String, Float>()
        
        val cal = java.util.Calendar.getInstance()
        var earliest = Long.MAX_VALUE
        
        val now = System.currentTimeMillis()
        val periodMs = if (timePeriod == TimePeriod.ALL) Long.MAX_VALUE else timePeriod.days * 24L * 60 * 60 * 1000L
        val thresholdTime = now - periodMs
        
        var periodDosesCount = 0
        var periodCost = 0f
        var periodConsumption = 0f
        
        val spendMap = mutableMapOf<String, Float>()
        val consumeMap = mutableMapOf<String, Float>()
        val formatter = java.text.SimpleDateFormat("MM-dd", java.util.Locale.getDefault())

        if (timePeriod != TimePeriod.ALL) {
            for (i in (timePeriod.days - 1) downTo 0) {
                val d = java.util.Date(now - i * 24L * 60 * 60 * 1000L)
                val str = formatter.format(d)
                spendMap[str] = 0f
                consumeMap[str] = 0f
            }
        }

        doses.forEach { dose ->
            totalCost += dose.price ?: 0f
            totalConsumption += dose.doseAmount
            
            cal.timeInMillis = dose.timestamp
            if (dose.timestamp < earliest) earliest = dose.timestamp
            
            if (dose.timestamp >= thresholdTime) {
                periodDosesCount++
                periodCost += dose.price ?: 0f
                periodConsumption += dose.doseAmount
                
                val dateStr = formatter.format(java.util.Date(dose.timestamp))
                if (timePeriod == TimePeriod.ALL) {
                    spendMap[dateStr] = (spendMap[dateStr] ?: 0f) + (dose.price ?: 0f)
                    consumeMap[dateStr] = (consumeMap[dateStr] ?: 0f) + dose.doseAmount
                } else {
                    if (spendMap.containsKey(dateStr)) {
                        spendMap[dateStr] = spendMap[dateStr]!! + (dose.price ?: 0f)
                        consumeMap[dateStr] = consumeMap[dateStr]!! + dose.doseAmount
                    }
                }
            }
            
            // DOW
            val dayName = cal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SHORT, java.util.Locale.getDefault()) ?: ""
            dow[dayName] = (dow[dayName] ?: 0f) + 1f
            
            // Hour
            val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            val hLabel = "${hour}:00"
            hod[hLabel] = (hod[hLabel] ?: 0f) + 1f
            
            // Variant
            val varName = variants.find { it.id == dose.variantId }?.name ?: "Unknown"
            varUsage[varName] = (varUsage[varName] ?: 0f) + 1f
            
            // ROA
            roa[dose.route] = (roa[dose.route] ?: 0f) + 1f
        }
        
        val daysTotal = if (earliest != Long.MAX_VALUE && earliest < now) {
            ((now - earliest) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
        } else {
            1
        }
        
        // Generate kinetic lines (Last 48 hours for detail view)
        val startTime = now - 48 * 60 * 60 * 1000L
        val stepMs = 15 * 60 * 1000L
        val activeDoses = doses.filter { it.timestamp > startTime - 48 * 60 * 60 * 1000L && it.timestamp <= now }
        
        val line = if (substance != null && activeDoses.isNotEmpty()) {
            val pts = mutableListOf<com.example.ui.screens.dashboard.KineticPoint>()
            val variantMap = variants.associateBy { it.id }
            for (time in startTime..now step stepMs) {
                var valueAtTime = 0.0
                for (dose in activeDoses) {
                    if (dose.timestamp > time || time - dose.timestamp > 48 * 60 * 60 * 1000L) continue 
                    val focus = com.example.domain.model.PharmacokineticEngine.calculateTotalConcentration(dose, substance, compounds, variantMap[dose.variantId], time)
                    valueAtTime += focus
                }
                pts.add(com.example.ui.screens.dashboard.KineticPoint(time, valueAtTime.toFloat()))
            }
            listOf(com.example.ui.screens.dashboard.KineticLine(
                substanceId = substance.id,
                name = substance.name,
                points = pts,
                colorHex = substance.colorHex
            ))
        } else {
            emptyList()
        }
        
        val spendTrend = if (timePeriod == TimePeriod.ALL) {
            spendMap.entries.sortedBy { it.key }.map { TimePoint(it.key, it.value) }
        } else {
            spendMap.map { TimePoint(it.key, it.value) }
        }
        
        val consumptionTrend = if (timePeriod == TimePeriod.ALL) {
            consumeMap.entries.sortedBy { it.key }.map { TimePoint(it.key, it.value) }
        } else {
            consumeMap.map { TimePoint(it.key, it.value) }
        }
        
        return SubstanceDetailState(
            substance = substance,
            compounds = compounds,
            variants = variants,
            doses = doses,
            totalDoses = totalDoses,
            avgPerDay = totalDoses.toFloat() / daysTotal,
            totalCost = totalCost,
            totalConsumption = totalConsumption,
            periodDoses = periodDosesCount,
            periodCost = periodCost,
            periodConsumption = periodConsumption,
            dayOfWeekDist = dow,
            hourOfDayDist = hod,
            variantUsage = varUsage,
            roaUsage = roa,
            kineticLines = line,
            timePeriod = timePeriod,
            spendTrend = spendTrend,
            consumptionTrend = consumptionTrend
        )
    }
}

class SubstanceDetailViewModelFactory(
    private val repository: BioTrackRepository,
    private val substanceId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubstanceDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubstanceDetailViewModel(repository, substanceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
