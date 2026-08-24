package com.example.calculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.engine.CalculusEngine
import com.example.calculator.engine.MathEvaluator
import com.example.calculator.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.*

data class CalculatorUiState(
    val expression: String = "",
    val result: String = "",
    val exactFraction: Fraction? = null,
    val decimalVal: Double? = null,
    val angleMode: AngleMode = AngleMode.DEG,
    val calcMode: CalcMode = CalcMode.COMP,
    val baseNMode: BaseNMode = BaseNMode.DEC,
    val displayFormat: DisplayFormat = DisplayFormat.NATURAL_FRACTION,
    val isShift: Boolean = false,
    val isAlpha: Boolean = false,
    val isHyp: Boolean = false,
    val isSto: Boolean = false,
    val isRcl: Boolean = false,
    val hasMemory: Boolean = false,
    val memoryM: Double = 0.0,
    val ans: Double = 0.0,
    val variables: Map<Char, Double> = mapOf(
        'A' to 0.0, 'B' to 0.0, 'C' to 0.0, 'D' to 0.0, 'E' to 0.0, 'F' to 0.0,
        'X' to 0.0, 'Y' to 0.0, 'M' to 0.0
    ),
    val history: List<CalculationHistory> = emptyList(),
    val historyIndex: Int = -1,
    val showModeDialog: Boolean = false,
    val showSetupDialog: Boolean = false,
    val showHistoryDialog: Boolean = false,
    val showConstantsDialog: Boolean = false,
    val showConversionsDialog: Boolean = false
)

class CalculatorViewModel : ViewModel() {

