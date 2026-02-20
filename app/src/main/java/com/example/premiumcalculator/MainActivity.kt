package com.example.premiumcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.premiumcalculator.ui.CalculatorScreen
import com.example.premiumcalculator.ui.SettingsScreen
import com.example.premiumcalculator.ui.AgeCalculatorScreen
import com.example.premiumcalculator.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "calculator") {
        composable("calculator") {
            CalculatorScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("age") {
            AgeCalculatorScreen(navController)
        }
        // ভবিষ্যতে BMI বা অন্যান্য ফিচারের রুট এখানে যোগ করা যাবে
    }
}
