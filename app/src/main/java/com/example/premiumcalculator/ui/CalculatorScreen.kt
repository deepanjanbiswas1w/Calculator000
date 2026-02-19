package com.example.premiumcalculator.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.premiumcalculator.viewmodel.CalculatorViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.map
import com.example.premiumcalculator.App
import com.example.premiumcalculator.Theme.dataStore

private val HAPTIC_KEY = booleanPreferencesKey("haptic")
private val PRECISION_KEY = intPreferencesKey("precision")
private val BUTTON_ROUND_KEY = booleanPreferencesKey("button_round")
private val GLASSMORPHISM_KEY = booleanPreferencesKey("glassmorphism")

data class CalcButton(val text: String)

private val basicButtons = listOf(
    CalcButton("7"), CalcButton("8"), CalcButton("9"), CalcButton("/"),
    CalcButton("4"), CalcButton("5"), CalcButton("6"), CalcButton("*"),
    CalcButton("1"), CalcButton("2"), CalcButton("3"), CalcButton("-"),
    CalcButton("0"), CalcButton("."), CalcButton("="), CalcButton("+"),
    CalcButton("("), CalcButton(")"), CalcButton("C"), CalcButton("DEL"), CalcButton("%"), CalcButton("^")
)

private val scientificButtons = basicButtons + listOf(
    CalcButton("sin"), CalcButton("cos"), CalcButton("tan"), CalcButton("log"), CalcButton("ln"),
    CalcButton("π"), CalcButton("e"), CalcButton("!"), CalcButton("√")
)

@Composable
fun CalculatorScreen(navController: NavController) {
    val viewModel: CalculatorViewModel = hiltViewModel()
    val context = LocalContext.current
    val haptic by context.dataStore.data.map { it[HAPTIC_KEY] ?: true }.collectAsState(initial = true)
    val glassmorphism by context.dataStore.data.map { it[GLASSMORPHISM_KEY] ?: false }.collectAsState(initial = false)
    val buttonRound by context.dataStore.data.map { it[BUTTON_ROUND_KEY] ?: true }.collectAsState(initial = true)
    val scientificMode = remember { mutableStateOf(false) }
    val buttons = if (scientificMode.value) scientificButtons else basicButtons
    val expression by viewModel.expression
    val preview by viewModel.preview

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (glassmorphism) Color.White.copy(alpha = 0.2f) else Color.Transparent)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sci-Mode")
            Switch(checked = scientificMode.value, onCheckedChange = { scientificMode.value = it })
            IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
            IconButton(onClick = { navController.navigate("history") }) { Icon(Icons.Default.History, "History") }
            IconButton(onClick = { navController.navigate("solver") }) { Text("Solv") }
            IconButton(onClick = { navController.navigate("age") }) { Text("Age") }
            IconButton(onClick = { navController.navigate("land") }) { Text("Land") }
            IconButton(onClick = { navController.navigate("emi") }) { Text("EMI") }
            IconButton(onClick = { navController.navigate("discount") }) { Text("Disc") }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = expression, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            Text(text = preview, style = MaterialTheme.typography.displayLarge, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
        }
        LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.padding(8.dp)) {
            items(buttons) { button ->
                AnimatedButton(
                    text = button.text,
                    shape = if (buttonRound) CircleShape else RoundedCornerShape(4.dp),
                    onClick = {
                        if (haptic) (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        viewModel.onButtonClick(button.text)
                    }
                )
            }
        }
    }
}

@Composable
fun AnimatedButton(text: String, shape: Shape, onClick: () -> Unit) {
    val pressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed.value) 0.9f else 1f)

    Card(
        modifier = Modifier
            .padding(4.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed.value = true
                    tryAwaitRelease()
                    pressed.value = false
                    onClick()
                })
            },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Text(text, style = MaterialTheme.typography.titleLarge)
        }
    }
}
