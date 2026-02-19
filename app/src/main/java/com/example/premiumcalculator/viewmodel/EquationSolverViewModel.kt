package com.example.premiumcalculator.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class EquationSolverViewModel @Inject constructor() : ViewModel() {

    private val _result = mutableStateOf("")
    val result: State<String> = _result

    fun solve(equation: String) {
        try {
            val cleaned = equation.replace(" ", "").replace("=0", "")
            if (cleaned.contains("x^2")) {
                // Quadratic: ax^2 + bx + c
                val parts = cleaned.split(Regex("(?=[-+])"))
                var a = 0.0
                var b = 0.0
                var c = 0.0
                parts.forEach {
                    val part = it.trim()
                    if (part.isEmpty()) return@forEach
                    when {
                        part.contains("x^2") -> {
                            val coeff = part.replace("x^2", "")
                            a = when {
                                coeff.isEmpty() || coeff == "+" -> 1.0
                                coeff == "-" -> -1.0
                                else -> coeff.toDouble()
                            }
                        }
                        part.contains("x") -> {
                            val coeff = part.replace("x", "")
                            b = when {
                                coeff.isEmpty() || coeff == "+" -> 1.0
                                coeff == "-" -> -1.0
                                else -> coeff.toDouble()
                            }
                        }
                        else -> c = part.toDouble()
                    }
                }
                val disc = b * b - 4 * a * c
                if (disc < 0) _result.value = "No real roots"
                else {
                    val r1 = (-b + sqrt(disc)) / (2 * a)
                    val r2 = (-b - sqrt(disc)) / (2 * a)
                    _result.value = "Roots: $r1, $r2"
                }
            } else {
                // Linear: ax + b
                val parts = cleaned.split(Regex("(?=[-+])"))
                var a = 0.0
                var b = 0.0
                parts.forEach {
                    val part = it.trim()
                    if (part.isEmpty()) return@forEach
                    when {
                        part.contains("x") -> {
                            val coeff = part.replace("x", "")
                            a = when {
                                coeff.isEmpty() || coeff == "+" -> 1.0
                                coeff == "-" -> -1.0
                                else -> coeff.toDouble()
                            }
                        }
                        else -> b = part.toDouble()
                    }
                }
                if (a == 0.0) _result.value = "Not a valid linear equation"
                else _result.value = "x = ${-b / a}"
            }
        } catch (e: Exception) {
            _result.value = "Error: Check formatting"
        }
    }
}
