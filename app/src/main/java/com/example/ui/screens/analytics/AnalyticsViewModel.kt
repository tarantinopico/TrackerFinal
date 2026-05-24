package com.example.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.BioTrackRepository
import com.example.domain.analytics.GlobalAnalytics
import com.example.domain.analytics.FinanceAnalytics
import com.example.domain.analytics.AnalyticsEngine
import kotlinx.coroutines.flow.*

data class AnalyticsState(
    val substances: List<Substance> = emptyList(),
    val doses: List<Dose> = emptyList(),
    val compounds: List<Compound> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val settings: AppSettings? = null,
    val selectedSubsubsection: AnalyticsSection = AnalyticsSection.SUMMARY,
    val detailSubstanceId: String? = null,
    val globalAnalytics: GlobalAnalytics? = null,
    val financeAnalytics: FinanceAnalytics? = null
)

enum class AnalyticsSection {
    SUMMARY, SUBSTANCES, CATEGORIES, TIMING, HISTORY_CALENDAR, FINANCE
}

class AnalyticsViewModel(private val repository: BioTrackRepository) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    init {
        combine(
            repository.getAllSubstances(),
            repository.getAllDoses(),
            repository.getAllCompounds(),
            repository.getAllVariants(),
            repository.getSettings()
        ) { subs, dses, cmps, vts, setts ->
            val global = AnalyticsEngine.computeGlobalAnalytics(dses, subs, vts, setts)
            val finance = AnalyticsEngine.computeFinanceAnalytics(dses, subs, vts, setts)
            
            AnalyticsState(
                substances = subs,
                doses = dses,
                compounds = cmps,
                variants = vts,
                settings = setts,
                selectedSubsubsection = _state.value.selectedSubsubsection,
                detailSubstanceId = _state.value.detailSubstanceId,
                globalAnalytics = global,
                financeAnalytics = finance
            )
        }.onEach { s ->
            _state.value = s
        }.launchIn(viewModelScope)
    }

    fun selectSection(section: AnalyticsSection) {
        _state.update { it.copy(selectedSubsubsection = section, detailSubstanceId = null) }
    }

    fun viewSubstanceDetail(substanceId: String?) {
        _state.update { it.copy(detailSubstanceId = substanceId) }
    }
}

class AnalyticsViewModelFactory(private val repository: BioTrackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
