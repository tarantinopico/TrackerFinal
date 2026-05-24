package com.example.ui.screens.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class LabFilter { ALL, ACTIVE, ARCHIVED }

data class LabState(
    val substances: List<Substance> = emptyList(),
    val filter: LabFilter = LabFilter.ACTIVE,
    
    val viewingSubstanceId: String? = null,
    val selectedSubstance: Substance? = null,
    val selectedCompounds: List<Compound> = emptyList(),
    val selectedVariants: List<Variant> = emptyList(),
    
    // Editors
    val editingSubstance: Substance? = null,
    val isEditingNewSubstance: Boolean = false,
    
    val editingCompound: Compound? = null,
    val isEditingNewCompound: Boolean = false,
    
    val editingVariant: Variant? = null,
    val isEditingNewVariant: Boolean = false,
    
    // Dialogs
    val deleteConfirmCompound: Compound? = null,
    val deleteConfirmVariant: Variant? = null,
    val deleteConfirmSubstance: Substance? = null
)

class LabViewModel(private val repository: BioTrackRepository) : ViewModel() {
    private val _state = MutableStateFlow(LabState())
    val state = _state.asStateFlow()

    init {
        // Load all substances and apply filter
        viewModelScope.launch {
            repository.getAllSubstances().collectLatest { allSubs ->
                val filtered = when (_state.value.filter) {
                    LabFilter.ALL -> allSubs
                    LabFilter.ACTIVE -> allSubs.filter { it.active }
                    LabFilter.ARCHIVED -> allSubs.filter { !it.active || it.archivedAt != null }
                }
                
                // Update selected substance if it was modified
                val currentlyViewing = _state.value.viewingSubstanceId
                val updatedSelected = allSubs.find { it.id == currentlyViewing }
                
                _state.update { it.copy(
                    substances = filtered,
                    selectedSubstance = updatedSelected
                ) }
            }
        }
    }

    fun setFilter(filter: LabFilter) {
        _state.update { it.copy(filter = filter) }
        // force reload
        viewModelScope.launch {
            val allSubs = repository.getAllSubstances().first()
            val filtered = when (filter) {
                LabFilter.ALL -> allSubs
                LabFilter.ACTIVE -> allSubs.filter { it.active }
                LabFilter.ARCHIVED -> allSubs.filter { !it.active || it.archivedAt != null }
            }
            _state.update { it.copy(substances = filtered) }
        }
    }

    fun viewSubstance(substanceId: String?) {
        _state.update { it.copy(viewingSubstanceId = substanceId) }
        if (substanceId == null) {
            _state.update { it.copy(
                selectedSubstance = null,
                selectedCompounds = emptyList(),
                selectedVariants = emptyList()
            ) }
            return
        }

        viewModelScope.launch {
            val sub = repository.getSubstanceById(substanceId)
            _state.update { it.copy(selectedSubstance = sub) }
            
            launch {
                repository.getCompoundsForSubstance(substanceId).collectLatest { cmp ->
                    // Only update if we're still viewing the same substance (debouncing check)
                    if (_state.value.viewingSubstanceId == substanceId) {
                        _state.update { it.copy(selectedCompounds = cmp) }
                    }
                }
            }
            launch {
                repository.getVariantsForSubstance(substanceId).collectLatest { vrnt ->
                    if (_state.value.viewingSubstanceId == substanceId) {
                        _state.update { it.copy(selectedVariants = vrnt) }
                    }
                }
            }
        }
    }

