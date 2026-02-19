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
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(navController: NavController) {
    var birthDate by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Age Calculator") },
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
                value = birthDate, 
                onValueChange = { birthDate = it }, 
                label = { Text("Birthdate (YYYY-MM-DD)") },
                placeholder = { Text("e.g. 1995-05-15") }
            )
            Button(
                onClick = {
                    try {
                        val birth = LocalDate.parse(birthDate.trim())
                        val now = LocalDate.now()
                        val period = Period.between(birth, now)
                        val seconds = ChronoUnit.SECONDS.between(birth.atStartOfDay(), now.atStartOfDay())
                        age = "${period.years} years, ${period.months} months, ${period.days} days\nTotal: $seconds seconds"
                    } catch (e: Exception) {
                        age = "Invalid date format (YYYY-MM-DD)"
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) { Text("Calculate") }
            
            Text(text = "Age: $age", modifier = Modifier.padding(top = 16.dp))
        }
    }
}
