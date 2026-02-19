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

// Fixed naming conflict: renamed data class to KeypadButtonData
data class KeypadButtonData(
    val text: String,
    val isOperator: Boolean = false,
    val special: Boolean = false
)

private val fullProToolsList = listOf(
    ProTool(Icons.Default.HealthAndSafety, "BMI/Health", "Track health", "bmi"),
    ProTool(Icons.Default.AttachMoney, "Investment", "Compound interest", "investment"),
    ProTool(Icons.Default.LocalGasStation, "Fuel Cost", "Trip optimizer", "fuel"),
    ProTool(Icons.Default.CompareArrows, "Unit Price", "Deal finder", "unit_price"),
    ProTool(Icons.Default.School, "GPA/CGPA", "Grade calc", "gpa"),
    ProTool(Icons.Default.CurrencyExchange, "Currency", "Live rates", "currency"),
    ProTool(Icons.Default.Cake, "Age Calculator", "Precise age", "age"),
    ProTool(Icons.Default.Calculate, "EMI Calculator", "Loan planner", "emi"),
    ProTool(Icons.Default.Functions, "Equation Solver", "Math problems", "solver")
)

private val keypadButtons = listOf(
    KeypadButtonData("C", isOperator = true, special = true),
    KeypadButtonData("⌫", isOperator = true, special = true),
    KeypadButtonData("%", isOperator = true),
    KeypadButtonData("÷", isOperator = true),
    KeypadButtonData("7"), KeypadButtonData("8"), KeypadButtonData("9"), 
    KeypadButtonData("×", isOperator = true),
    KeypadButtonData("4"), KeypadButtonData("5"), KeypadButtonData("6"), 
    KeypadButtonData("−", isOperator = true),
    KeypadButtonData("1"), KeypadButtonData("2"), KeypadButtonData("3"), 
    KeypadButtonData("+", isOperator = true),
    KeypadButtonData("."), KeypadButtonData("0"), KeypadButtonData("="), 
    KeypadButtonData("")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(navController: NavController) {
    val viewModel: CalculatorViewModel = hiltViewModel()
    val context = LocalContext.current
    val hapticEnabled by remember { mutableStateOf(true) }
    
    val expression by viewModel.expression.collectAsState()
    val preview by viewModel.preview.collectAsState()

    var showFeatureSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val coroutineScope = rememberCoroutineScope()

    if (showFeatureSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFeatureSheet = false },
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
                        coroutineScope.launch { sheetState.hide(); showFeatureSheet = false }
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                HorizontalDivider()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = fullProToolsList, key = { it.title }) { tool ->
                        ProToolCard(tool, navController) {
                            coroutineScope.launch { sheetState.hide(); showFeatureSheet = false }
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
                    IconButton(onClick = { showFeatureSheet = true }) {
                        Icon(Icons.Default.Widgets, "Features", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(expression.ifEmpty { "0" }, fontSize = 42.sp, fontWeight = FontWeight.Light, textAlign = TextAlign.End, maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Text(preview.ifEmpty { "" }, fontSize = 64.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1.8f)
            ) {
                items(items = keypadButtons, key = { it.text }) { button ->
                    if (button.text.isNotEmpty()) {
                        KeypadButtonUI(button) {
                            if (hapticEnabled) {
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                            }
                            viewModel.onButtonClick(button.text)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButtonUI(button: KeypadButtonData, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "scale")

    Button(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f).graphicsLayer { scaleX = scale; scaleY = scale }.pointerInput(Unit) {
            detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false })
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (button.special) MaterialTheme.colorScheme.errorContainer 
                            else if (button.isOperator) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (button.special) MaterialTheme.colorScheme.onErrorContainer 
                           else if (button.isOperator) MaterialTheme.colorScheme.onPrimary 
                           else MaterialTheme.colorScheme.onSurface
        )
    ) {
        if (button.text == "⌫") {
            Icon(Icons.AutoMirrored.Filled.Backspace, "Delete", modifier = Modifier.size(28.dp))
        } else {
            Text(button.text, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProToolCard(tool: ProTool, navController: NavController, onDismiss: () -> Unit) {
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
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(tool.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Text(tool.subtitle, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
        }
    }
}

data class ProTool(val icon: ImageVector, val title: String, val subtitle: String, val route: String)
