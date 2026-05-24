package com.example.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.Substance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    substances: List<Substance>,
    onSave: (String, Float, String, String, Float?) -> Unit,
    onBack: () -> Unit
) {
    var selectedSubstance by remember { mutableStateOf<Substance?>(substances.firstOrNull()) }
    var amountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log entry") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Substance Selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedSubstance?.name ?: "Select Substance",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Substance") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    substances.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text(sub.name) },
                            onClick = {
                                selectedSubstance = sub
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Amount (${selectedSubstance?.defaultUnit ?: ""})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amount = amountStr.toFloatOrNull()
                    if (amount != null && selectedSubstance != null) {
                        onSave(selectedSubstance!!.id, amount, selectedSubstance!!.defaultUnit, notes, null)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amountStr.isNotBlank() && selectedSubstance != null
            ) {
                Text("Save Entry")
            }
        }
    }
}
