package com.example.premiumcalculator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmiCalculatorScreen(navController: NavController) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var emi by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EMI Calculator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            TextField(value = principal, onValueChange = { principal = it }, label = { Text("Principal Amount") }, modifier = Modifier.padding(bottom = 8.dp))
            TextField(value = rate, onValueChange = { rate = it }, label = { Text("Annual Rate (%)") }, modifier = Modifier.padding(bottom = 8.dp))
            TextField(value = tenure, onValueChange = { tenure = it }, label = { Text("Tenure (months)") }, modifier = Modifier.padding(bottom = 16.dp))
            
            Button(onClick = {
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = (rate.toDoubleOrNull() ?: 0.0) / 1200
                val n = tenure.toDoubleOrNull() ?: 0.0
                if (r > 0 && n > 0) {
                    val emiVal = p * r * (1 + r).pow(n) / ((1 + r).pow(n) - 1)
                    emi = String.format("%.2f", emiVal)
                } else if (r == 0.0 && n > 0) {
                    emi = String.format("%.2f", p / n)
                } else {
                    emi = "Invalid Input"
                }
            }) { Text("Calculate EMI") }
            
            if (emi.isNotEmpty()) {
                Text(text = "Monthly EMI: $emi", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
