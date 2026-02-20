package com.example.premiumcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.compose.rememberNavController
import com.example.premiumcalculator.ui.theme.AppTheme
import com.example.premiumcalculator.ui.dataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

private val THEME_KEY = stringPreferencesKey("theme")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themeMode by context.dataStore.data
                .map { it[THEME_KEY] ?: "system" }
                .collectAsState(initial = "system")

            val isDarkTheme = when (themeMode.lowercase()) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            AppTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = rememberNavController())
                }
            }
        }
    }
}
