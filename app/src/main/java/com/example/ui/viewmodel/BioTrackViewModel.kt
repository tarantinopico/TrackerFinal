package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.BioTrackRepository
import com.example.ui.state.AppSettingsState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class BioTrackViewModel(
    private val repository: BioTrackRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow(AppSettingsState())
    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    private val _substances = MutableStateFlow<List<Substance>>(emptyList())
    val substances: StateFlow<List<Substance>> = _substances.asStateFlow()

    private val _doses = MutableStateFlow<List<Dose>>(emptyList())
    val logs: StateFlow<List<Dose>> = _doses.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveSubstances().collectLatest { 
                _substances.value = it 
            }
        }
        viewModelScope.launch {
            repository.getAllDoses().collectLatest { 
                _doses.value = it 
            }
        }
        
        seedDataIfEmpty()
    }

    private fun seedDataIfEmpty() = viewModelScope.launch {
        repository.getAllSubstances().collect { subs ->
            if (subs.isEmpty()) {
                val caffeine = Substance(
                    id = UUID.randomUUID().toString(),
                    name = "Caffeine",
                    category = SubstanceCategory.STIMULANT,
                    notes = "A central nervous system stimulant.",
                    defaultUnit = "mg",
                    iconKey = "ic_coffee"
                )
                repository.saveSubstance(caffeine)
                
                val vitaminD = Substance(
                    id = UUID.randomUUID().toString(),
                    name = "Vitamin D3",
                    category = SubstanceCategory.SUPPLEMENT,
                    notes = "Fat-soluble vitamin.",
                    defaultUnit = "IU",
                    iconKey = "ic_pill"
                )
                repository.saveSubstance(vitaminD)
            }
        }
    }

    fun togglePrivacyMode() {
        _settingsState.update { it.copy(privacyMode = !it.privacyMode) }
    }

    fun updateThemeMode(mode: String) {
        _settingsState.update { it.copy(themeMode = mode) }
    }

    fun updateAccentPalette(accent: String) {
        _settingsState.update { it.copy(accentPalette = accent) }
    }

    fun addLog(substanceId: String, amount: Float, unit: String, notes: String, cost: Float?) {
        viewModelScope.launch {
            val dose = Dose(
                id = UUID.randomUUID().toString(),
                substanceId = substanceId,
                doseAmount = amount,
                unit = unit,
                timestamp = System.currentTimeMillis(),
                notes = notes,
                price = cost,
                route = "Oral"
            )
            repository.saveDose(dose)
        }
    }
}

class BioTrackViewModelFactory(
    private val repository: BioTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BioTrackViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BioTrackViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
