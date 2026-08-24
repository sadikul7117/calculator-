package com.example.calculator.engine

import com.example.calculator.model.*
import kotlin.math.*

class MathEvaluator {

    data class EvalResult(
        val displayStr: String,
        val exactFraction: Fraction? = null,
        val decimalVal: Double? = null,
        val complexVal: Complex? = null,
        val isError: Boolean = false,
        val errorMessage: String = ""
    )

    fun evaluate(
        expr: String,
        angleMode: AngleMode = AngleMode.DEG,
        variables: Map<Char, Double> = emptyMap(),
        ans: Double = 0.0
    ): EvalResult {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) {
            return EvalResult("0", Fraction.ZERO, 0.0)
        }

        try {
            // First attempt exact rational/fraction evaluation if applicable
            val fractionResult = evaluateFractionIfPossible(trimmed, variables, ans)
            if (fractionResult != null) {
                val mixed = fractionResult.toMixed()
                val display = when {
                    fractionResult.denominator == 1L -> "${fractionResult.numerator}"
                    fractionResult.isImproper() -> "${mixed.first} ${mixed.second}/${mixed.third}"
                    else -> "${fractionResult.numerator}/${fractionResult.denominator}"
                }
                return EvalResult(
                    displayStr = display,
                    exactFraction = fractionResult,
                    decimalVal = fractionResult.toDouble()
                )
            }

            // Otherwise evaluate as full scientific floating-point / complex expression
            val (valDouble, valComplex) = evaluateDoubleOrComplex(trimmed, angleMode, variables, ans)
            if (valComplex != null && !valComplex.isReal()) {
                return EvalResult(
                    displayStr = valComplex.toString(),
                    complexVal = valComplex,
                    decimalVal = valComplex.real
                )
            }

            val d = valDouble ?: valComplex?.real ?: 0.0
            if (d.isNaN()) {
                return EvalResult("Math ERROR", isError = true, errorMessage = "Math ERROR")
            }
            if (d.isInfinite()) {
                return EvalResult(if (d > 0) "Infinity" else "-Infinity", decimalVal = d)
            }

            // Check if can be converted to exact fraction
            val fractionFromDouble = Fraction.fromDouble(d)
            val formatted = formatNumber(d)

            return EvalResult(
                displayStr = formatted,
                exactFraction = fractionFromDouble,
                decimalVal = d
            )
        } catch (e: ArithmeticException) {
            return EvalResult("Math ERROR", isError = true, errorMessage = "Math ERROR: ${e.message}")
        } catch (e: Exception) {
            return EvalResult("Syntax ERROR", isError = true, errorMessage = "Syntax ERROR")
        }
    }

    private fun evaluateFractionIfPossible(
        rawExpr: String,
        vars: Map<Char, Double>,
        ans: Double
    ): Fraction? {
        // Quick check: if expression contains transcendentals (sin, cos, log, ln, sqrt of non-square, etc.), return null
        val transcendentalPattern = Regex("[a-zA-Z]{2,}|√|°|∫|d/dx|\\^")
        if (transcendentalPattern.containsMatchIn(rawExpr)) {
            // Check for simple integer powers like ^2, ^3
            if (!rawExpr.matches(Regex("^[0-9+\\-*/()\\s./]+(\\^[23])?.*$"))) {
                return null
            }
        }

        try {
            val tokens = tokenizeForFraction(rawExpr, vars, ans) ?: return null
            val rpn = toRpn(tokens) ?: return null
            return evalRpnFraction(rpn)
        } catch (e: Exception) {
            return null
        }
    }

    private fun tokenizeForFraction(expr: String, vars: Map<Char, Double>, ans: Double): List<String>? {
        val list = mutableListOf<String>()
        var i = 0
        val s = expr.replace("×", "*").replace("÷", "/").replace("−", "-")

        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    list.add(s.substring(start, i))
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')' -> {
                    list.add(c.toString())
                    i++
                }
                c == 'a' && i + 3 < s.length && s.substring(i, i + 3) == "Ans" -> {
                    val ansFrac = Fraction.fromDouble(ans) ?: return null
                    list.add("${ansFrac.numerator}/${ansFrac.denominator}")
                    i += 3
                }
                c in vars.keys -> {
                    val v = vars[c] ?: 0.0
                    val vFrac = Fraction.fromDouble(v) ?: return null
                    list.add("${vFrac.numerator}/${vFrac.denominator}")
                    i++
                }
                else -> return null // Unknown token for pure fraction
            }
        }
        return list
    }

    private fun toRpn(tokens: List<String>): List<String>? {
        val output = mutableListOf<String>()
        val ops = mutableListOf<String>()

        fun precedence(op: String) = when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "u-" -> 3
            else -> 0
        }

        var prevToken: String? = null

        for (token in tokens) {
            when {
                token.matches(Regex("^-?\\d+(\\.\\d+)?(/\\d+)?$")) -> {
                    output.add(token)
                }
                token == "(" -> ops.add(token)
                token == ")" -> {
                    while (ops.isNotEmpty() && ops.last() != "(") {
                        output.add(ops.removeAt(ops.lastIndex))
                    }
                    if (ops.isEmpty()) return null
                    ops.removeAt(ops.lastIndex) // Pop '('
                }
                token in listOf("+", "-", "*", "/") -> {
                    var actualOp = token
                    if (token == "-" && (prevToken == null || prevToken == "(" || prevToken in listOf("+", "-", "*", "/"))) {
                        actualOp = "u-"
                    }
                    while (ops.isNotEmpty() && precedence(ops.last()) >= precedence(actualOp)) {
                        output.add(ops.removeAt(ops.lastIndex))
                    }
                    ops.add(actualOp)
                }
                else -> return null
            }
            prevToken = token
        }

        while (ops.isNotEmpty()) {
            if (ops.last() == "(") return null
            output.add(ops.removeAt(ops.lastIndex))
        }

        return output
    }

    private fun evalRpnFraction(rpn: List<String>): Fraction? {
        val stack = mutableListOf<Fraction>()
        for (tok in rpn) {
            when (tok) {
                "+" -> {
                    if (stack.size < 2) return null
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a + b)
                }
                "-" -> {
                    if (stack.size < 2) return null
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a - b)
                }
                "*" -> {
                    if (stack.size < 2) return null
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a * b)
                }
                "/" -> {
                    if (stack.size < 2) return null
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    if (b.numerator == 0L) return null
                    stack.add(a / b)
                }
                "u-" -> {
                    if (stack.isEmpty()) return null
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(Fraction(-a.numerator, a.denominator))
                }
                else -> {
                    if (tok.contains("/")) {
                        val parts = tok.split("/")
                        val n = parts[0].toLongOrNull() ?: return null
                        val d = parts[1].toLongOrNull() ?: return null
                        stack.add(Fraction(n, d))
                    } else if (tok.contains(".")) {
                        val d = tok.toDoubleOrNull() ?: return null
                        val f = Fraction.fromDouble(d) ?: return null
                        stack.add(f)
                    } else {
                        val n = tok.toLongOrNull() ?: return null
                        stack.add(Fraction(n, 1))
                    }
                }
            }
        }
        return if (stack.size == 1) stack.first() else null
    }

    private fun evaluateDoubleOrComplex(
        expr: String,
        angleMode: AngleMode,
        vars: Map<Char, Double>,
        ans: Double
    ): Pair<Double?, Complex?> {
        val parser = ExpressionParser(expr, angleMode, vars, ans)
        return parser.parse()
    }
}

