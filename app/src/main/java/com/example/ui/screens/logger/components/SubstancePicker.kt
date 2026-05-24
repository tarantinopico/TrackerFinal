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
import com.example.domain.model.Substance

@Composable
fun SubstancePicker(
    substances: List<Substance>,
    selectedSubstance: Substance?,
    onSubstanceSelected: (Substance) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        substances.forEach { sub ->
            FilterChip(
                selected = selectedSubstance?.id == sub.id,
                onClick = { onSubstanceSelected(sub) },
                label = { Text(sub.name) }
            )
        }
    }
}
