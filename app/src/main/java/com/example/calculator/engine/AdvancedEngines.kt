package com.example.calculator.engine

import com.example.calculator.model.*
import kotlin.math.*

object CalculusEngine {

    /**
     * Approximates numerical derivative d/dx f(x) at x = x0
     * Using central difference: f'(x) ≈ (f(x+h) - f(x-h)) / (2h)
     */
    fun derivative(
        expressionTemplate: String, // e.g. "x^2 + 3x"
        x0: Double,
        angleMode: AngleMode = AngleMode.RAD
    ): Double {
        val evaluator = MathEvaluator()
        val h = 1e-6

        fun evalAt(xVal: Double): Double {
            val exprWithX = expressionTemplate.replace("x", "($xVal)").replace("X", "($xVal)")
            val res = evaluator.evaluate(exprWithX, angleMode)
            return res.decimalVal ?: 0.0
        }

        val fPlus = evalAt(x0 + h)
        val fMinus = evalAt(x0 - h)
        return (fPlus - fMinus) / (2.0 * h)
    }

    /**
     * Approximates definite integral ∫ [a, b] f(x) dx using Composite Simpson's 1/3 rule
     */
    fun integrate(
        expressionTemplate: String,
        a: Double,
        b: Double,
        n: Int = 100,
        angleMode: AngleMode = AngleMode.RAD
    ): Double {
        val evaluator = MathEvaluator()
        val steps = if (n % 2 == 1) n + 1 else n
        val h = (b - a) / steps

        fun evalAt(xVal: Double): Double {
            val exprWithX = expressionTemplate.replace("x", "($xVal)").replace("X", "($xVal)")
            val res = evaluator.evaluate(exprWithX, angleMode)
            return res.decimalVal ?: 0.0
        }

        var sum = evalAt(a) + evalAt(b)
        for (i in 1 until steps) {
            val x = a + i * h
            val fx = evalAt(x)
            sum += if (i % 2 == 1) 4.0 * fx else 2.0 * fx
        }

        return (h / 3.0) * sum
    }
}

object BaseNEngine {
    fun convert(valueStr: String, from: BaseNMode, to: BaseNMode): String {
        return try {
            val num = valueStr.trim().toLong(from.radix)
            num.toString(to.radix).uppercase()
        } catch (e: Exception) {
            "ERR"
        }
    }

    fun parseBase(valueStr: String, base: BaseNMode): Long? {
        return try {
            valueStr.trim().toLong(base.radix)
        } catch (e: Exception) {
            null
        }
    }

    fun formatBase(value: Long, base: BaseNMode): String {
        return value.toString(base.radix).uppercase()
    }
}
