package com.example.premiumcalculator.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.premiumcalculator.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

data class KeypadButtonData(
    val display: String,
    val command: String,
    val isOperator: Boolean = false,
    val special: Boolean = false,
    val useIcon: Boolean = false
)

data class ProToolData(val icon: ImageVector, val title: String, val subtitle: String, val route: String)

private val fullProToolsList = listOf(
    ProToolData(Icons.Default.HealthAndSafety, "BMI/Health", "Track health", "bmi"),
    ProToolData(Icons.Default.AttachMoney, "Investment", "Compound interest", "investment"),
    ProToolData(Icons.Default.LocalGasStation, "Fuel Cost", "Trip optimizer", "fuel"),
    ProToolData(Icons.Default.CompareArrows, "Unit Price", "Deal finder", "unit_price"),
    ProToolData(Icons.Default.School, "GPA/CGPA", "Grade calc", "gpa"),
    ProToolData(Icons.Default.CurrencyExchange, "Currency", "Live rates", "currency"),
    ProToolData(Icons.Default.Cake, "Age Calculator", "Precise age", "age"),
    ProToolData(Icons.Default.Calculate, "EMI Calculator", "Loan planner", "emi"),
    ProToolData(Icons.Default.Functions, "Equation Solver", "Math problems", "solver")
)

private val basicKeypadButtons = listOf(
    KeypadButtonData("C", "C", isOperator = true, special = true),
    KeypadButtonData("⌫", "⌫", isOperator = true, special = true, useIcon = true),
    KeypadButtonData("%", "%", isOperator = true),
    KeypadButtonData("÷", "÷", isOperator = true),
    KeypadButtonData("7", "7"), KeypadButtonData("8", "8"), KeypadButtonData("9", "9"), 
    KeypadButtonData("×", "×", isOperator = true),
    KeypadButtonData("4", "4"), KeypadButtonData("5", "5"), KeypadButtonData("6", "6"), 
    KeypadButtonData("−", "−", isOperator = true),
    KeypadButtonData("1", "1"), KeypadButtonData("2", "2"), KeypadButtonData("3", "3"), 
    KeypadButtonData("+", "+", isOperator = true),
    KeypadButtonData(".", "."), KeypadButtonData("0", "0"), KeypadButtonData("=", "=", isOperator = true), 
    KeypadButtonData("", "")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(navController: NavController) {
    val viewModel: CalculatorViewModel = hiltViewModel()
    val context = LocalContext.current
    val hapticEnabled by remember { mutableStateOf(true) }
    
    val expression by viewModel.expression.collectAsState()
    val preview by viewModel.preview.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    val keypadButtons = remember { basicKeypadButtons }
    val proToolsList = remember { fullProToolsList }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pro Tools", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        scope.launch { sheetState.hide(); showSheet = false }
                    }) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                HorizontalDivider()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = proToolsList, key = { it.title }) { tool ->
                        ProToolCard(tool, navController) {
                            scope.launch { sheetState.hide(); showSheet = false }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pro Calculator", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.Widgets, "Features", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(expression.ifEmpty { "0" }, fontSize = 44.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.End, maxLines = 2)
                Spacer(Modifier.height(12.dp))
                Text(preview.ifEmpty { "" }, fontSize = 68.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(32.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().weight(1.8f)
            ) {
                items(items = keypadButtons, key = { it.display + it.command }) { btn ->
                    if (btn.display.isNotEmpty()) {
                        KeypadButtonUI(btn) {
                            if (hapticEnabled) {
                                (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
                                    ?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                            }
                            viewModel.onButtonClick(btn.command)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButtonUI(btn: KeypadButtonData, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "scale")

    Button(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f).graphicsLayer { scaleX = scale; scaleY = scale }.pointerInput(Unit) {
            detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false })
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (btn.special) MaterialTheme.colorScheme.errorContainer 
                            else if (btn.isOperator) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (btn.special) MaterialTheme.colorScheme.onErrorContainer 
                           else if (btn.isOperator) MaterialTheme.colorScheme.onPrimary 
                           else MaterialTheme.colorScheme.onSurface
        )
    ) {
        if (btn.useIcon) {
            Icon(Icons.AutoMirrored.Filled.Backspace, "Delete", modifier = Modifier.size(30.dp))
        } else {
            Text(btn.display, fontSize = 30.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProToolCard(tool: ProToolData, navController: NavController, onDismiss: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "scale")

    Card(
        onClick = { navController.navigate(tool.route); onDismiss() },
        modifier = Modifier.aspectRatio(1f).graphicsLayer { scaleX = scale; scaleY = scale }.pointerInput(Unit) {
            detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false })
        }.shadow(4.dp, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(tool.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(tool.subtitle, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}
