package com.example.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.screens.settings.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(state.backupMessage) {
        state.backupMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearBackupMessage()
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            ProfileSection(
                settings = state.settings,
                onUpdate = { transform -> viewModel.updateSettings(transform) }
            )
            
            AppearanceSection(
                settings = state.settings,
                onUpdate = { transform -> viewModel.updateSettings(transform) }
            )
            
            PrivacySection(
                settings = state.settings,
                onUpdate = { transform -> viewModel.updateSettings(transform) }
            )
            
            DataManagementSection(
                onExport = { uri -> viewModel.exportDatabase(uri) },
                onImport = { uri, overwrite -> viewModel.importDatabase(uri, overwrite) },
                onWipeData = { viewModel.clearAllData() }
            )
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