    private val evaluator = MathEvaluator()

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    fun onKeyClick(key: String) {
        val state = _uiState.value
        val isShift = state.isShift
        val isAlpha = state.isAlpha
        val isHyp = state.isHyp

        // Toggle modifier keys directly
        if (key == "SHIFT") {
            _uiState.update { it.copy(isShift = !it.isShift, isAlpha = false) }
            return
        }
        if (key == "ALPHA") {
            _uiState.update { it.copy(isAlpha = !it.isAlpha, isShift = false) }
            return
        }

        // Consume one-shot modifier keys for all normal functional/typing keys
        if (state.isShift || state.isAlpha) {
            _uiState.update { it.copy(isShift = false, isAlpha = false) }
        }

        // If in STO or RCL mode and a variable key is pressed (A, B, C, D, E, F, X, Y, M)
        if (state.isSto && key in listOf("(-)", "° ' \"", "°'''", "hyp", "sin", "cos", "tan", ")", "S<=>D", "SD", "M+", "M_PLUS")) {
            val varChar = mapKeyToVar(key)
            if (varChar != null) {
                storeVariable(varChar)
                return
            }
        }
        if (state.isRcl && key in listOf("(-)", "° ' \"", "°'''", "hyp", "sin", "cos", "tan", ")", "S<=>D", "SD", "M+", "M_PLUS")) {
            val varChar = mapKeyToVar(key)
            if (varChar != null) {
                recallVariable(varChar)
                return
            }
        }

        when (key) {
            // TOP ROW CONTROLS
            "MODE" -> {
                if (isShift) {
                    _uiState.update { it.copy(showSetupDialog = true) }
                } else {
                    _uiState.update { it.copy(showModeDialog = true) }
                }
            }
            "ON" -> resetAll()

            // UPPER FUNCTION KEYS
            "CALC" -> {
                if (isAlpha) {
                    appendToken("=")
                } else if (isShift) {
                    // SOLVE
                    evaluateCurrent()
                } else {
                    evaluateCurrent()
                }
            }
            "INTEGRAL" -> {
                if (isAlpha) {
                    appendToken(":")
                } else if (isShift) {
                    // d/dx (derivative)
                    appendToken("d/dx(")
                } else {
                    // ∫ (integral)
                    appendToken("∫(")
                }
            }
            "INV" -> {
                if (isShift) {
                    // x! (factorial)
                    appendToken("!")
                } else {
                    // x⁻¹
                    appendToken("⁻¹")
                }
            }
            "LOG_AB" -> {
                if (isShift) {
                    appendToken("Σ(")
                } else {
                    appendToken("log_(")
                }
            }

            // ROW 2
            "FRAC" -> {
                if (isShift) {
                    // Mixed fraction template
                    appendToken(" 1/2")
                } else {
                    // Simple fraction /
                    appendToken("/")
                }
            }
            "SQRT" -> {
                if (isShift) {
                    appendToken("∛(")
                } else {
                    appendToken("√(")
                }
            }
            "SQUARE" -> {
                if (isShift) {
                    appendToken("³")
                } else {
                    appendToken("²")
                }
            }
            "POWER" -> {
                if (isShift) {
                    appendToken("^(1/")
                } else {
                    appendToken("^")
                }
            }
            "LOG" -> {
                if (isShift) {
                    appendToken("10^(")
                } else {
                    appendToken("log(")
                }
            }
            "LN" -> {
                if (isShift) {
                    appendToken("e^(")
                } else {
                    appendToken("ln(")
                }
            }

            // ROW 3
            "NEG" -> {
                if (isAlpha) appendToken("A")
                else if (isShift) appendToken("∠")
                else appendToken("(-)")
            }
            "DMS" -> {
                if (isAlpha) appendToken("B")
                else if (isShift) appendToken("°")
                else appendToken("°")
            }
            "HYP" -> {
                if (isAlpha) appendToken("C")
                else if (isShift) appendToken("Abs(")
                else _uiState.update { it.copy(isHyp = !it.isHyp) }
            }
            "SIN" -> {
                if (isAlpha) appendToken("D")
                else if (isShift) {
                    if (isHyp) {
                        _uiState.update { it.copy(isHyp = false) }
                        appendToken("sinh⁻¹(")
                    } else {
                        appendToken("sin⁻¹(")
                    }
                } else {
                    if (isHyp) {
                        _uiState.update { it.copy(isHyp = false) }
                        appendToken("sinh(")
                    } else {
                        appendToken("sin(")
                    }
                }
            }
            "COS" -> {
                if (isAlpha) appendToken("E")
                else if (isShift) {
                    if (isHyp) {
                        _uiState.update { it.copy(isHyp = false) }
                        appendToken("cosh⁻¹(")
                    } else {
                        appendToken("cos⁻¹(")
                    }
                } else {
                    if (isHyp) {
                        _uiState.update { it.copy(isHyp = false) }
                        appendToken("cosh(")
                    } else {
                        appendToken("cos(")
                    }
                }
            }
            "TAN" -> {
                if (isAlpha) appendToken("F")
                else if (isShift) {
                    if (isHyp) {
                        _uiState.update { it.copy(isHyp = false) }
                        appendToken("tanh⁻¹(")
                    } else {
                        appendToken("tan⁻¹(")
                    }
                } else {
                    if (isHyp) {
                        _uiState.update { it.copy(isHyp = false) }
                        appendToken("tanh(")
                    } else {
                        appendToken("tan(")
                    }
                }
            }

            // ROW 4
            "RCL" -> {
                if (isShift) {
                    _uiState.update { it.copy(isSto = true, isRcl = false) }
                } else {
                    _uiState.update { it.copy(isRcl = true, isSto = false) }
                }
            }
            "ENG" -> {
                if (isAlpha) {
                    appendToken("i")
                } else if (isShift) {
                    appendToken("i")
                } else {
                    toggleEngineeringFormat()
                }
            }
            "LPAREN" -> {
                if (isShift) appendToken("%")
                else appendToken("(")
            }
            "RPAREN" -> {
                if (isAlpha) appendToken("X")
                else if (isShift) appendToken(",")
                else appendToken(")")
            }
            "SD" -> {
                if (isAlpha) appendToken("Y")
                else toggleStandardDecimal()
            }
            "M_PLUS" -> {
                if (isAlpha) appendToken("M")
                else if (isShift) memorySubtract()
                else memoryAdd()
            }

            // ROW 5 (NUMBER KEYPAD TOP)
            "7" -> {
                if (isShift) _uiState.update { it.copy(showConstantsDialog = true) }
                else appendToken("7")
            }
            "8" -> {
                if (isShift) _uiState.update { it.copy(showConversionsDialog = true) }
                else appendToken("8")
            }
            "9" -> {
                if (isShift) clearVariables()
                else appendToken("9")
            }
            "DEL" -> deleteLast()
            "AC" -> {
                if (isShift) resetAll()
                else clearScreen()
            }

            // ROW 6
            "4" -> {
                if (isShift) appendToken("MatrixA")
                else appendToken("4")
            }
            "5" -> {
                if (isShift) appendToken("VectorA")
                else appendToken("5")
            }
            "6" -> appendToken("6")
            "MUL" -> {
                if (isShift) appendToken("nPr(")
                else appendToken("×")
            }
            "DIV" -> {
                if (isShift) appendToken("nCr(")
                else appendToken("÷")
            }

            // ROW 7
            "1" -> {
                if (isShift) appendToken("Stat(")
                else appendToken("1")
            }
            "2" -> {
                if (isShift) appendToken("Cmplx(")
                else appendToken("2")
            }
            "3" -> {
                if (isShift) appendToken("Base(")
                else appendToken("3")
            }
            "ADD" -> {
                if (isShift) appendToken("Pol(")
                else appendToken("+")
            }
            "SUB" -> {
                if (isShift) appendToken("Rec(")
                else appendToken("−")
            }

            // ROW 8
            "0" -> {
                if (isShift) appendToken("Rnd(")
                else appendToken("0")
            }
            "DOT" -> {
                if (isAlpha) appendToken("RanInt#(")
                else if (isShift) appendToken("Ran#")
                else appendToken(".")
            }
            "EXP" -> {
                if (isAlpha) appendToken("e")
                else if (isShift) appendToken("π")
                else appendToken("×10^")
            }
            "ANS" -> {
                if (isShift) {
                    // DRG angle unit change
                    cycleAngleMode()
                } else {
                    appendToken("Ans")
                }
            }
            "EQUALS" -> evaluateCurrent()

            else -> appendToken(key)
        }
    }