/**
 * Recursive Descent Expression Parser for full scientific calculator capabilities
 */
class ExpressionParser(
    private val rawExpr: String,
    private val angleMode: AngleMode,
    private val vars: Map<Char, Double>,
    private val ans: Double
) {
    private var pos = 0
    private val src: String = preprocess(rawExpr)

    private fun preprocess(s: String): String {
        return s.replace("×10^", "*10^")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("(-)", "-")
            .replace("π", "PI")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("Ans", "$ans")
    }

    private fun peek(): Char = if (pos < src.length) src[pos] else '\u0000'
    private fun next(): Char = if (pos < src.length) src[pos++] else '\u0000'

    private fun skipWhitespace() {
        while (pos < src.length && src[pos].isWhitespace()) pos++
    }

    private fun match(expected: String): Boolean {
        skipWhitespace()
        if (src.startsWith(expected, pos)) {
            pos += expected.length
            return true
        }
        return false
    }

    fun parse(): Pair<Double?, Complex?> {
        val result = parseExpression()
        skipWhitespace()
        if (pos < src.length) {
            throw IllegalArgumentException("Unexpected character '${src[pos]}' at position $pos")
        }
        return Pair(result, null)
    }

    private fun parseExpression(): Double {
        var result = parseTerm()
        while (true) {
            skipWhitespace()
            when {
                match("+") -> result += parseTerm()
                match("-") -> result -= parseTerm()
                else -> break
            }
        }
        return result
    }

    private fun parseTerm(): Double {
        var result = parsePower()
        while (true) {
            skipWhitespace()
            when {
                match("*") -> result *= parsePower()
                match("/") -> {
                    val d = parsePower()
                    if (d == 0.0) throw ArithmeticException("Division by zero")
                    result /= d
                }
                match("%") -> result = result % parsePower()
                // Implicit multiplication like 2(3) or 2sin(30) or 2π
                peek() == '(' || peek().isLetter() || peek() == '√' -> {
                    result *= parsePower()
                }
                else -> break
            }
        }
        return result
    }

    private fun parsePower(): Double {
        var result = parseFactor()
        skipWhitespace()
        if (match("^")) {
            val exponent = parseFactor()
            result = result.pow(exponent)
        } else if (match("!")) {
            result = factorial(result)
        } else if (match("²")) {
            result = result * result
        } else if (match("³")) {
            result = result * result * result
        } else if (match("⁻¹")) {
            if (result == 0.0) throw ArithmeticException("Division by zero")
            result = 1.0 / result
        }
        return result
    }

    private fun parseFactor(): Double {
        skipWhitespace()

        // Unary signs
        if (match("+")) return parseFactor()
        if (match("-")) return -parseFactor()

        // Parentheses
        if (match("(")) {
            val result = parseExpression()
            match(")")
            return result
        }

        // Functions
        if (match("sqrt(")) {
            val v = parseExpression()
            match(")")
            if (v < 0) throw ArithmeticException("Square root of negative number")
            return sqrt(v)
        }
        if (match("sqrt")) {
            val v = parseFactor()
            if (v < 0) throw ArithmeticException("Square root of negative number")
            return sqrt(v)
        }
        if (match("cbrt(")) {
            val v = parseExpression()
            match(")")
            return cbrt(v)
        }
        if (match("cbrt")) {
            return cbrt(parseFactor())
        }

        // Trigonometric functions
        if (match("sin⁻¹(") || match("asin(")) {
            val v = parseExpression()
            match(")")
            return fromRad(asin(v))
        }
        if (match("cos⁻¹(") || match("acos(")) {
            val v = parseExpression()
            match(")")
            return fromRad(acos(v))
        }
        if (match("tan⁻¹(") || match("atan(")) {
            val v = parseExpression()
            match(")")
            return fromRad(atan(v))
        }
        if (match("sinh⁻¹(") || match("asinh(")) {
            val v = parseExpression()
            match(")")
            return asinh(v)
        }
        if (match("cosh⁻¹(") || match("acosh(")) {
            val v = parseExpression()
            match(")")
            return acosh(v)
        }
        if (match("tanh⁻¹(") || match("atanh(")) {
            val v = parseExpression()
            match(")")
            return atanh(v)
        }

        if (match("sinh(")) {
            val v = parseExpression()
            match(")")
            return sinh(v)
        }
        if (match("cosh(")) {
            val v = parseExpression()
            match(")")
            return cosh(v)
        }
        if (match("tanh(")) {
            val v = parseExpression()
            match(")")
            return tanh(tanhToRad(v))
        }

        if (match("sin(")) {
            val v = parseExpression()
            match(")")
            return sin(toRad(v))
        }
        if (match("cos(")) {
            val v = parseExpression()
            match(")")
            return cos(toRad(v))
        }
        if (match("tan(")) {
            val v = parseExpression()
            match(")")
            val rad = toRad(v)
            val c = cos(rad)
            if (abs(c) < 1e-15) throw ArithmeticException("tan undefined")
            return tan(rad)
        }

        // Logarithms
        if (match("log(")) {
            val v = parseExpression()
            match(")")
            if (v <= 0) throw ArithmeticException("log of non-positive number")
            return log10(v)
        }
        if (match("ln(")) {
            val v = parseExpression()
            match(")")
            if (v <= 0) throw ArithmeticException("ln of non-positive number")
            return ln(v)
        }
        if (match("log_")) {
            // log base a of b: log_a(b)
            val base = parseFactor()
            match("(")
            val arg = parseExpression()
            match(")")
            if (base <= 0 || base == 1.0 || arg <= 0) throw ArithmeticException("Invalid log base/arg")
            return ln(arg) / ln(base)
        }

        // Absolute value
        if (match("abs(") || match("Abs(")) {
            val v = parseExpression()
            match(")")
            return abs(v)
        }

        // Permutations / Combinations: nPr(n, r) or nCr(n, r)
        if (match("nPr(") || match("P(")) {
            val n = parseExpression()
            match(",")
            val r = parseExpression()
            match(")")
            return permutation(n.roundToInt(), r.roundToInt())
        }
        if (match("nCr(") || match("C(")) {
            val n = parseExpression()
            match(",")
            val r = parseExpression()
            match(")")
            return combination(n.roundToInt(), r.roundToInt())
        }

        // Polar / Rectangular coordinates: Pol(x, y) -> returns r; Rec(r, θ) -> returns x
        if (match("Pol(")) {
            val x = parseExpression()
            match(",")
            val y = parseExpression()
            match(")")
            return hypot(x, y)
        }
        if (match("Rec(")) {
            val r = parseExpression()
            match(",")
            val theta = parseExpression()
            match(")")
            return r * cos(toRad(theta))
        }

        // Calculus: d/dx(expr, x0)
        if (match("d/dx(")) {
            val startPos = pos
            var depth = 1
            var commaPos = -1
            while (pos < src.length && depth > 0) {
                if (src[pos] == '(') depth++
                else if (src[pos] == ')') {
                    depth--
                    if (depth == 0) break
                } else if (src[pos] == ',' && depth == 1) {
                    commaPos = pos
                }
                pos++
            }
            if (commaPos != -1) {
                val exprStr = src.substring(startPos, commaPos)
                val x0Str = src.substring(commaPos + 1, pos)
                match(")")
                val x0Eval = ExpressionParser(x0Str, angleMode, vars, ans).parse().first ?: 0.0
                return CalculusEngine.derivative(exprStr, x0Eval, angleMode)
            } else {
                match(")")
                return CalculusEngine.derivative(src.substring(startPos, pos), 0.0, angleMode)
            }
        }

        // Integral: ∫(expr, a, b)
        if (match("∫(") || match("integral(")) {
            val startPos = pos
            var depth = 1
            val commas = mutableListOf<Int>()
            while (pos < src.length && depth > 0) {
                if (src[pos] == '(') depth++
                else if (src[pos] == ')') {
                    depth--
                    if (depth == 0) break
                } else if (src[pos] == ',' && depth == 1) {
                    commas.add(pos)
                }
                pos++
            }
            if (commas.size >= 2) {
                val exprStr = src.substring(startPos, commas[0])
                val aStr = src.substring(commas[0] + 1, commas[1])
                val bStr = src.substring(commas[1] + 1, pos)
                match(")")
                val aVal = ExpressionParser(aStr, angleMode, vars, ans).parse().first ?: 0.0
                val bVal = ExpressionParser(bStr, angleMode, vars, ans).parse().first ?: 1.0
                return CalculusEngine.integrate(exprStr, aVal, bVal, 100, angleMode)
            } else {
                match(")")
                return CalculusEngine.integrate(src.substring(startPos, pos), 0.0, 1.0, 100, angleMode)
            }
        }

        // Random functions
        if (match("Ran#") || match("ran#")) {
            return (kotlin.random.Random.nextDouble() * 1000.0).roundToInt() / 1000.0
        }
        if (match("RanInt#(") || match("ranInt(")) {
            val a = parseExpression().roundToInt()
            match(",")
            val b = parseExpression().roundToInt()
            match(")")
            val min = minOf(a, b)
            val max = maxOf(a, b)
            return kotlin.random.Random.nextInt(min, max + 1).toDouble()
        }
        if (match("Rnd(")) {
            val v = parseExpression()
            match(")")
            return (v * 1000000000.0).roundToLong() / 1000000000.0
        }

        // Constants
        if (match("PI") || match("π")) return Math.PI
        if (match("E") || (peek() == 'e' && (pos + 1 == src.length || !src[pos + 1].isLetter()))) {
            if (match("e")) return Math.E
            if (match("E")) return Math.E
        }

        // Variables (A, B, C, D, E, F, X, Y, M)
        val peekChar = peek()
        if (peekChar in vars.keys && (pos + 1 == src.length || !src[pos + 1].isLetter())) {
            next()
            return vars[peekChar] ?: 0.0
        }

        // Numerical value
        return parseNumber()
    }

    private fun parseNumber(): Double {
        skipWhitespace()
        val start = pos
        var hasDot = false
        var hasExp = false

        while (pos < src.length) {
            val c = src[pos]
            if (c.isDigit()) {
                pos++
            } else if (c == '.' && !hasDot) {
                hasDot = true
                pos++
            } else if ((c == 'e' || c == 'E' || (c == '×' && pos + 4 <= src.length && src.substring(pos, pos + 4) == "×10^")) && !hasExp) {
                if (src.startsWith("×10^", pos)) {
                    pos += 4
                    if (pos < src.length && (src[pos] == '+' || src[pos] == '-')) pos++
                    while (pos < src.length && src[pos].isDigit()) pos++
                    val numPart = src.substring(start, pos - 4).toDoubleOrNull() ?: 1.0
                    val expPart = src.substring(pos - 2, pos).toIntOrNull() ?: 0
                    return numPart * 10.0.pow(expPart)
                }
                hasExp = true
                pos++
                if (pos < src.length && (src[pos] == '+' || src[pos] == '-')) pos++
            } else {
                break
            }
        }

        val numStr = src.substring(start, pos)
        return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: '$numStr'")
    }

    private fun toRad(angle: Double): Double = when (angleMode) {
        AngleMode.DEG -> Math.toRadians(angle)
        AngleMode.RAD -> angle
        AngleMode.GRAD -> angle * Math.PI / 200.0
    }

    private fun fromRad(rad: Double): Double = when (angleMode) {
        AngleMode.DEG -> Math.toDegrees(rad)
        AngleMode.RAD -> rad
        AngleMode.GRAD -> rad * 200.0 / Math.PI
    }

    private fun tanhToRad(v: Double): Double = v

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170) throw ArithmeticException("Invalid factorial operand")
        var res = 1.0
        val k = n.toInt()
        for (i in 2..k) res *= i
        return res
    }

    private fun permutation(n: Int, r: Int): Double {
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid nPr arguments")
        var res = 1.0
        for (i in 0 until r) res *= (n - i)
        return res
    }

    private fun combination(n: Int, r: Int): Double {
        if (n < 0 || r < 0 || r > n) throw ArithmeticException("Invalid nCr arguments")
        val k = min(r, n - r)
        var res = 1.0
        for (i in 1..k) {
            res = res * (n - i + 1) / i
        }
        return res
    }

    private fun asinh(x: Double): Double = ln(x + sqrt(x * x + 1.0))
    private fun acosh(x: Double): Double {
        if (x < 1.0) throw ArithmeticException("acosh domain is x >= 1")
        return ln(x + sqrt(x * x - 1.0))
    }
    private fun atanh(x: Double): Double {
        if (abs(x) >= 1.0) throw ArithmeticException("atanh domain is -1 < x < 1")
        return 0.5 * ln((1.0 + x) / (1.0 - x))
    }
    private fun cbrt(x: Double): Double = if (x >= 0) x.pow(1.0 / 3.0) else -(-x).pow(1.0 / 3.0)
}
