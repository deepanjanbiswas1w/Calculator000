package com.example.premiumcalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactorialScreen(navController: NavController) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Factorial Calculator") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { if(it.length <= 4) input = it },
                label = { Text("Enter a number (Max 5000)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                val n = input.toIntOrNull() ?: 0
                if (n > 5000) {
                    result = "Too Large!"
                } else {
                    var fact = BigInteger.ONE
                    for (i in 1..n) fact = fact.multiply(BigInteger.valueOf(i.toLong()))
                    result = fact.toString()
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Calculate !") }
            if (result.isNotEmpty()) {
                Text("Result: $result", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}
