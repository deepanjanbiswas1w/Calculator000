package com.example.premiumcalculator.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.premiumcalculator.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(navController: NavController) {
    val viewModel: CalculatorViewModel = hiltViewModel()
    val context = LocalContext.current
    val expression by viewModel.expression.collectAsState()
    val preview by viewModel.preview.collectAsState()

    var showToolsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calculator") },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { showToolsSheet = true }) {
                        Icon(Icons.Default.Widgets, contentDescription = "Pro Tools")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = preview.ifEmpty { "0" },
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Keypad (তোর বিদ্যমান লজিক অনুযায়ী বাটনগুলো আসবে)
            Box(modifier = Modifier.weight(2f)) {
                // তোর Keypad গ্রিড এখানে বসবে
            }
        }
    }
}
