package com.example.ui.screens.logger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class LoggerState(
    val substances: List<Substance> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val compounds: List<Compound> = emptyList(),
    val quickDoses: List<QuickDose> = emptyList(),
    val selectedSubstance: Substance? = null,
    val selectedVariant: Variant? = null,
    val amount: Float = 0f,
    val unit: String = "mg",
    val route: String = "Oral",
    val timestamp: Long = System.currentTimeMillis(),
    val manualPrice: Float? = null,
    val notes: String = "",
    val estimatedOnset: Int? = null,
    val estimatedPeak: Int? = null,
    val estimatedDuration: Float? = null,
    val warningMessage: String? = null,
    val showQuickDoseSheet: Boolean = false
) {
    val computedPrice: Float
        get() = manualPrice ?: (selectedVariant?.pricePerUnit?.let { it * amount } ?: 0f)
}

class LoggerViewModel(
    private val repository: BioTrackRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoggerState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveSubstances().collectLatest { subs ->
                _state.update { it.copy(substances = subs) }
                if (subs.isNotEmpty() && _state.value.selectedSubstance == null) {
                    selectSubstance(subs.first())
                }
            }
        }
        viewModelScope.launch {
            repository.getAllQuickDoses().collectLatest { qd ->
                _state.update { it.copy(quickDoses = qd) }
            }
        }
    }

    fun selectSubstance(substance: Substance) {
        _state.update { it.copy(selectedSubstance = substance, unit = substance.defaultUnit) }
        viewModelScope.launch {
            val variants = repository.getVariantsForSubstance(substance.id).first()
            val lastDose = repository.getDosesForSubstance(substance.id).first().firstOrNull()
            
            _state.update { s -> 
                val newVariant = variants.firstOrNull()
                s.copy(
                    variants = variants,
                    selectedVariant = newVariant,
                    amount = lastDose?.doseAmount ?: 0f,
                    route = lastDose?.route ?: newVariant?.roaDefault ?: "Oral",
                    unit = lastDose?.unit ?: substance.defaultUnit
                )
            }
            updateEstimatesAndWarnings()
        }
    }

    fun selectVariant(variant: Variant?) {
        _state.update { it.copy(selectedVariant = variant) }
        updateEstimatesAndWarnings()
    }

    fun updateAmount(amount: Float) {
        _state.update { it.copy(amount = amount) }
        updateEstimatesAndWarnings()
    }

    fun updateRoute(route: String) {
        _state.update { it.copy(route = route) }
        updateEstimatesAndWarnings()
    }
    
    fun updateTime(timestamp: Long) {
        _state.update { it.copy(timestamp = timestamp) }
    }

    fun updatePrice(price: Float?) {
        _state.update { it.copy(manualPrice = price) }
    }
    
    fun updateTimestamp(time: Long) {
        _state.update { it.copy(timestamp = time) }
    }
    
    fun updateNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }
    
    fun toggleQuickDoseSheet(show: Boolean) {
        _state.update { it.copy(showQuickDoseSheet = show) }
    }
    
    fun applyQuickDose(qd: QuickDose, onSuccess: () -> Unit) {
        val sub = _state.value.substances.find { it.id == qd.substanceId }
        if (sub != null) {
            viewModelScope.launch {
                val variants = repository.getVariantsForSubstance(sub.id).first()
                val varnt = variants.find { it.id == qd.variantId } ?: variants.firstOrNull()
                
                val dose = Dose(
                    id = java.util.UUID.randomUUID().toString(),
                    substanceId = sub.id,
                    variantId = varnt?.id ?: "",
                    doseAmount = qd.defaultAmount,
                    unit = qd.defaultUnit,
                    route = qd.defaultRoute,
                    price = qd.defaultPrice,
                    timestamp = _state.value.timestamp,
                    notes = "Logged via Quick Dose"
                )
                repository.saveDose(dose)
                _state.update { it.copy(showQuickDoseSheet = false) }
                onSuccess()
            }
        }
    }

    fun saveQuickDose(qd: QuickDose) {
        viewModelScope.launch {
            repository.saveQuickDose(qd)
        }
    }

    fun deleteQuickDose(id: String) {
        viewModelScope.launch {
            repository.deleteQuickDose(id)
        }
    }

    private fun updateEstimatesAndWarnings() {
        val currentState = _state.value
        val sub = currentState.selectedSubstance ?: return
        
        viewModelScope.launch {
            val compounds = repository.getCompoundsForSubstance(sub.id).first()
            var warning: String? = null
            
            _state.update { it.copy(compounds = compounds) }
            
            // Farmakokinetický pre-kalkul
            val compound = compounds.firstOrNull()
            var onset = compound?.onsetMin
            var peak = compound?.peakMin
            var duration = compound?.durationHours
            
            val routeFactor = when (currentState.route.lowercase()) {
                "intranasal" -> 0.5f
                "inhalation" -> 0.2f
                "sublingual" -> 0.7f
                else -> 1f
            }
            
            onset = onset?.let { (it * routeFactor).toInt() }
            peak = peak?.let { (it * routeFactor).toInt() }
            duration = duration?.let { it * routeFactor }
            
            if (compound != null && compound.strongDose != null) {
                if (currentState.amount > compound.strongDose) {
                    warning = "WARNING: Amount exceeds strong dose threshold (${compound.strongDose} ${currentState.unit}). Proceed with caution."
                }
            }
            
            _state.update { 
                it.copy(
                    estimatedOnset = onset,
                    estimatedPeak = peak,
                    estimatedDuration = duration,
                    warningMessage = warning
                )
            }
        }
    }

    fun saveLog(onSuccess: () -> Unit) {
        val state = _state.value
        val subId = state.selectedSubstance?.id ?: return
        if (state.amount <= 0) return
        
        val dose = Dose(
            id = UUID.randomUUID().toString(),
            substanceId = subId,
            variantId = state.selectedVariant?.id,
            doseAmount = state.amount,
            unit = state.unit,
            route = state.route,
            price = state.computedPrice,
            timestamp = state.timestamp,
            notes = state.notes
        )
        
        viewModelScope.launch {
            repository.saveDose(dose)
            onSuccess()
            
            // Reset for next entry
            _state.update { it.copy(amount = 0f, notes = "", warningMessage = null, manualPrice = null) }
        }
    }
}

class LoggerViewModelFactory(
    private val repository: BioTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoggerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoggerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