    private fun mapKeyToVar(key: String): Char? = when (key) {
        "(-)" -> 'A'
        "°'''" -> 'B'
        "hyp" -> 'C'
        "sin" -> 'D'
        "cos" -> 'E'
        "tan" -> 'F'
        ")" -> 'X'
        "S<=>D" -> 'Y'
        "M+" -> 'M'
        else -> null
    }

    private fun storeVariable(varChar: Char) {
        val currentVal = _uiState.value.decimalVal ?: 0.0
        val updatedVars = _uiState.value.variables.toMutableMap()
        updatedVars[varChar] = currentVal
        _uiState.update {
            it.copy(
                variables = updatedVars,
                isSto = false,
                result = "$varChar = ${formatNumber(currentVal)}"
            )
        }
    }

    private fun recallVariable(varChar: Char) {
        val v = _uiState.value.variables[varChar] ?: 0.0
        _uiState.update {
            it.copy(
                isRcl = false,
                expression = it.expression + varChar
            )
        }
    }

    private fun memoryAdd() {
        val currentVal = _uiState.value.decimalVal ?: 0.0
        val newM = _uiState.value.memoryM + currentVal
        val updatedVars = _uiState.value.variables.toMutableMap()
        updatedVars['M'] = newM
        _uiState.update {
            it.copy(
                memoryM = newM,
                hasMemory = true,
                variables = updatedVars,
                result = "M+ (${formatNumber(newM)})"
            )
        }
    }

    private fun memorySubtract() {
        val currentVal = _uiState.value.decimalVal ?: 0.0
        val newM = _uiState.value.memoryM - currentVal
        val updatedVars = _uiState.value.variables.toMutableMap()
        updatedVars['M'] = newM
        _uiState.update {
            it.copy(
                memoryM = newM,
                hasMemory = true,
                variables = updatedVars,
                result = "M- (${formatNumber(newM)})"
            )
        }
    }

    private fun clearVariables() {
        val cleared = _uiState.value.variables.mapValues { 0.0 }
        _uiState.update {
            it.copy(
                variables = cleared,
                memoryM = 0.0,
                hasMemory = false,
                result = "Memory Cleared"
            )
        }
    }

