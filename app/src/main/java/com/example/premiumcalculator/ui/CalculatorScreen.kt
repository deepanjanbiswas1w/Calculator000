package com.example.premiumcalculator.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(navController: NavController) {
    val viewModel: CalculatorViewModel = hiltViewModel()
    val context = LocalContext.current
    val expression by viewModel.expression.collectAsState()
    val preview by viewModel.preview.collectAsState()

    var showProTools by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showFactorialWarning by remember { mutableStateOf(false) }

    if (showProTools) {
        ModalBottomSheet(
            onDismissRequest = { showProTools = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pro Tools", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { scope.launch { sheetState.hide() }; showProTools = false }) { Icon(Icons.Default.Close, "Close") }
                }
                Divider()
                LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(proTools) { tool -> ProToolCard(tool, navController) { showProTools = false } }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calculator") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, "Settings") }
                    IconButton(onClick = { showProTools = true }) { Icon(Icons.Default.Widgets, "Pro Tools") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            // Display Area
            Column(modifier = Modifier.weight(2.6f).fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())
                    )
                    IconButton(onClick = { viewModel.onButtonClick("⌫") }) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, "Delete", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    text = preview.ifEmpty { "0" },
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                )
                if (showFactorialWarning) {
                    Text(text = "⚠️ Result may be extremely large (>20!)", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
                Spacer(Modifier.height(40.dp))
            }

            // Keypad
            LazyVerticalGrid(columns = GridCells.Fixed(4), contentPadding = PaddingValues(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(3.4f)) {
                items(keypadButtons) { btn ->
                    KeypadButton(btn) {
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        if (btn.command == "!") {
                            val lastNum = expression.split(Regex("[+\\-×÷]")).lastOrNull()?.toIntOrNull() ?: 0
                            if (lastNum > 20) {
                                showFactorialWarning = true
                                scope.launch { kotlinx.coroutines.delay(3000); showFactorialWarning = false }
                            }
                        }
                        viewModel.onButtonClick(btn.command)
                    }
                }
            }
        }
    }
}

private val keypadButtons = listOf(
    KeypadButton("%", "%"), KeypadButton("^", "^"), KeypadButton("√", "√"), KeypadButton("!", "!"),
    KeypadButton("7", "7"), KeypadButton("8", "8"), KeypadButton("9", "9"), KeypadButton("÷", "÷"),
    KeypadButton("4", "4"), KeypadButton("5", "5"), KeypadButton("6", "6"), KeypadButton("×", "×"),
    KeypadButton("1", "1"), KeypadButton("2", "2"), KeypadButton("3", "3"), KeypadButton("−", "−"),
    KeypadButton("0", "0"), KeypadButton(".", "."), KeypadButton("C", "C"), KeypadButton("=", "=")
)

data class KeypadButton(val display: String, val command: String)

@Composable
private fun KeypadButton(btn: KeypadButton, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f)
    val isOperator = btn.display in listOf("÷", "×", "−", "+", "=", "%", "^", "√", "!")
    Button(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f).graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }) },
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOperator) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isOperator) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(btn.display, fontSize = 28.sp, fontWeight = FontWeight.Medium)
    }
}

private val proTools = listOf(
    ProTool(Icons.Default.Cake, "Age Calculator", "age"),
    ProTool(Icons.Default.Calculate, "EMI Calculator", "emi"),
    ProTool(Icons.Default.School, "GPA/CGPA", "gpa"),
    ProTool(Icons.Default.CurrencyExchange, "Currency Converter", "currency"),
    ProTool(Icons.Default.LocalGasStation, "Fuel Cost", "fuel"),
    ProTool(Icons.Default.HealthAndSafety, "BMI/Health", "bmi"),
    ProTool(Icons.Default.CompareArrows, "Unit Price", "unit_price"),
    ProTool(Icons.Default.AttachMoney, "Investment", "investment"),
    ProTool(Icons.Default.ShoppingCart, "Discount/Tax", "discount"),
    ProTool(Icons.Default.Landscape, "Land Converter", "land"),
    ProTool(Icons.Default.Functions, "Equation Solver", "solver")
)

data class ProTool(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val route: String)

@Composable
private fun ProToolCard(tool: ProTool, navController: NavController, onDismiss: () -> Unit) {
    Card(
        onClick = { navController.navigate(tool.route); onDismiss() },
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(tool.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text(tool.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
