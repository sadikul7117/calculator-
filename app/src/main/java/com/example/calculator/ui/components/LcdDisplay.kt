package com.example.calculator.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.model.AngleMode
import com.example.calculator.model.CalcMode

// Authentic LCD colors matching Technical Dashboard / Data Grid theme
val LcdBgColor = Color(0xFF9FB1A0)       // Authentic pale sage-gray LCD background
val LcdBezelColor = Color(0xFF374151)   // Dark gray bezel border
val LcdInnerShadow = Color(0x33000000)
val LcdPixelColor = Color(0xFF111827)   // Dark slate/black ink LCD pixel
val LcdPixelDim = Color(0x28111827)     // Faint inactive segment ghosting
val LcdSolarColor = Color(0xFF374151)
val LcdSolarLine = Color(0xFF1F2937)

/**
 * Natural Display AST parser for real-time textbook-like visual layout
 */
sealed class DisplayElement {
    data class TextElem(val text: String) : DisplayElement()
    data class FractionElem(
        val whole: String? = null,
        val numerator: String,
        val denominator: String
    ) : DisplayElement()
    data class RadicalElem(
        val index: String? = null,
        val radicand: String
    ) : DisplayElement()
    data class PowerElem(
        val base: String,
        val exponent: String
    ) : DisplayElement()
    data class LogBaseElem(
        val base: String,
        val argument: String
    ) : DisplayElement()
    data class IntegralElem(
        val expr: String,
        val lower: String,
        val upper: String
    ) : DisplayElement()
    data class DerivativeElem(
        val expr: String,
        val point: String
    ) : DisplayElement()
}

/**
 * Parses raw calculator expression into Natural Display UI layout blocks
 */
object NaturalDisplayParser {
    fun parse(raw: String): List<DisplayElement> {
        if (raw.isEmpty()) return emptyList()
        val elements = mutableListOf<DisplayElement>()

        // Search for fraction patterns: mixed (e.g. 2 1/3 or 4 3/5) or simple (2/3)
        // Or structured tokens like frac(n,d), sqrt(x), pow(b,e), int(f,a,b), diff(f,x)
        var i = 0
        val s = raw

        while (i < s.length) {
            when {
                // Derivative: d/dx(expr)|x=val or diff(expr, point)
                s.startsWith("d/dx(", i) -> {
                    val end = findMatchingParen(s, i + 4)
                    val content = s.substring(i + 5, end)
                    val parts = content.split(",")
                    val expr = parts.getOrNull(0) ?: "x"
                    val pt = parts.getOrNull(1) ?: "0"
                    elements.add(DisplayElement.DerivativeElem(expr, pt))
                    i = end + 1
                }
                // Integral: ∫(expr, a, b)
                s.startsWith("∫(", i) -> {
                    val end = findMatchingParen(s, i + 1)
                    val content = s.substring(i + 2, end)
                    val parts = content.split(",")
                    val expr = parts.getOrNull(0) ?: "x"
                    val a = parts.getOrNull(1) ?: "0"
                    val b = parts.getOrNull(2) ?: "1"
                    elements.add(DisplayElement.IntegralElem(expr, a, b))
                    i = end + 1
                }
                // Square root: √(x) or sqrt(x)
                s.startsWith("√(", i) || s.startsWith("sqrt(", i) -> {
                    val openOffset = if (s.startsWith("√(", i)) 1 else 4
                    val end = findMatchingParen(s, i + openOffset)
                    val radicand = s.substring(i + openOffset + 1, end)
                    elements.add(DisplayElement.RadicalElem(null, radicand))
                    i = end + 1
                }
                // Cube root: ∛(x) or cbrt(x)
                s.startsWith("∛(", i) || s.startsWith("cbrt(", i) -> {
                    val openOffset = if (s.startsWith("∛(", i)) 1 else 4
                    val end = findMatchingParen(s, i + openOffset)
                    val radicand = s.substring(i + openOffset + 1, end)
                    elements.add(DisplayElement.RadicalElem("3", radicand))
                    i = end + 1
                }
                // Log base: log_a(b) or log(a,b)
                s.startsWith("log_(", i) -> {
                    val end = findMatchingParen(s, i + 4)
                    val content = s.substring(i + 5, end)
                    val parts = content.split(",")
                    val base = parts.getOrNull(0) ?: "10"
                    val arg = parts.getOrNull(1) ?: ""
                    elements.add(DisplayElement.LogBaseElem(base, arg))
                    i = end + 1
                }
                // Check for mixed fraction like "2 1/3" or "(2 1/3)"
                else -> {
                    // Collect standard string segment
                    val nextSpecial = findNextSpecial(s, i)
                    val segment = s.substring(i, nextSpecial)
                    if (segment.isNotEmpty()) {
                        elements.add(DisplayElement.TextElem(segment))
                    }
                    i = nextSpecial
                }
            }
        }

        return elements
    }

