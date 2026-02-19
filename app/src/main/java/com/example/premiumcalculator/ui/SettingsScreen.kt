package com.example.premiumcalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.map
import com.example.premiumcalculator.dataStore

private val THEME_KEY = stringPreferencesKey("theme")
private val HAPTIC_KEY = booleanPreferencesKey("haptic")
private val PRECISION_KEY = intPreferencesKey("precision")
private val PRIMARY_COLOR_KEY = longPreferencesKey("primary_color")
private val GLASSMORPHISM_KEY = booleanPreferencesKey("glassmorphism")
private val BUTTON_ROUND_KEY = booleanPreferencesKey("button_round")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme by context.dataStore.data.map { it[THEME_KEY] ?: "light" }.collectAsState("light")
    val haptic by context.dataStore.data.map { it[HAPTIC_KEY] ?: true }.collectAsState(true)
    val precision by context.dataStore.data.map { it[PRECISION_KEY] ?: 6 }.collectAsState(6)
    val primary by context.dataStore.data.map { it[PRIMARY_COLOR_KEY] ?: 0xFF6200EE }.collectAsState(0xFF6200EE)
    val glassmorphism by context.dataStore.data.map { it[GLASSMORPHISM_KEY] ?: false }.collectAsState(false)
    val buttonRound by context.dataStore.data.map { it[BUTTON_ROUND_KEY] ?: true }.collectAsState(true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Text("Theme")
            Row {
                RadioButton(selected = theme == "light", onClick = { scope.launch { context.dataStore.edit { it[THEME_KEY] = "light" } } })
                Text("Light")
                RadioButton(selected = theme == "dark", onClick = { scope.launch { context.dataStore.edit { it[THEME_KEY] = "dark" } } })
                Text("Dark")
            }

            Text("Glassmorphism", modifier = Modifier.padding(top = 16.dp))
            Switch(checked = glassmorphism, onCheckedChange = { isChecked -> 
                scope.launch { context.dataStore.edit { prefs -> prefs[GLASSMORPHISM_KEY] = isChecked } } 
            })

            Text("Button Shape Round", modifier = Modifier.padding(top = 16.dp))
            Switch(checked = buttonRound, onCheckedChange = { isChecked -> 
                scope.launch { context.dataStore.edit { prefs -> prefs[BUTTON_ROUND_KEY] = isChecked } } 
            })

            Text("Haptic Feedback", modifier = Modifier.padding(top = 16.dp))
            Switch(checked = haptic, onCheckedChange = { isChecked -> 
                scope.launch { context.dataStore.edit { prefs -> prefs[HAPTIC_KEY] = isChecked } } 
            })

            Text("Decimal Places (2, 4, 6)", modifier = Modifier.padding(top = 16.dp))
            Slider(
                value = precision.toFloat(),
                onValueChange = { scope.launch { context.dataStore.edit { it[PRECISION_KEY] = precision.toInt() } } },
                valueRange = 2f..6f,
                steps = 1
            )
        }
    }
}
