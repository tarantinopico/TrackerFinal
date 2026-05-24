package com.example.ui.screens.logger.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RouteSelector(
    selectedRoute: String,
    onRouteSelected: (String) -> Unit
) {
    val routes = listOf("Oral", "Sublingual", "Intranasal", "Inhalation")
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        routes.forEach { route ->
            ElevatedFilterChip(
                selected = selectedRoute == route,
                onClick = { onRouteSelected(route) },
                label = { Text(route) }
            )
        }
    }
}
