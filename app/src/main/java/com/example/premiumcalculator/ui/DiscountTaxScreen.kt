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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountTaxScreen(navController: NavController) {
    var price by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("") }
    var tax by remember { mutableStateOf("") }
    var finalPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discount/Tax Calculator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            TextField(value = price, onValueChange = { price = it }, label = { Text("Original Price") }, modifier = Modifier.padding(bottom = 8.dp))
            TextField(value = discount, onValueChange = { discount = it }, label = { Text("Discount (%)") }, modifier = Modifier.padding(bottom = 8.dp))
            TextField(value = tax, onValueChange = { tax = it }, label = { Text("Tax (%)") }, modifier = Modifier.padding(bottom = 16.dp))
            
            Button(onClick = {
                val p = price.toDoubleOrNull() ?: 0.0
                val d = discount.toDoubleOrNull() ?: 0.0
                val t = tax.toDoubleOrNull() ?: 0.0
                val afterDiscount = p - (p * d / 100)
                val afterTax = afterDiscount + (afterDiscount * t / 100)
                finalPrice = String.format("%.2f", afterTax)
            }) { Text("Calculate Final Price") }
            
            if (finalPrice.isNotEmpty()) {
                Text(text = "Final Price: $finalPrice", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
