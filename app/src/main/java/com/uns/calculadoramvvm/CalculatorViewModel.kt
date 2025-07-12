package com.uns.calculadoramvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.DecimalFormat
import kotlin.math.*

class CalculatorViewModel : ViewModel() {

    // UI State
    private val _display = MutableLiveData<String>()
    val display: LiveData<String> = _display

    private val _expression = MutableLiveData<String>()
    val expression: LiveData<String> = _expression

    private val _history = MutableStateFlow<List<CalculationHistory>>(emptyList())
    val history: StateFlow<List<CalculationHistory>> = _history

    private val _isScientificMode = MutableLiveData<Boolean>()
    val isScientificMode: LiveData<Boolean> = _isScientificMode

    private val _isDegreeMode = MutableLiveData<Boolean>()
    val isDegreeMode: LiveData<Boolean> = _isDegreeMode

    private val _memory = MutableLiveData<Double>()
    val memory: LiveData<Double> = _memory

    private val _hasMemory = MutableLiveData<Boolean>()
    val hasMemory: LiveData<Boolean> = _hasMemory

    // Internal state
    private var currentInput = ""
    private var currentExpression = ""
    private var lastResult = 0.0
    private var isNewCalculation = true
    private var memoryValue = 0.0
    private var pendingOperator = ""
    private var operand1 = 0.0
    private var waitingForOperand = false

    private val decimalFormat = DecimalFormat("#.##########")
    private val scientificFormat = DecimalFormat("0.######E0")

    init {
        _display.value = "0"
        _expression.value = ""
        _isScientificMode.value = false
        _isDegreeMode.value = true
        _memory.value = 0.0
        _hasMemory.value = false
    }

    // Basic number input
    fun onNumberClick(number: String) {
        if (waitingForOperand) {
            currentInput = number
            waitingForOperand = false
        } else {
            currentInput = if (currentInput == "0") number else currentInput + number
        }
        updateDisplay()
    }

    // Basic operators
    fun onOperatorClick(operator: String) {
        val inputValue = currentInput.toDoubleOrNull() ?: 0.0

        if (pendingOperator.isNotEmpty() && !waitingForOperand) {
            calculate()
        } else {
            operand1 = inputValue
        }

        pendingOperator = operator
        waitingForOperand = true
        currentExpression = "${formatNumber(operand1)} $operator"
        _expression.value = currentExpression
    }

    // Scientific functions
    fun onScientificFunction(function: String) {
        val value = currentInput.toDoubleOrNull() ?: 0.0

        val result = when (function) {
            "sin" -> if (_isDegreeMode.value == true) sin(Math.toRadians(value)) else sin(value)
            "cos" -> if (_isDegreeMode.value == true) cos(Math.toRadians(value)) else cos(value)
            "tan" -> if (_isDegreeMode.value == true) tan(Math.toRadians(value)) else tan(value)
            "log" -> log10(value)
            "ln" -> ln(value)
            "sqrt" -> sqrt(value)
            "x²" -> value * value
            "x³" -> value * value * value
            "1/x" -> if (value != 0.0) 1.0 / value else Double.NaN
            "x!" -> factorial(value.toInt())
            "π" -> PI
            "e" -> E
            else -> value
        }

        if (result.isNaN() || result.isInfinite()) {
            _display.value = "Error"
            currentInput = "0"
        } else {
            currentInput = result.toString()
            lastResult = result
            addToHistory("$function($value)", result)
            updateDisplay()
        }
    }

    // Power function
    fun onPowerClick() {
        onOperatorClick("^")
    }

    // Plus/Minus toggle
    fun onPlusMinusClick() {
        val value = currentInput.toDoubleOrNull() ?: 0.0
        currentInput = (-value).toString()
        updateDisplay()
    }

    // Equals calculation
    fun onEqualsClick() {
        if (pendingOperator.isNotEmpty() && !waitingForOperand) {
            calculate()
            addToHistory(currentExpression, lastResult)
            pendingOperator = ""
            waitingForOperand = true
            currentExpression = ""
            _expression.value = ""
        }
    }