    private fun toggleEngineeringFormat() {
        val d = _uiState.value.decimalVal ?: return
        val exp = (floor(log10(abs(d))) / 3).toInt() * 3
        val mantissa = d / 10.0.pow(exp)
        val engStr = if (exp != 0) "%.4f×10^%d".format(mantissa, exp) else formatNumber(d)
        _uiState.update { it.copy(result = engStr) }
    }

    private fun toggleStandardDecimal() {
        val state = _uiState.value
        val frac = state.exactFraction
        val dec = state.decimalVal

        if (frac != null) {
            val nextFormat = when (state.displayFormat) {
                DisplayFormat.NATURAL_FRACTION -> DisplayFormat.MIXED_FRACTION
                DisplayFormat.MIXED_FRACTION -> DisplayFormat.DECIMAL
                DisplayFormat.DECIMAL -> DisplayFormat.NATURAL_FRACTION
                DisplayFormat.ENGINEERING -> DisplayFormat.NATURAL_FRACTION
            }

            val nextStr = when (nextFormat) {
                DisplayFormat.NATURAL_FRACTION -> "${frac.numerator}/${frac.denominator}"
                DisplayFormat.MIXED_FRACTION -> {
                    val (w, n, d) = frac.toMixed()
                    if (w != 0L && n != 0L) "$w $n/$d" else "${frac.numerator}/${frac.denominator}"
                }
                DisplayFormat.DECIMAL -> formatNumber(frac.toDouble())
                DisplayFormat.ENGINEERING -> formatNumber(frac.toDouble())
            }

            _uiState.update {
                it.copy(displayFormat = nextFormat, result = nextStr)
            }
        } else if (dec != null) {
            val fraction = Fraction.fromDouble(dec)
            if (fraction != null) {
                _uiState.update {
                    it.copy(
                        exactFraction = fraction,
                        result = "${fraction.numerator}/${fraction.denominator}"
                    )
                }
            }
        }
    }

    private fun appendToken(token: String) {
        _uiState.update {
            val nextExpr = if (it.result.isNotEmpty() && isNewCalculationToken(token)) {
                // Starting a fresh calculation
                token
            } else {
                it.expression + token
            }
            it.copy(
                expression = nextExpr,
                result = if (it.result.isNotEmpty() && isNewCalculationToken(token)) "" else it.result
            )
        }
    }

    private fun isNewCalculationToken(token: String): Boolean {
        return token in listOf(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
            "sin(", "cos(", "tan(", "sin⁻¹(", "cos⁻¹(", "tan⁻¹(",
            "sinh(", "cosh(", "tanh(", "sinh⁻¹(", "cosh⁻¹(", "tanh⁻¹(",
            "√(", "∛(", "log(", "ln(", "10^(", "e^(", "Ans", "π", "e",
            "Ran#", "RanInt#(", "d/dx(", "∫(", "Abs(", "Pol(", "Rec(", "nPr(", "nCr("
        )
    }

    private fun deleteLast() {
        _uiState.update {
            if (it.expression.isNotEmpty()) {
                // Delete word token if ends with function like "sin(" or "√("
                val patterns = listOf(
                    "sinh⁻¹(", "cosh⁻¹(", "tanh⁻¹(", "sin⁻¹(", "cos⁻¹(", "tan⁻¹(",
                    "sinh(", "cosh(", "tanh(", "sin(", "cos(", "tan(",
                    "log(", "ln(", "log_(", "10^(", "e^(", "RanInt#(", "Ran#", "Rnd(",
                    "d/dx(", "∫(", "√(", "∛(", "×10^", "Ans", "Abs(",
                    "MatrixA", "VectorA", "Stat(", "Cmplx(", "Base(", "Pol(", "Rec(", "nPr(", "nCr("
                )
                var newExpr = it.expression
                var matched = false
                for (p in patterns) {
                    if (newExpr.endsWith(p)) {
                        newExpr = newExpr.dropLast(p.length)
                        matched = true
                        break
                    }
                }
                if (!matched) {
                    newExpr = newExpr.dropLast(1)
                }
                it.copy(expression = newExpr)
            } else {
                it
            }
        }
    }

