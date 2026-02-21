package com.example.premiumcalculator

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.premiumcalculator.ui.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "calculator") {
        composable("calculator") { CalculatorScreen(navController) }
        composable("bmi") { BmiHealthScreen(navController) }
        composable("investment") { InvestmentScreen(navController) }
        composable("fuel") { FuelCostScreen(navController) }
        composable("unit_price") { UnitPriceScreen(navController) }
        composable("gpa") { GpaCalculatorScreen(navController) }
        composable("currency") { CurrencyScreen(navController) }
        composable("age") { AgeCalculatorScreen(navController) }
        composable("emi") { EmiCalculatorScreen(navController) }
        composable("solver") { EquationSolverScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        
        // নতুন রুটগুলো এখানে যোগ করা হলো (ক্রাশ ফিক্স)
        composable("discount") { DiscountTaxScreen(navController) }
        composable("land") { LandConverterScreen(navController) }
    }
}
