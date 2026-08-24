package com.example.calculator.model

import kotlin.math.*

/**
 * Represents exact rational numbers (fractions) with automatic reduction.
 */
data class Fraction(val num: Long, val den: Long = 1L) : Comparable<Fraction> {
    init {
        require(den != 0L) { "Denominator cannot be zero" }
    }

    val numerator: Long
    val denominator: Long

    init {
        val g = gcd(abs(num), abs(den))
        val sign = if (den < 0) -1 else 1
        numerator = (num / g) * sign
        denominator = abs(den) / g
    }

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    operator fun plus(other: Fraction): Fraction =
        Fraction(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)

    operator fun minus(other: Fraction): Fraction =
        Fraction(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)

    operator fun times(other: Fraction): Fraction =
        Fraction(numerator * other.numerator, denominator * other.denominator)

    operator fun div(other: Fraction): Fraction {
        require(other.numerator != 0L) { "Division by zero fraction" }
        return Fraction(numerator * other.denominator, denominator * other.numerator)
    }

    fun reciprocal(): Fraction {
        require(numerator != 0L) { "Reciprocal of zero is undefined" }
        return Fraction(denominator, numerator)
    }

    fun isInteger(): Boolean = denominator == 1L

    fun isImproper(): Boolean = abs(numerator) >= denominator && denominator != 1L

    /**
     * Converts to mixed fraction components: (whole, remainderNumerator, denominator)
     */
    fun toMixed(): Triple<Long, Long, Long> {
        val whole = numerator / denominator
        val remNum = abs(numerator % denominator)
        return Triple(whole, remNum, denominator)
    }

    override fun compareTo(other: Fraction): Int =
        (numerator * other.denominator).compareTo(other.numerator * denominator)

    override fun toString(): String {
        return if (denominator == 1L) "$numerator" else "$numerator/$denominator"
    }

    companion object {
        val ZERO = Fraction(0, 1)
        val ONE = Fraction(1, 1)

        private fun gcd(a: Long, b: Long): Long {
            var x = a
            var y = b
            while (y != 0L) {
                val t = y
                y = x % y
                x = t
            }
            return if (x == 0L) 1L else x
        }

        fun fromDouble(value: Double, maxDenominator: Long = 100000L): Fraction? {
            if (value.isNaN() || value.isInfinite()) return null
            if (abs(value) > 1e10) return null

            val sign = if (value < 0) -1 else 1
            val absVal = abs(value)

            // Check for simple integers
            val roundVal = absVal.roundToLong()
            if (abs(absVal - roundVal) < 1e-9) {
                return Fraction(sign * roundVal, 1)
            }

            // Continued fractions algorithm
            var m00 = 1L
            var m01 = 0L
            var m10 = 0L
            var m11 = 1L

            var x = absVal
            var count = 0
            while (count < 20) {
                val a = x.toLong()
                val t00 = m00 * a + m01
                val t10 = m10 * a + m11
                if (t10 > maxDenominator) break

                m01 = m00
                m00 = t00
                m01 = m00
                m11 = m10
                m10 = t10

                val frac = x - a
                if (abs(frac) < 1e-9) {
                    return Fraction(sign * m00, m10)
                }
                x = 1.0 / frac
                count++
            }

            val error = abs(absVal - (m00.toDouble() / m10))
            return if (error < 1e-6 && m10 > 0) {
                Fraction(sign * m00, m10)
            } else null
        }
    }
}

/**
 * Complex number for CMPLX mode
 */
data class Complex(val real: Double, val imag: Double = 0.0) {
    fun magnitude(): Double = hypot(real, imag)
    fun phase(): Double = atan2(imag, real) // in radians

    operator fun plus(other: Complex) = Complex(real + other.real, imag + other.imag)
    operator fun minus(other: Complex) = Complex(real - other.real, imag - other.imag)
    operator fun times(other: Complex) = Complex(
        real * other.real - imag * other.imag,
        real * other.imag + imag * other.real
    )
    operator fun div(other: Complex): Complex {
        val d = other.real * other.real + other.imag * other.imag
        require(d != 0.0) { "Division by zero complex" }
        return Complex(
            (real * other.real + imag * other.imag) / d,
            (imag * other.real - real * other.imag) / d
        )
    }

