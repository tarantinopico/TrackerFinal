package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme() // Or read from theme mode if explicitly passed, but MaterialTheme colors do the job
    val borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)
    val gradientStart = if (isDark) Color(0x1AFFFFFF) else Color(0x66FFFFFF)
    
    Box(
        modifier = modifier
            .shadow(
                elevation = if(isDark) 0.dp else 8.dp,
                shape = MaterialTheme.shapes.large,
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        gradientStart,
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(padding)
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun GlassCardPreview() {
    MaterialTheme {
        GlassCard {
            Text("This is a glass card")
        }
    }
}

