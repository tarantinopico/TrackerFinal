package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AccentPalette { EMERALD, SAPPHIRE, AMETHYST, ROSE, AMBER }

private fun getDarkColorScheme(accent: AccentPalette): ColorScheme {
    val (primary, secondary, tertiary) = when (accent) {
        AccentPalette.EMERALD -> Triple(EmeraldPrimary, EmeraldSecondary, EmeraldTertiary)
        AccentPalette.SAPPHIRE -> Triple(SapphirePrimary, SapphireSecondary, SapphireTertiary)
        AccentPalette.AMETHYST -> Triple(AmethystPrimary, AmethystSecondary, AmethystTertiary)
        AccentPalette.ROSE -> Triple(RosePrimary, RoseSecondary, RoseTertiary)
        AccentPalette.AMBER -> Triple(AmberPrimary, AmberSecondary, AmberTertiary)
    }
    return darkColorScheme(
        primary = primary,
        secondary = secondary,
        tertiary = tertiary,
        background = OledBlack,
        surface = DarkSurface,
        surfaceVariant = DarkSurfaceVariant,
        onPrimary = OledBlack,
        onSecondary = OledBlack,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        onSurfaceVariant = TextSecondaryDark,
        error = DangerRed
    )
}

private fun getLightColorScheme(accent: AccentPalette): ColorScheme {
    val (primary, secondary, tertiary) = when (accent) {
        AccentPalette.EMERALD -> Triple(EmeraldSecondary, EmeraldPrimary, EmeraldTertiary)
        AccentPalette.SAPPHIRE -> Triple(SapphireSecondary, SapphirePrimary, SapphireTertiary)
        AccentPalette.AMETHYST -> Triple(AmethystSecondary, AmethystPrimary, AmethystTertiary)
        AccentPalette.ROSE -> Triple(RoseSecondary, RosePrimary, RoseTertiary)
        AccentPalette.AMBER -> Triple(AmberSecondary, AmberPrimary, AmberTertiary)
    }
    return lightColorScheme(
        primary = primary,
        secondary = secondary,
        tertiary = tertiary,
        background = LightBackground,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = TextPrimaryLight,
        onSurface = TextPrimaryLight,
        onSurfaceVariant = TextSecondaryLight,
        error = DangerRed
    )
}

@Composable
fun BioTrackTheme(
    themeMode: String = "Dark",
    accentPaletteStr: String = "Emerald",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemInDarkTheme()
    }

    val accent = try {
        AccentPalette.valueOf(accentPaletteStr.uppercase())
    } catch (e: Exception) {
        AccentPalette.EMERALD
    }

    val colorScheme = if (darkTheme) getDarkColorScheme(accent) else getLightColorScheme(accent)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

