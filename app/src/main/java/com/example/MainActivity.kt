package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.example.ui.navigation.BioTrackApp
import com.example.ui.theme.BioTrackTheme
import com.example.ui.viewmodel.BioTrackViewModel
import com.example.ui.viewmodel.BioTrackViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: BioTrackViewModel by viewModels {
        BioTrackViewModelFactory((application as BioTrackApplication).container.bioTrackRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsState = viewModel.settingsState.collectAsState().value
            
            BioTrackTheme(
                themeMode = settingsState.themeMode,
                accentPaletteStr = settingsState.accentPalette
            ) {
                BioTrackApp(viewModel)
            }
        }
    }
}
