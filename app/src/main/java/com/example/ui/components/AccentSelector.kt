package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AccentSelector(
    selectedAccent: String,
    onAccentSelected: (String) -> Unit
) {
    val accents = listOf(
        "Emerald" to Color(0xFF10B981),
        "Sapphire" to Color(0xFF3B82F6),
        "Amethyst" to Color(0xFF8B5CF6),
        "Rose" to Color(0xFFF43F5E),
        "Amber" to Color(0xFFF59E0B)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(accents) { (name, color) ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onAccentSelected(name) }
                    .border(
                        width = if (selectedAccent == name) 3.dp else 0.dp,
                        color = if (selectedAccent == name) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // optional: add check icon inside if selected
            }
        }
    }
}