    fun isReal(): Boolean = abs(imag) < 1e-12

    override fun toString(): String {
        return when {
            abs(imag) < 1e-12 -> formatNumber(real)
            abs(real) < 1e-12 -> "${formatNumber(imag)}i"
            imag < 0 -> "${formatNumber(real)} - ${formatNumber(-imag)}i"
            else -> "${formatNumber(real)} + ${formatNumber(imag)}i"
        }
    }

    companion object {
        val ZERO = Complex(0.0, 0.0)
        val ONE = Complex(1.0, 0.0)
        val I = Complex(0.0, 1.0)
    }
}

/**
 * Matrix for MATRIX mode
 */
data class Matrix(val rows: Int, val cols: Int, val data: List<Double>) {
    init {
        require(data.size == rows * cols) { "Data size does not match matrix dimensions" }
    }

    operator fun get(r: Int, c: Int): Double = data[r * cols + c]

    fun determinant(): Double {
        require(rows == cols) { "Determinant only valid for square matrices" }
        return when (rows) {
            1 -> this[0, 0]
            2 -> this[0, 0] * this[1, 1] - this[0, 1] * this[1, 0]
            3 -> {
                this[0, 0] * (this[1, 1] * this[2, 2] - this[1, 2] * this[2, 1]) -
                this[0, 1] * (this[1, 0] * this[2, 2] - this[1, 2] * this[2, 0]) +
                this[0, 2] * (this[1, 0] * this[2, 1] - this[1, 1] * this[2, 0])
            }
            else -> 0.0
        }
    }

    fun transpose(): Matrix {
        val res = ArrayList<Double>(rows * cols)
        for (c in 0 until cols) {
            for (r in 0 until rows) {
                res.add(this[r, c])
            }
        }
        return Matrix(cols, rows, res)
    }
}

/**
 * Vector for VECTOR mode (3D or 2D)
 */
data class Vector3(val x: Double, val y: Double, val z: Double = 0.0) {
    fun magnitude(): Double = sqrt(x * x + y * y + z * z)
    fun dot(other: Vector3): Double = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
}

enum class AngleMode { DEG, RAD, GRAD }

enum class CalcMode(val label: String, val desc: String) {
    COMP("COMP", "Standard Calculation"),
    CMPLX("CMPLX", "Complex Numbers"),
    STAT("STAT", "Statistics (SD / Reg)"),
    BASE_N("BASE-N", "Dec / Hex / Bin / Oct"),
    EQN("EQN", "Equation Solver"),
    MATRIX("MATRIX", "Matrix Algebra"),
    VECTOR("VECTOR", "Vector Operations"),
    TABLE("TABLE", "Function Table")
}

enum class BaseNMode(val label: String, val radix: Int, val prefix: String) {
    DEC("DEC", 10, "d:"),
    HEX("HEX", 16, "h:"),
    BIN("BIN", 2, "b:"),
    OCT("OCT", 8, "o:")
}

enum class DisplayFormat {
    NATURAL_FRACTION,
    MIXED_FRACTION,
    DECIMAL,
    ENGINEERING
}

data class CalculationHistory(
    val expression: String,
    val result: String,
    val exactFraction: Fraction? = null,
    val decimalVal: Double? = null,
    val isError: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG
)

fun formatNumber(value: Double, maxDecimals: Int = 10): String {
    if (value.isNaN()) return "Math ERROR"
    if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
    if (abs(value) < 1e-12) return "0"

    // Check if it's an integer
    if (abs(value - value.roundToLong()) < 1e-11 && abs(value) < 1e14) {
        return value.roundToLong().toString()
    }

    if (abs(value) >= 1e10 || (abs(value) < 1e-4 && abs(value) > 0)) {
        val exp = floor(log10(abs(value))).toInt()
        val mantissa = value / 10.0.pow(exp)
        return "%.${maxDecimals.coerceAtMost(6)}f×10^%d".format(mantissa, exp)
            .replace(".000000", "")
            .replace(Regex("(\\.\\d*?)0+×"), "$1×")
            .replace(".×", "×")
    }

    val formatted = "%.${maxDecimals}f".format(value).trimEnd('0').trimEnd('.')
    return if (formatted == "-0") "0" else formatted
}
