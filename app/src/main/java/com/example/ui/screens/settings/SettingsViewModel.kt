package com.example.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.domain.model.AppSettings
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val backupMessage: String? = null
)

class SettingsViewModel(
    private val repository: BioTrackRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        repository.getSettings().onEach { currentSettings ->
            _state.update { it.copy(settings = currentSettings ?: AppSettings(), isLoading = false) }
        }.launchIn(viewModelScope)
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val current = _state.value.settings
            val newSettings = transform(current)
            repository.saveSettings(newSettings)
        }
    }

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportBackup(uri)
            if (result.isSuccess) {
                _state.update { it.copy(backupMessage = "Export successful!") }
            } else {
                _state.update { it.copy(backupMessage = "Export failed: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun importDatabase(uri: Uri, overwrite: Boolean) {
        viewModelScope.launch {
            val result = backupManager.importBackup(uri, overwrite)
            if (result.isSuccess) {
                _state.update { it.copy(backupMessage = "Import successful!") }
            } else {
                _state.update { it.copy(backupMessage = "Import failed: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun clearBackupMessage() {
        _state.update { it.copy(backupMessage = null) }
    }
}

class SettingsViewModelFactory(
    private val repository: BioTrackRepository,
    private val backupManager: BackupManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository, backupManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