    private fun findMatchingParen(s: String, openPos: Int): Int {
        var depth = 1
        var p = openPos + 1
        while (p < s.length && depth > 0) {
            if (s[p] == '(') depth++
            else if (s[p] == ')') depth--
            if (depth == 0) return p
            p++
        }
        return s.length
    }

    private fun findNextSpecial(s: String, start: Int): Int {
        val targets = listOf("d/dx(", "∫(", "√(", "sqrt(", "∛(", "cbrt(", "log_(")
        var minIdx = s.length
        for (t in targets) {
            val idx = s.indexOf(t, start)
            if (idx in start until minIdx) {
                minIdx = idx
            }
        }
        return minIdx
    }
}

/**
 * High-fidelity LCD screen matching real Casio fx-115ES natural display layout
 */
@Composable
fun LcdScreen(
    expression: String,
    resultStr: String,
    angleMode: AngleMode,
    calcMode: CalcMode,
    isShift: Boolean,
    isAlpha: Boolean,
    isHyp: Boolean,
    isSto: Boolean,
    isRcl: Boolean,
    hasMemory: Boolean,
    modifier: Modifier = Modifier
) {
    // Blinking cursor
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )

    val exprScrollState = rememberScrollState()

    LaunchedEffect(expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF6B7280), RoundedCornerShape(8.dp))
            .background(LcdBezelColor, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        // Inner LCD screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LcdBgColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // 1. TOP STATUS INDICATOR ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    LcdStatusFlag(text = "S", active = isShift, isModifier = true)
                    LcdStatusFlag(text = "A", active = isAlpha, isModifier = true)
                    LcdStatusFlag(text = "M", active = hasMemory)
                    LcdStatusFlag(text = "STO", active = isSto)
                    LcdStatusFlag(text = "RCL", active = isRcl)
                    LcdStatusFlag(text = "STAT", active = calcMode == CalcMode.STAT)
                    LcdStatusFlag(text = "CMPLX", active = calcMode == CalcMode.CMPLX)
                    LcdStatusFlag(text = "MAT", active = calcMode == CalcMode.MATRIX)
                    LcdStatusFlag(text = "VCT", active = calcMode == CalcMode.VECTOR)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    // Angle Mode Indicator
                    LcdStatusFlag(text = "D", active = angleMode == AngleMode.DEG)
                    LcdStatusFlag(text = "R", active = angleMode == AngleMode.RAD)
                    LcdStatusFlag(text = "G", active = angleMode == AngleMode.GRAD)
                    LcdStatusFlag(text = "Math", active = true)
                    LcdStatusFlag(text = "▲", active = true)
                    LcdStatusFlag(text = "▼", active = true)
                    LcdStatusFlag(text = "Disp", active = resultStr.isNotEmpty())
                }
            }

            // Subtle divider line in LCD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0x22192520))
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 2. MAIN NATURAL EXPRESSION LINE (SCROLLABLE TEXTBOOK NOTATION)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 38.dp)
                    .horizontalScroll(exprScrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val parsed = remember(expression) { NaturalDisplayParser.parse(expression) }

                if (parsed.isEmpty()) {
                    // Blinking block cursor at empty start
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(18.dp)
                            .background(LcdPixelColor.copy(alpha = cursorAlpha))
                    )
                } else {
                    parsed.forEach { elem ->
                        when (elem) {
                            is DisplayElement.TextElem -> {
                                Text(
                                    text = elem.text,
                                    color = LcdPixelColor,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 20.sp
                                )
                            }
                            is DisplayElement.FractionElem -> {
                                FractionBlock(elem)
                            }
                            is DisplayElement.RadicalElem -> {
                                RadicalBlock(elem)
                            }
                            is DisplayElement.PowerElem -> {
                                PowerBlock(elem)
                            }
                            is DisplayElement.LogBaseElem -> {
                                LogBaseBlock(elem)
                            }
                            is DisplayElement.IntegralElem -> {
                                IntegralBlock(elem)
                            }
                            is DisplayElement.DerivativeElem -> {
                                DerivativeBlock(elem)
                            }
                        }
                    }

                    // Cursor at end of expression
                    Box(
                        modifier = Modifier
                            .padding(start = 1.dp)
                            .width(2.dp)
                            .height(18.dp)
                            .background(LcdPixelColor.copy(alpha = cursorAlpha))
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 3. RESULT LINE (LOWER RIGHT LARGE NATURAL LCD NUMERALS)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 30.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (resultStr.isNotEmpty()) {
                    ResultNaturalFormatter(resultStr)
                }
            }
        }
    }
}

