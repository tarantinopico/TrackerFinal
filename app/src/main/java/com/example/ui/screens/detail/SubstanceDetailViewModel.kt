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

data class SubstanceDetailState(
    val substance: Substance? = null,
    val compounds: List<Compound> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val doses: List<Dose> = emptyList(),
    // Analytics
    val totalDoses: Int = 0,
    val avgPerDay: Float = 0f,
    val totalCost: Float = 0f,
    val dayOfWeekDist: Map<String, Float> = emptyMap(),
    val hourOfDayDist: Map<String, Float> = emptyMap(),
    val variantUsage: Map<String, Float> = emptyMap(),
    val roaUsage: Map<String, Float> = emptyMap(),
    val kineticLines: List<KineticLine> = emptyList()
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
                calculateState(substance, compounds, variants, doses)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
    
    private fun calculateState(substance: Substance?, compounds: List<Compound>, variants: List<Variant>, doses: List<Dose>): SubstanceDetailState {
        val totalDoses = doses.size
        var totalCost = 0f
        
        val dow = mutableMapOf<String, Float>()
        val hod = mutableMapOf<String, Float>()
        val varUsage = mutableMapOf<String, Float>()
        val roa = mutableMapOf<String, Float>()
        
        val cal = java.util.Calendar.getInstance()
        var earliest = Long.MAX_VALUE
        
        doses.forEach { dose ->
            totalCost += dose.price ?: 0f
            
            cal.timeInMillis = dose.timestamp
            if (dose.timestamp < earliest) earliest = dose.timestamp
            
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
        
        val now = System.currentTimeMillis()
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
            for (time in startTime..now step stepMs) {
                var valueAtTime = 0.0
                for (dose in activeDoses) {
                    if (dose.timestamp > time || time - dose.timestamp > 48 * 60 * 60 * 1000L) continue 
                    val focus = com.example.domain.model.PharmacokineticEngine.calculateTotalConcentration(dose, substance, compounds, null, time)
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
        
        return SubstanceDetailState(
            substance = substance,
            compounds = compounds,
            variants = variants,
            doses = doses,
            totalDoses = totalDoses,
            avgPerDay = totalDoses.toFloat() / daysTotal,
            totalCost = totalCost,
            dayOfWeekDist = dow,
            hourOfDayDist = hod,
            variantUsage = varUsage,
            roaUsage = roa,
            kineticLines = line
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
