package com.example.premiumcalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premiumcalculator.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.util.ArrayDeque

private val PRECISION_KEY = intPreferencesKey("precision")
private val operatorsSet = setOf("+", "-", "*", "/", "÷", "×", "−")

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _preview = MutableStateFlow("")
    val preview: StateFlow<String> = _preview.asStateFlow()

    private val precision: StateFlow<Int> = dataStore.data
        .map { it[PRECISION_KEY] ?: 6 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)

    fun onButtonClick(text: String) {
        viewModelScope.launch {
            when (text) {
                "=" -> {
                    val currentExpr = _expression.value
                    if (currentExpr.isNotEmpty()) {
                        launch(Dispatchers.Default) {
                            try {
                                val result = evaluateExpression(currentExpr)
                                val formatted = formatResult(result)
                                _preview.update { formatted }
                                repository.insert(currentExpr, formatted, "")
                                _expression.update { formatted }
                            } catch (e: Exception) {
                                _preview.update { if (e.message?.contains("zero") == true) "Cannot divide by zero" else "Error" }
                            }
                        }
                    }
                }
                "C" -> {
                    _expression.update { "" }
                    _preview.update { "" }
                }
                "DEL" -> {
                    _expression.update { if (it.isNotEmpty()) it.dropLast(1) else "" }
                    updatePreviewInBackground()
                }
                else -> {
                    _expression.update { current ->
                        if (text in operatorsSet && current.isNotEmpty() && current.last().toString() in operatorsSet) {
                            current.dropLast(1) + text
                        } else {
                            current + text
                        }
                    }
                    updatePreviewInBackground()
                }
            }
        }
    }

    private fun updatePreviewInBackground() {
        viewModelScope.launch(Dispatchers.Default) {
            val currentExpr = _expression.value
            if (currentExpr.isEmpty() || currentExpr.last().toString() in operatorsSet) {
                _preview.update { "" }
                return@launch
            }
            _preview.update {
                try {
                    formatResult(evaluateExpression(currentExpr))
                } catch (_: Exception) {
                    ""
                }
            }
        }
    }

    private fun formatResult(result: BigDecimal): String {
        return result.setScale(precision.value, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    }

    private fun evaluateExpression(expr: String): BigDecimal {
        // UI চিহ্নের পরিবর্তে ম্যাথ অপারেটর বসানো হচ্ছে
        val normalizedExpr = expr.replace("×", "*")
                                .replace("÷", "/")
                                .replace("−", "-")
                                .replace(" ", "")
        
        val tokens = mutableListOf<String>()
        var currentNumber = ""
        
        for (char in normalizedExpr) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber)
                    currentNumber = ""
                }
                tokens.add(char.toString())
            }
        }
        if (currentNumber.isNotEmpty()) tokens.add(currentNumber)

        val output = ArrayDeque<BigDecimal>()
        val ops = ArrayDeque<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)

        for (token in tokens) {
            when {
                token.toBigDecimalOrNull() != null -> output.addLast(token.toBigDecimal())
                precedence.containsKey(token) -> {
                    while (ops.isNotEmpty() && precedence.getOrDefault(ops.last(), 0) >= precedence[token]!!) {
                        applyOp(output, ops.removeLast())
                    }
                    ops.addLast(token)
                }
                else -> throw IllegalArgumentException("Invalid token: $token")
            }
        }

        while (ops.isNotEmpty()) applyOp(output, ops.removeLast())
        return output.first()
    }

    private fun applyOp(output: ArrayDeque<BigDecimal>, op: String) {
        if (output.size < 2) return
        val b = output.removeLast()
        val a = output.removeLast()
        val res = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b == BigDecimal.ZERO) throw ArithmeticException("Division by zero") 
                  else a.divide(b, MathContext.DECIMAL128)
            else -> BigDecimal.ZERO
        }
        output.addLast(res)
    }
}