@Composable
private fun LcdStatusFlag(
    text: String,
    active: Boolean,
    isModifier: Boolean = false
) {
    if (isModifier && active) {
        Box(
            modifier = Modifier
                .background(LcdPixelColor, RoundedCornerShape(2.dp))
                .padding(horizontal = 2.5.dp, vertical = 0.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = LcdBgColor,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
        }
    } else {
        Text(
            text = text,
            color = if (active) LcdPixelColor else LcdPixelDim,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Natural Fraction Block: Numerator above horizontal bar over Denominator
 */
@Composable
fun FractionBlock(elem: DisplayElement.FractionElem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        if (elem.whole != null) {
            Text(
                text = elem.whole,
                color = LcdPixelColor,
                fontSize = 17.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 2.dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 1.dp)
        ) {
            Text(
                text = elem.numerator,
                color = LcdPixelColor,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                lineHeight = 13.sp
            )
            Box(
                modifier = Modifier
                    .widthIn(min = 16.dp)
                    .height(1.5.dp)
                    .background(LcdPixelColor)
            )
            Text(
                text = elem.denominator,
                color = LcdPixelColor,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * Natural Radical Block: √ symbol with top horizontal vinculum
 */
@Composable
fun RadicalBlock(elem: DisplayElement.RadicalElem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        if (elem.index != null) {
            Text(
                text = elem.index,
                color = LcdPixelColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-5).dp)
            )
        }
        Text(
            text = "√",
            color = LcdPixelColor,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .drawBehind {
                    // Draw top vinculum line
                    drawLine(
                        color = LcdPixelColor,
                        start = Offset(0f, 2f),
                        end = Offset(size.width, 2f),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
                .padding(top = 2.dp, start = 1.dp, end = 2.dp)
        ) {
            Text(
                text = elem.radicand,
                color = LcdPixelColor,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Power block with raised superscript
 */
@Composable
fun PowerBlock(elem: DisplayElement.PowerElem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = elem.base,
            color = LcdPixelColor,
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = elem.exponent,
            color = LcdPixelColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = (-6).dp)
        )
    }
}

/**
 * Log with subscript base
 */
@Composable
fun LogBaseBlock(elem: DisplayElement.LogBaseElem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "log",
            color = LcdPixelColor,
            fontSize = 17.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = elem.base,
            color = LcdPixelColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = 4.dp)
        )
        Text(
            text = "(${elem.argument})",
            color = LcdPixelColor,
            fontSize = 17.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Calculus Integral notation: ∫_a^b expr dx
 */
@Composable
fun IntegralBlock(elem: DisplayElement.IntegralElem) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 2.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = elem.upper,
                color = LcdPixelColor,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "∫",
                color = LcdPixelColor,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = elem.lower,
                color = LcdPixelColor,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = " ${elem.expr} dx",
            color = LcdPixelColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Calculus Derivative notation: d/dx(expr)|x=point
 */
@Composable
fun DerivativeBlock(elem: DisplayElement.DerivativeElem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "d", color = LcdPixelColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.width(14.dp).height(1.dp).background(LcdPixelColor))
            Text(text = "dx", color = LcdPixelColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "(${elem.expr})|x=${elem.point}",
            color = LcdPixelColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Formats the result in Natural display (e.g. Mixed fraction 34 2/3 or standard fraction 2/3)
 */
@Composable
fun ResultNaturalFormatter(result: String) {
    // Check if result is a mixed fraction like "34 2/3" or simple fraction "2/3"
    val mixedMatch = Regex("^(-?\\d+)\\s+(\\d+)/(\\d+)$").find(result)
    val simpleFractionMatch = Regex("^(-?\\d+)/(\\d+)$").find(result)

    when {
        mixedMatch != null -> {
            val (whole, num, den) = mixedMatch.destructured
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = whole,
                    color = LcdPixelColor,
                    fontSize = 23.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = num,
                        color = LcdPixelColor,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                    Box(modifier = Modifier.widthIn(min = 18.dp).height(1.5.dp).background(LcdPixelColor))
                    Text(
                        text = den,
                        color = LcdPixelColor,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }
            }
        }
        simpleFractionMatch != null -> {
            val (num, den) = simpleFractionMatch.destructured
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = num,
                    color = LcdPixelColor,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 15.sp
                )
                Box(modifier = Modifier.widthIn(min = 18.dp).height(1.5.dp).background(LcdPixelColor))
                Text(
                    text = den,
                    color = LcdPixelColor,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 15.sp
                )
            }
        }
        else -> {
            Text(
                text = result,
                color = LcdPixelColor,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
    }
}