    // --- Substance Editor ---
    fun openNewSubstanceEditor() {
        _state.update { it.copy(
            isEditingNewSubstance = true, 
            editingSubstance = Substance(
                id = UUID.randomUUID().toString(),
                name = "",
                category = SubstanceCategory.OTHER,
                defaultUnit = "mg"
            )
        ) }
    }
    fun openSubstanceEditor(substance: Substance) {
        _state.update { it.copy(isEditingNewSubstance = false, editingSubstance = substance) }
    }
    fun updateEditingSubstance(sub: Substance) {
        _state.update { it.copy(editingSubstance = sub) }
    }
    fun saveSubstance() {
        val sub = _state.value.editingSubstance ?: return
        viewModelScope.launch {
            repository.saveSubstance(sub.copy(updatedAt = System.currentTimeMillis()))
            _state.update { it.copy(editingSubstance = null, isEditingNewSubstance = false) }
        }
    }
    fun closeSubstanceEditor() {
        _state.update { it.copy(editingSubstance = null, isEditingNewSubstance = false) }
    }
    fun archiveSubstance(substance: Substance, archive: Boolean) {
        viewModelScope.launch {
            repository.saveSubstance(substance.copy(
                active = !archive,
                archivedAt = if (archive) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
    fun confirmDeleteSubstance(substance: Substance) {
        _state.update { it.copy(deleteConfirmSubstance = substance) }
    }
    fun performDeleteSubstance() {
        val sub = _state.value.deleteConfirmSubstance ?: return
        viewModelScope.launch {
            repository.deleteSubstance(sub.id)
            if (_state.value.viewingSubstanceId == sub.id) {
                viewSubstance(null)
            }
            _state.update { it.copy(deleteConfirmSubstance = null) }
        }
    }
    fun cancelDeleteSubstance() {
        _state.update { it.copy(deleteConfirmSubstance = null) }
    }

    // --- Compound Editor ---
    fun openNewCompoundEditor() {
        val subId = _state.value.selectedSubstance?.id ?: return
        _state.update { it.copy(
            isEditingNewCompound = true, 
            editingCompound = Compound(
                id = UUID.randomUUID().toString(),
                substanceId = subId,
                name = "",
            )
        ) }
    }
    fun openCompoundEditor(compound: Compound) {
        _state.update { it.copy(isEditingNewCompound = false, editingCompound = compound) }
    }
    fun updateEditingCompound(cmp: Compound) {
        _state.update { it.copy(editingCompound = cmp) }
    }
    fun saveCompound() {
        val cmp = _state.value.editingCompound ?: return
        viewModelScope.launch {
            repository.saveCompound(cmp.copy(updatedAt = System.currentTimeMillis()))
            _state.update { it.copy(editingCompound = null, isEditingNewCompound = false) }
        }
    }
    fun closeCompoundEditor() {
        _state.update { it.copy(editingCompound = null, isEditingNewCompound = false) }
    }
    fun confirmDeleteCompound(compound: Compound) {
        _state.update { it.copy(deleteConfirmCompound = compound) }
    }
    fun performDeleteCompound() {
        val cmp = _state.value.deleteConfirmCompound ?: return
        viewModelScope.launch {
            repository.deleteCompound(cmp.id)
            _state.update { it.copy(deleteConfirmCompound = null) }
        }
    }
    fun cancelDeleteCompound() {
        _state.update { it.copy(deleteConfirmCompound = null) }
    }

    // --- Variant Editor ---
    fun openNewVariantEditor() {
        val subId = _state.value.selectedSubstance?.id ?: return
        _state.update { it.copy(
            isEditingNewVariant = true, 
            editingVariant = Variant(
                id = UUID.randomUUID().toString(),
                substanceId = subId,
                name = "",
                colorHex = "#FFFFFF"
            )
        ) }
    }
    fun openVariantEditor(variant: Variant) {
        _state.update { it.copy(isEditingNewVariant = false, editingVariant = variant) }
    }
    fun updateEditingVariant(variant: Variant) {
        _state.update { it.copy(editingVariant = variant) }
    }
    fun saveVariant() {
        val variant = _state.value.editingVariant ?: return
        viewModelScope.launch {
            repository.saveVariant(variant.copy(updatedAt = System.currentTimeMillis()))
            _state.update { it.copy(editingVariant = null, isEditingNewVariant = false) }
        }
    }
    fun closeVariantEditor() {
        _state.update { it.copy(editingVariant = null, isEditingNewVariant = false) }
    }
    fun confirmDeleteVariant(variant: Variant) {
        _state.update { it.copy(deleteConfirmVariant = variant) }
    }
    fun performDeleteVariant() {
        val variant = _state.value.deleteConfirmVariant ?: return
        viewModelScope.launch {
            repository.deleteVariant(variant.id)
            _state.update { it.copy(deleteConfirmVariant = null) }
        }
    }
    fun cancelDeleteVariant() {
        _state.update { it.copy(deleteConfirmVariant = null) }
    }
}

class LabViewModelFactory(private val repository: BioTrackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LabViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
