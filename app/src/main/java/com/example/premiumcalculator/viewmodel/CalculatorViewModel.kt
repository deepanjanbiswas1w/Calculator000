package com.example.premiumcalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premiumcalculator.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

private val PRECISION_KEY = intPreferencesKey("precision")
private val operatorsSet = setOf("+", "-", "*", "/", "÷", "×", "−", "^", "%")

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

    fun onButtonClick(input: String) {
        viewModelScope.launch {
            val current = _expression.value

            when (input) {
                "⌫", "DEL" -> {
                    if (current.isNotEmpty()) {
                        _expression.update { it.dropLast(1) }
                        updatePreviewInBackground()
                    }
                }
                "C" -> {
                    _expression.update { "" }
                    _preview.update { "" }
                }
                "." -> {
                    val lastNumber = current.split(Regex("[+\\-×÷^%]")).lastOrNull() ?: ""
                    if (!lastNumber.contains(".")) {
                        _expression.update { it + "." }
                    }
                }
                "=" -> {
                    if (current.isNotEmpty() && current.last().toString() !in operatorsSet) {
                        try {
                            val result = evaluateExpression(current)
                            val formatted = formatResult(result)
                            repository.insert(current, formatted, "")
                            _expression.update { formatted }
                            _preview.update { "" }
                        } catch (e: Exception) {
                            _preview.update { "Error" }
                        }
                    }
                }
                else -> {
                    if (input in operatorsSet) {
                        if (current.isEmpty()) return@launch
                        if (current.last().toString() in operatorsSet) {
                            _expression.update { it.dropLast(1) + input }
                        } else {
                            _expression.update { it + input }
                        }
                    } else {
                        _expression.update { it + input }
                    }
                    updatePreviewInBackground()
                }
            }
        }
    }

    private fun updatePreviewInBackground() {
        viewModelScope.launch(Dispatchers.Default) {
            val expr = _expression.value
            if (expr.isEmpty() || expr.last().toString() in operatorsSet) {
                _preview.update { "" }
                return@launch
            }
            try { _preview.update { formatResult(evaluateExpression(expr)) } } catch (_: Exception) { }
        }
    }

    private fun formatResult(result: BigDecimal): String {
        return result.setScale(precision.value, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    private fun evaluateExpression(expr: String): BigDecimal {
        val normalized = expr.replace("×", "*").replace("÷", "/").replace("−", "-")
        val tokens = mutableListOf<String>()
        var currentNumber = ""

        for (char in normalized) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else {
                if (currentNumber.isNotEmpty()) tokens.add(currentNumber)
                tokens.add(char.toString())
                currentNumber = ""
            }
        }
        if (currentNumber.isNotEmpty()) tokens.add(currentNumber)

        val output = java.util.ArrayDeque<BigDecimal>()
        val ops = java.util.ArrayDeque<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2, "^" to 3)

        for (token in tokens) {
            when {
                token.toBigDecimalOrNull() != null -> output.addLast(token.toBigDecimal())
                token == "%" -> {
                    if (output.isNotEmpty()) {
                        val v = output.removeLast()
                        output.addLast(v.divide(BigDecimal("100"), java.math.MathContext.DECIMAL128))
                    }
                }
                precedence.containsKey(token) -> {
                    while (ops.isNotEmpty() && precedence.getOrDefault(ops.last(), 0) >= precedence[token]!!) {
                        applyOp(output, ops.removeLast())
                    }
                    ops.addLast(token)
                }
            }
        }
        while (ops.isNotEmpty()) applyOp(output, ops.removeLast())
        return if (output.isEmpty()) BigDecimal.ZERO else output.first()
    }

    private fun applyOp(output: java.util.ArrayDeque<BigDecimal>, op: String) {
        if (output.size < 2) return
        val b = output.removeLast()
        val a = output.removeLast()
        val res = when (op) {
            "+" -> a.add(b)
            "-" -> a.subtract(b)
            "*" -> a.multiply(b)
            "/" -> if (b.compareTo(BigDecimal.ZERO) == 0) throw java.lang.ArithmeticException("Div zero") else a.divide(b, java.math.MathContext.DECIMAL128)
            "^" -> BigDecimal(Math.pow(a.toDouble(), b.toDouble()))
            else -> BigDecimal.ZERO
        }
        output.addLast(res)
    }
}
