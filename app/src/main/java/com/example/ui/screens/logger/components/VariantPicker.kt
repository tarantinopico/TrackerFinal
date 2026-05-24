package com.example.ui.screens.logger.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.Variant

@Composable
fun VariantPicker(
    variants: List<Variant>,
    selectedVariant: Variant?,
    onVariantSelected: (Variant) -> Unit
) {
    if (variants.isEmpty()) {
        Text("No variants available for this substance.", modifier = Modifier.fillMaxWidth())
        return
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        variants.forEach { variant ->
            FilterChip(
                selected = selectedVariant?.id == variant.id,
                onClick = { onVariantSelected(variant) },
                label = { Text(variant.name) }
            )
        }
    }
}
