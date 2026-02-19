package com.example.premiumcalculator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandConverterScreen(navController: NavController) {
    var value by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf("Acre") }
    var toUnit by remember { mutableStateOf("Bigha") }
    var result by remember { mutableStateOf("") }
    val units = listOf("Acre", "Bigha", "Kattha", "Shotok")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Land Converter") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            TextField(
                value = value, 
                onValueChange = { value = it }, 
                label = { Text("Enter Value") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                UnitDropdown(units, fromUnit) { fromUnit = it }
                Text(" to ", modifier = Modifier.padding(horizontal = 8.dp))
                UnitDropdown(units, toUnit) { toUnit = it }
            }
            Button(onClick = {
                val v = value.toDoubleOrNull() ?: 0.0
                val toAcre = when (fromUnit) {
                    "Bigha" -> v * 0.3305
                    "Kattha" -> v * 0.0165
                    "Shotok" -> v * 0.01
                    else -> v
                }
                val finalRes = when (toUnit) {
                    "Bigha" -> (toAcre / 0.3305)
                    "Kattha" -> (toAcre / 0.0165)
                    "Shotok" -> (toAcre / 0.01)
                    else -> toAcre
                }
                result = String.format("%.4f", finalRes)
            }) { Text("Convert") }
            
            if (result.isNotEmpty()) {
                Text(text = "Result: $result $toUnit", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Composable
fun UnitDropdown(units: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Button(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            units.forEach { unit ->
                DropdownMenuItem(text = { Text(unit) }, onClick = { onSelect(unit); expanded = false })
            }
        }
    }
}
