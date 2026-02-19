package com.example.premiumcalculator.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premiumcalculator.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.util.ArrayDeque
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private val PRECISION_KEY = intPreferencesKey("precision")

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: HistoryRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _expression = mutableStateOf("")
    val expression: State<String> = _expression

    private val _preview = mutableStateOf("")
    val preview: State<String> = _preview

    private val precision: State<Int> = dataStore.data
        .map { it[PRECISION_KEY] ?: 6 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)

    fun onButtonClick(text: String) {
        when (text) {
            "=" -> {
                try {
                    val result = evaluateExpression(_expression.value)
                    val formatted = formatResult(result)
                    _preview.value = formatted
                    viewModelScope.launch {
                        repository.insert(_expression.value, formatted, "") 
                    }
                    _expression.value = formatted
                } catch (e: Exception) {
                    _preview.value = e.message ?: "Error"
                }
            }
            "C" -> {
                _expression.value = ""
                _preview.value = ""
            }
            "DEL" -> {
                if (_expression.value.isNotEmpty()) {
                    _expression.value = _expression.value.dropLast(1)
                    updatePreview()
                }
            }
            else -> {
                _expression.value += when (text) {
                    "sin", "cos", "tan", "log", "ln", "√" -> "$text("
                    else -> text
                }
                updatePreview()
            }
        }
    }

    private fun updatePreview() {
        _preview.value = try {
            formatResult(evaluateExpression(_expression.value))
        } catch (_: Exception) {
            ""
        }
    }

    private fun formatResult(result: BigDecimal): String {
        return try {
            result.setScale(precision.value, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        } catch (e: Exception) {
            result.toPlainString()
        }
    }

    private fun evaluateExpression(expr: String): BigDecimal {
        if (expr.isEmpty()) return BigDecimal.ZERO
        val expression = expr.replace(" ", "")
        val tokens = tokenize(expression)

        val output = ArrayDeque<BigDecimal>()
        val operators = ArrayDeque<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2, "^" to 3)

        for (token in tokens) {
            when {
                token.toBigDecimalOrNull() != null -> output.addLast(token.toBigDecimal())
                token in listOf("π", "e") -> output.addLast(if (token == "π") BigDecimal(Math.PI) else BigDecimal(Math.E))
                token == "(" -> operators.addLast(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.last() != "(") {
                        applyOperator(output, operators.removeLast())
                    }
                    if (operators.isNotEmpty()) operators.removeLast()
                    if (operators.isNotEmpty() && operators.last() in listOf("sin", "cos", "tan", "log", "ln", "√")) {
                        applyUnary(output, operators.removeLast())
                    }
                }
                token in listOf("sin", "cos", "tan", "log", "ln", "√", "!") -> {
                    if (token == "!") applyUnary(output, token) else operators.addLast(token)
                }
                precedence.containsKey(token) -> {
                    while (operators.isNotEmpty() && operators.last() != "(" && (precedence[operators.last()] ?: 0) >= precedence[token]!!) {
                        applyOperator(output, operators.removeLast())
                    }
                    operators.addLast(token)
                }
            }
        }

        while (operators.isNotEmpty()) {
            applyOperator(output, operators.removeLast())
        }

        return output.firstOrNull() ?: BigDecimal.ZERO
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var current = ""
        var i = 0
        while (i < expr.length) {
            val char = expr[i]
            if (char.isDigit() || char == '.') {
                current += char
            } else {
                if (current.isNotEmpty()) {
                    tokens.add(current)
                    current = ""
                }
                if (char in "+-*/^()%!πe√") {
                    tokens.add(char.toString())
                } else if (i + 2 < expr.length) {
                    val tri = expr.substring(i, i + 3)
                    if (tri in listOf("sin", "cos", "tan", "log")) {
                        tokens.add(tri)
                        i += 2
                    } else if (expr.substring(i, i + 2) == "ln") {
                        tokens.add("ln")
                        i += 1
                    }
                } else if (i + 1 < expr.length && expr.substring(i, i + 2) == "ln") {
                    tokens.add("ln")
                    i += 1
                }
            }
            i++
        }
        if (current.isNotEmpty()) tokens.add(current)
        return tokens
    }

    private fun applyOperator(output: ArrayDeque<BigDecimal>, op: String) {
        if (output.size < 2) return
        val b = output.removeLast()
        val a = output.removeLast()
        val res = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else a.divide(b, MathContext.DECIMAL128)
            "^" -> BigDecimal(Math.pow(a.toDouble(), b.toDouble()))
            else -> a
        }
        output.addLast(res)
    }

    private fun applyUnary(output: ArrayDeque<BigDecimal>, op: String) {
        if (output.isEmpty()) return
        val a = output.removeLast()
        val d = a.toDouble()
        val res = when (op) {
            "sin" -> BigDecimal(sin(d))
            "cos" -> BigDecimal(cos(d))
            "tan" -> BigDecimal(tan(d))
            "log" -> BigDecimal(log10(d))
            "ln" -> BigDecimal(ln(d))
            "√" -> BigDecimal(sqrt(d))
            "!" -> factorial(a)
            else -> a
        }
        output.addLast(res)
    }

    private fun factorial(n: BigDecimal): BigDecimal {
        val num = n.toInt()
        if (num < 0) return BigDecimal.ZERO
        var res = BigDecimal.ONE
        for (j in 2..num) res = res.multiply(BigDecimal(j))
        return res
    }
}
