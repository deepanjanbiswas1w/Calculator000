package com.example.premiumcalculator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import com.example.premiumcalculator.dataStore

private val THEME_KEY = stringPreferencesKey("theme")
private val HAPTIC_KEY = booleanPreferencesKey("haptic")
private val PRECISION_KEY = intPreferencesKey("precision")
private val PRIMARY_COLOR_KEY = longPreferencesKey("primary_color")
private val SECONDARY_COLOR_KEY = longPreferencesKey("secondary_color")
private val GLASSMORPHISM_KEY = booleanPreferencesKey("glassmorphism")
private val BUTTON_ROUND_KEY = booleanPreferencesKey("button_round")
private val FONT_KEY = stringPreferencesKey("font")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme by context.dataStore.data.map { it[THEME_KEY] ?: "light" }.collectAsState("light")
    val haptic by context.dataStore.data.map { it[HAPTIC_KEY] ?: true }.collectAsState(true)
    val precision by context.dataStore.data.map { it[PRECISION_KEY] ?: 6 }.collectAsState(6)
    val primary by context.dataStore.data.map { it[PRIMARY_COLOR_KEY] ?: 0xFF6200EE }.collectAsState(0xFF6200EE)
    val secondary by context.dataStore.data.map { it[SECONDARY_COLOR_KEY] ?: 0xFF03DAC6 }.collectAsState(0xFF03DAC6)
    val glassmorphism by context.dataStore.data.map { it[GLASSMORPHISM_KEY] ?: false }.collectAsState(false)
    val buttonRound by context.dataStore.data.map { it[BUTTON_ROUND_KEY] ?: true }.collectAsState(true)
    val font by context.dataStore.data.map { it[FONT_KEY] ?: "sans" }.collectAsState("sans")

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
                RadioButton(selected = theme == "black", onClick = { scope.launch { context.dataStore.edit { it[THEME_KEY] = "black" } } })
                Text("AMOLED")
            }

            Text("Primary Color (Hex: AARRGGBB)", modifier = Modifier.padding(top = 16.dp))
            TextField(value = primary.toString(16), onValueChange = { newVal ->
                scope.launch { try { context.dataStore.edit { it[PRIMARY_COLOR_KEY] = newVal.toLong(16) } } catch(e:Exception){} }
            })

            Text("Secondary Color (Hex: AARRGGBB)", modifier = Modifier.padding(top = 8.dp))
            TextField(value = secondary.toString(16), onValueChange = { newVal ->
                scope.launch { try { context.dataStore.edit { it[SECONDARY_COLOR_KEY] = newVal.toLong(16) } } catch(e:Exception){} }
            })

            Text("Glassmorphism", modifier = Modifier.padding(top = 16.dp))
            Switch(checked = glassmorphism, onCheckedChange = { scope.launch { context.dataStore.edit { it[GLASSMORPHISM_KEY] = it } } })

            Text("Button Shape Round", modifier = Modifier.padding(top = 16.dp))
            Switch(checked = buttonRound, onCheckedChange = { scope.launch { context.dataStore.edit { it[BUTTON_ROUND_KEY] = it } } })

            Text("Font", modifier = Modifier.padding(top = 16.dp))
            Row {
                RadioButton(selected = font == "sans", onClick = { scope.launch { context.dataStore.edit { it[FONT_KEY] = "sans" } } })
                Text("Sans")
                RadioButton(selected = font == "serif", onClick = { scope.launch { context.dataStore.edit { it[FONT_KEY] = "serif" } } })
                Text("Serif")
                RadioButton(selected = font == "monospace", onClick = { scope.launch { context.dataStore.edit { it[FONT_KEY] = "monospace" } } })
                Text("Mono")
            }

            Text("Haptic Feedback", modifier = Modifier.padding(top = 16.dp))
            Switch(checked = haptic, onCheckedChange = { scope.launch { context.dataStore.edit { it[HAPTIC_KEY] = it } } })

            Text("Decimal Places", modifier = Modifier.padding(top = 16.dp))
            Row {
                RadioButton(selected = precision == 2, onClick = { scope.launch { context.dataStore.edit { it[PRECISION_KEY] = 2 } } })
                Text("2")
                RadioButton(selected = precision == 4, onClick = { scope.launch { context.dataStore.edit { it[PRECISION_KEY] = 4 } } })
                Text("4")
                RadioButton(selected = precision == 6, onClick = { scope.launch { context.dataStore.edit { it[PRECISION_KEY] = 6 } } })
                Text("6")
            }
        }
    }
}