    // Decimal point
    fun onDecimalClick() {
        if (waitingForOperand) {
            currentInput = "0."
            waitingForOperand = false
        } else if (!currentInput.contains(".")) {
            currentInput += "."
        }
        updateDisplay()
    }

    // Clear functions
    fun onClearClick() {
        currentInput = "0"
        currentExpression = ""
        lastResult = 0.0
        pendingOperator = ""
        operand1 = 0.0
        waitingForOperand = false
        _display.value = "0"
        _expression.value = ""
    }

    fun onClearEntryClick() {
        currentInput = "0"
        updateDisplay()
    }

    fun onDeleteClick() {
        if (currentInput.length > 1) {
            currentInput = currentInput.dropLast(1)
        } else {
            currentInput = "0"
        }
        updateDisplay()
    }

    // Memory functions
    fun onMemoryStore() {
        val value = currentInput.toDoubleOrNull() ?: 0.0
        memoryValue = value
        _memory.value = value
        _hasMemory.value = true
    }

    fun onMemoryRecall() {
        if (_hasMemory.value == true) {
            currentInput = memoryValue.toString()
            updateDisplay()
        }
    }

    fun onMemoryAdd() {
        val value = currentInput.toDoubleOrNull() ?: 0.0
        memoryValue += value
        _memory.value = memoryValue
        _hasMemory.value = true
    }

    fun onMemorySubtract() {
        val value = currentInput.toDoubleOrNull() ?: 0.0
        memoryValue -= value
        _memory.value = memoryValue
        _hasMemory.value = true
    }

    fun onMemoryClear() {
        memoryValue = 0.0
        _memory.value = 0.0
        _hasMemory.value = false
    }

    // Mode toggles
    fun toggleScientificMode() {
        _isScientificMode.value = !(_isScientificMode.value ?: false)
    }

    fun toggleDegreeMode() {
        _isDegreeMode.value = !(_isDegreeMode.value ?: true)
    }

    // History management
    fun clearHistory() {
        _history.value = emptyList()
    }

    fun useHistoryItem(item: CalculationHistory) {
        currentInput = item.result.toString()
        lastResult = item.result
        waitingForOperand = false
        updateDisplay()
    }

    // Private helper functions
    private fun updateDisplay() {
        val value = currentInput.toDoubleOrNull() ?: 0.0
        _display.value = formatNumber(value)
    }

    private fun calculate() {
        val inputValue = currentInput.toDoubleOrNull() ?: 0.0

        val result = when (pendingOperator) {
            "+" -> operand1 + inputValue
            "-" -> operand1 - inputValue
            "×" -> operand1 * inputValue
            "÷" -> if (inputValue != 0.0) operand1 / inputValue else Double.NaN
            "^" -> operand1.pow(inputValue)
            else -> inputValue
        }

        if (result.isNaN() || result.isInfinite()) {
            _display.value = "Error"
            currentInput = "0"
        } else {
            currentInput = result.toString()
            lastResult = result
            operand1 = result
            updateDisplay()
        }
    }

    private fun formatNumber(number: Double): String {
        return when {
            number.isNaN() -> "Error"
            number.isInfinite() -> "∞"
            abs(number) >= 1e10 || (abs(number) < 1e-6 && number != 0.0) -> scientificFormat.format(number)
            number == number.toLong().toDouble() -> number.toLong().toString()
            else -> decimalFormat.format(number)
        }
    }

    private fun factorial(n: Int): Double {
        if (n < 0) return Double.NaN
        if (n > 170) return Double.POSITIVE_INFINITY
        var result = 1.0
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    private fun addToHistory(expression: String, result: Double) {
        val historyItem = CalculationHistory(
            expression = expression,
            result = result,
            timestamp = System.currentTimeMillis()
        )
        val currentHistory = _history.value.toMutableList()
        currentHistory.add(0, historyItem)
        if (currentHistory.size > 50) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        _history.value = currentHistory
    }
}

data class CalculationHistory(
    val expression: String,
    val result: Double,
    val timestamp: Long
)