    private fun clearScreen() {
        _uiState.update {
            it.copy(
                expression = "",
                result = "",
                exactFraction = null,
                decimalVal = null,
                isShift = false,
                isAlpha = false,
                isHyp = false,
                isSto = false,
                isRcl = false
            )
        }
    }

    private fun resetAll() {
        _uiState.update {
            CalculatorUiState(
                angleMode = it.angleMode,
                history = it.history
            )
        }
    }

    fun evaluateCurrent() {
        val state = _uiState.value
        val expr = state.expression.trim()
        if (expr.isEmpty()) return

        val evalRes = evaluator.evaluate(
            expr = expr,
            angleMode = state.angleMode,
            variables = state.variables,
            ans = state.ans
        )

        val newHistoryItem = CalculationHistory(
            expression = expr,
            result = evalRes.displayStr,
            exactFraction = evalRes.exactFraction,
            decimalVal = evalRes.decimalVal,
            isError = evalRes.isError,
            angleMode = state.angleMode
        )

        _uiState.update {
            it.copy(
                result = evalRes.displayStr,
                exactFraction = evalRes.exactFraction,
                decimalVal = evalRes.decimalVal,
                ans = evalRes.decimalVal ?: it.ans,
                history = it.history + newHistoryItem,
                historyIndex = -1
            )
        }
    }

    // Replay D-Pad Handlers
    fun onReplayUp() {
        val history = _uiState.value.history
        if (history.isEmpty()) return

        var newIdx = _uiState.value.historyIndex
        if (newIdx == -1) {
            newIdx = history.lastIndex
        } else if (newIdx > 0) {
            newIdx--
        }

        val item = history[newIdx]
        _uiState.update {
            it.copy(
                expression = item.expression,
                result = item.result,
                exactFraction = item.exactFraction,
                decimalVal = item.decimalVal,
                historyIndex = newIdx
            )
        }
    }

    fun onReplayDown() {
        val history = _uiState.value.history
        if (history.isEmpty() || _uiState.value.historyIndex == -1) return

        var newIdx = _uiState.value.historyIndex
        if (newIdx < history.lastIndex) {
            newIdx++
            val item = history[newIdx]
            _uiState.update {
                it.copy(
                    expression = item.expression,
                    result = item.result,
                    exactFraction = item.exactFraction,
                    decimalVal = item.decimalVal,
                    historyIndex = newIdx
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    expression = "",
                    result = "",
                    historyIndex = -1
                )
            }
        }
    }

    fun onReplayLeft() {
        // Quick history viewer trigger or cursor left
        _uiState.update { it.copy(showHistoryDialog = true) }
    }

    fun onReplayRight() {
        _uiState.update { it.copy(showHistoryDialog = true) }
    }

    fun onReplayCenter() {
        _uiState.update { it.copy(showHistoryDialog = true) }
    }

    // Mode & Setup
    fun setCalcMode(mode: CalcMode) {
        _uiState.update { it.copy(calcMode = mode, showModeDialog = false) }
    }

    fun setAngleMode(mode: AngleMode) {
        _uiState.update { it.copy(angleMode = mode, showSetupDialog = false) }
    }

    fun cycleAngleMode() {
        val next = when (_uiState.value.angleMode) {
            AngleMode.DEG -> AngleMode.RAD
            AngleMode.RAD -> AngleMode.GRAD
            AngleMode.GRAD -> AngleMode.DEG
        }
        _uiState.update { it.copy(angleMode = next) }
    }

    fun insertConstant(c: ScientificConstant) {
        appendToken(c.value.toString())
    }

    fun applyConversionResult(res: Double) {
        _uiState.update {
            it.copy(
                decimalVal = res,
                result = formatNumber(res),
                exactFraction = Fraction.fromDouble(res)
            )
        }
    }

    fun recallHistoryItem(item: CalculationHistory) {
        _uiState.update {
            it.copy(
                expression = item.expression,
                result = item.result,
                exactFraction = item.exactFraction,
                decimalVal = item.decimalVal,
                showHistoryDialog = false
            )
        }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList(), historyIndex = -1) }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                showModeDialog = false,
                showSetupDialog = false,
                showHistoryDialog = false,
                showConstantsDialog = false,
                showConversionsDialog = false
            )
        }
    }
}
