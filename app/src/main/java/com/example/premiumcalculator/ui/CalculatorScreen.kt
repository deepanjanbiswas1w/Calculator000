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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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

data class ButtonData(val text: String)

private val basicButtons = listOf(
    ButtonData("C"), ButtonData("DEL"), ButtonData("%"), ButtonData("÷"),
    ButtonData("7"), ButtonData("8"), ButtonData("9"), ButtonData("×"),
    ButtonData("4"), ButtonData("5"), ButtonData("6"), ButtonData("−"),
    ButtonData("1"), ButtonData("2"), ButtonData("3"), ButtonData("+"),
    ButtonData("."), ButtonData("0"), ButtonData("="), ButtonData("")
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
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Pro Tools",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
                ) {
                    items(fullProToolsList) { tool ->
                        ProToolCard(tool, navController) {
                            coroutineScope.launch {
                                sheetState.hide()
                                showFeatureSheet = false
                            }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = preview.ifEmpty { "" },
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().weight(1.5f)
            ) {
                items(basicButtons) { button ->
                    if (button.text.isNotEmpty()) {
                        CalcButtonUI(
                            text = button.text,
                            onClick = {
                                if (hapticEnabled) {
                                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                                }
                                viewModel.onButtonClick(button.text)
                            }
                        )
                    }
                }
            }
        }
    }
}

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

data class ProToolData(val icon: ImageVector, val title: String, val subtitle: String, val route: String)

@Composable
private fun ProToolCard(tool: ProToolData, navController: NavController, onDismiss: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f)

    Card(
        onClick = {
            navController.navigate(tool.route)
            onDismiss()
        },
        modifier = Modifier.aspectRatio(1f).scale(scale).shadow(4.dp, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(tool.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            Text(tool.subtitle, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
        }
    }
}

@Composable
private fun CalcButtonUI(text: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f)
    val isOperator = text in listOf("÷", "×", "−", "+", "=")

    Button(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f).scale(scale).pointerInput(Unit) {
            detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false })
        },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOperator) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isOperator) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = text, fontSize = 26.sp, fontWeight = FontWeight.Medium)
    }
}
