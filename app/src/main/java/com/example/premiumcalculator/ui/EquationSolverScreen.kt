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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.premiumcalculator.viewmodel.EquationSolverViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquationSolverScreen(navController: NavController) {
    val viewModel: EquationSolverViewModel = hiltViewModel()
    var equation by remember { mutableStateOf("") }
    val result by viewModel.result

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Equation Solver") },
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
                value = equation, 
                onValueChange = { equation = it }, 
                label = { Text("Enter equation (e.g., 2x^2 + 3x - 2 = 0)") }
            )
            Button(onClick = { viewModel.solve(equation) }, modifier = Modifier.padding(top = 8.dp)) { 
                Text("Solve") 
            }
            Text("Result: $result", modifier = Modifier.padding(top = 16.dp))
        }
    }
}
