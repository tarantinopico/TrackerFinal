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
    MONTH_3(90, "3M"),
    MONTH_6(180, "6M"),
    YEAR_1(365, "1Y"),
    ALL(-1, "ALL")
}

data class TimePoint(val label: String, val value: Float)

data class SubstanceDetailState(
    val substance: Substance? = null,
    val compounds: List<Compound> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val doses: List<Dose> = emptyList(),
    val timePeriod: TimePeriod = TimePeriod.DAYS_14,
    val analytics: com.example.domain.analytics.SubstanceAnalyticsEngine.AnalyticsResult = com.example.domain.analytics.SubstanceAnalyticsEngine().calculate(null, emptyList(), emptyList(), emptyList(), TimePeriod.DAYS_14)
)

class SubstanceDetailViewModel(
    private val repository: BioTrackRepository,
    private val substanceId: String
) : ViewModel() {

    private val engine = com.example.domain.analytics.SubstanceAnalyticsEngine()

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
                val result = engine.calculate(substance, compounds, variants, doses, _state.value.timePeriod)
                SubstanceDetailState(substance, compounds, variants, doses, _state.value.timePeriod, result)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }
    
    fun setTimePeriod(period: TimePeriod) {
        val currentState = _state.value
        val result = engine.calculate(currentState.substance, currentState.compounds, currentState.variants, currentState.doses, period)
        _state.update { it.copy(timePeriod = period, analytics = result) }
    }
    
    fun deleteDose(doseId: String) {
        viewModelScope.launch {
            repository.deleteDose(doseId)
        }
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
