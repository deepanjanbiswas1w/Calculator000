package com.example.premiumcalculator

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.premiumcalculator.ui.AgeCalculatorScreen
import com.example.premiumcalculator.ui.CalculatorScreen
import com.example.premiumcalculator.ui.DiscountTaxScreen
import com.example.premiumcalculator.ui.EmiCalculatorScreen
import com.example.premiumcalculator.ui.EquationSolverScreen
import com.example.premiumcalculator.ui.HistoryScreen
import com.example.premiumcalculator.ui.LandConverterScreen
import com.example.premiumcalculator.ui.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val currentDestination = currentRoute?.destination?.route

    NavHost(navController = navController, startDestination = "calculator") {
        composable("calculator") { CalculatorScreen(navController) }
        composable("solver") { EquationSolverScreen(navController) }
        composable("age") { AgeCalculatorScreen(navController) }
        composable("land") { LandConverterScreen(navController) }
        composable("emi") { EmiCalculatorScreen(navController) }
        composable("discount") { DiscountTaxScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}
