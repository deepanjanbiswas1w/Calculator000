package com.example.premiumcalculator

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

private val THEME_KEY = stringPreferencesKey("theme")
private val PRIMARY_COLOR_KEY = longPreferencesKey("primary_color")
private val SECONDARY_COLOR_KEY = longPreferencesKey("secondary_color")
private val GLASSMORPHISM_KEY = booleanPreferencesKey("glassmorphism")
private val FONT_KEY = stringPreferencesKey("font")

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dataStore: DataStore<Preferences> = remember { context.dataStore }
    val theme = dataStore.data.map { it[THEME_KEY] ?: "light" }.collectAsState(initial = "light").value
    val primary = Color(dataStore.data.map { it[PRIMARY_COLOR_KEY] ?: 0xFF6200EE }.collectAsState(initial = 0xFF6200EE).value)
    val secondary = Color(dataStore.data.map { it[SECONDARY_COLOR_KEY] ?: 0xFF03DAC6 }.collectAsState(initial = 0xFF03DAC6).value)
    val glassmorphism = dataStore.data.map { it[GLASSMORPHISM_KEY] ?: false }.collectAsState(initial = false).value
    val fontStr = dataStore.data.map { it[FONT_KEY] ?: "sans" }.collectAsState(initial = "sans").value
    val fontFamily = when (fontStr) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }

    val colorScheme = when (theme) {
        "dark" -> dynamicDarkColorScheme(context).copy(primary = primary, secondary = secondary)
        "black" -> dynamicDarkColorScheme(context).copy(primary = primary, secondary = secondary, background = Color.Black, surface = Color.Black)
        else -> dynamicLightColorScheme(context).copy(primary = primary, secondary = secondary)
    }

    // Glassmorphism global (applied in screens as needed)
    val typography = MaterialTheme.typography.copy(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontFamily = fontFamily),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

private val Context.dataStore: DataStore<Preferences>
    get() = (applicationContext as App).dataStore
