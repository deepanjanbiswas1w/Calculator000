package com.example.premiumcalculator.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.premiumcalculator.viewmodel.CalculatorViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.map
import com.example.premiumcalculator.dataStore

private val HAPTIC_KEY = booleanPreferencesKey("haptic")
private val BUTTON_ROUND_KEY = booleanPreferencesKey("button_round")
private val GLASSMORPHISM_KEY = booleanPreferencesKey("glassmorphism")

data class CalcButton(val text: String)

private val basicButtons = listOf(
    CalcButton("7"), CalcButton("8"), CalcButton("9"), CalcButton("/"),
    CalcButton("4"), CalcButton("5"), CalcButton("6"), CalcButton("*"),
    CalcButton("1"), CalcButton("2"), CalcButton("3"), CalcButton("-"),
    CalcButton("0"), CalcButton("."), CalcButton("="), CalcButton("+"),
    CalcButton("C"), CalcButton("DEL")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(navController: NavController) {
    val viewModel: CalculatorViewModel = hiltViewModel()
    val context = LocalContext.current
    val haptic by context.dataStore.data.map { it[HAPTIC_KEY] ?: true }.collectAsState(initial = true)
    val glassmorphism by context.dataStore.data.map { it[GLASSMORPHISM_KEY] ?: false }.collectAsState(initial = false)
    val buttonRound by context.dataStore.data.map { it[BUTTON_ROUND_KEY] ?: true }.collectAsState(initial = true)
    
    val expression by viewModel.expression
    val preview by viewModel.preview

    Column(modifier = Modifier.fillMaxSize().background(if (glassmorphism) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
            IconButton(onClick = { navController.navigate("history") }) { Icon(Icons.Default.Refresh, "History") }
        }
        
        Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.End) {
            Text(text = expression, style = MaterialTheme.typography.headlineMedium)
            Text(text = preview, style = MaterialTheme.typography.displayLarge)
        }
        
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.padding(8.dp)) {
            items(basicButtons) { button ->
                Card(
                    modifier = Modifier.padding(4.dp).aspectRatio(1f).clickable { 
                        if (haptic) {
                            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        viewModel.onButtonClick(button.text)
                    },
                    shape = if (buttonRound) CircleShape else RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(button.text, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
