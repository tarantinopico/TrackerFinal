package com.example.ui.state

data class AppSettingsState(
    val privacyMode: Boolean = false,
    val financeMode: Boolean = true,
    val compactMode: Boolean = false,
    val warningsEnabled: Boolean = true,
    val themeMode: String = "Dark",
    val accentPalette: String = "Emerald"
)
