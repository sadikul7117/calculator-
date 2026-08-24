package com.example.calculator.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Technical Dashboard / Data Grid Button Color Palette
val KeyDarkNum = Color(0xFF1F2937)          // Number keys bg-[#1F2937]
val KeyFunction = Color(0xFF4B5563)         // Upper function keys bg-[#4B5563]
val KeyControl = Color(0xFF374151)          // Top control keys bg-[#374151]
val KeyMaroon = Color(0xFF7F1D1D)           // DEL / AC keys bg-[#7F1D1D]
val KeyOperator = Color(0xFF9CA3AF)         // Operator keys bg-gray-400

val LabelShiftYellow = Color(0xFFB45309)    // Shift secondary label #B45309
val LabelAlphaRed = Color(0xFF9D174D)       // Alpha secondary label #9D174D
val LabelBaseCyan = Color(0xFF0369A1)       // Base-N cyan
val LabelModePurple = Color(0xFF7E22CE)     // Stat/Matrix purple

enum class KeyVariant {
    STANDARD,      // Number keys (dark gray/slate)
    FUNCTION,      // Upper function keys (medium gray)
    MAROON,        // DEL / AC (dark red)
    OPERATOR,      // +, -, *, /, = (light gray with dark text)
    CONTROL_OVAL   // Top controls
}

/**
 * Tactile physical 3D button styled with Technical Dashboard / Data Grid theme
 */
@Composable
fun PhysicalCalcKey(
    primaryText: String,
    shiftText: String? = null,
    alphaText: String? = null,
    baseText: String? = null,
    variant: KeyVariant = KeyVariant.STANDARD,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    fontSize: Int = 15,
    testTag: String = primaryText,
    onClick: () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 60),
        label = "keyPressScale"
    )

    val (bgColor, borderColor, textColor, cornerRadius) = when (variant) {
        KeyVariant.MAROON -> Quadruple(KeyMaroon, Color(0xFF450A0A), Color.White, 8.dp)
        KeyVariant.OPERATOR -> Quadruple(KeyOperator, Color(0xFF6B7280), Color(0xFF1F2937), 8.dp)
        KeyVariant.FUNCTION -> Quadruple(KeyFunction, Color(0xFF374151), Color.White, 4.dp)
        KeyVariant.CONTROL_OVAL -> Quadruple(KeyControl, Color(0xFF1F2937), Color.White, 6.dp)
        KeyVariant.STANDARD -> Quadruple(KeyDarkNum, Color(0xFF111827), Color.White, 8.dp)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // SECONDARY FUNCTION LABELS ROW ABOVE THE BUTTON
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(13.dp)
                .padding(horizontal = 1.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shift Label (#B45309) on Left
            Text(
                text = shiftText ?: "",
                color = LabelShiftYellow,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1
            )

            // Base / Mode Label in Center
            if (baseText != null) {
                Text(
                    text = baseText,
                    color = LabelBaseCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    maxLines = 1
                )
            }

            // Alpha Label (#9D174D) on Right
            Text(
                text = alphaText ?: "",
                color = LabelAlphaRed,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1
            )
        }

        // PHYSICAL BUTTON CHASSIS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .scale(scale)
                .shadow(
                    elevation = if (isPressed) 1.dp else 2.5.dp,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .background(
                    color = if (isPressed) bgColor.copy(alpha = 0.85f) else bgColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = {
                            onClick()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = primaryText,
                color = textColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Top Control Keys (SHIFT, ALPHA, MODE SETUP, ON) styled with Technical Dashboard theme
 */
@Composable
fun TopControlKey(
    primaryText: String,
    topLabel: String? = null,
    labelColor: Color = LabelShiftYellow,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val activeBorderColor = if (isActive) labelColor else Color(0xFF1F2937)
    val activeBgColor = if (isActive) KeyControl.copy(alpha = 0.9f) else KeyControl

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = topLabel ?: "",
            color = labelColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.height(12.dp)
        )
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(26.dp)
                .shadow(if (isPressed || isActive) 1.dp else 2.dp, RoundedCornerShape(6.dp))
                .border(if (isActive) 1.5.dp else 1.dp, activeBorderColor, RoundedCornerShape(6.dp))
                .background(
                    if (isPressed) activeBgColor.copy(alpha = 0.8f) else activeBgColor,
                    RoundedCornerShape(6.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = primaryText,
                color = if (isActive) labelColor else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Central 4-Way REPLAY Navigation D-Pad Disc (Technical Dashboard style)
 */
@Composable
fun CentralReplayPad(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .size(76.dp)
            .shadow(3.dp, CircleShape)
            .border(2.dp, Color(0xFF9CA3AF), CircleShape)
            .background(
                Color(0xFFD1D5DB),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // UP BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 34.dp, height = 24.dp)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onUp()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Replay Up",
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(20.dp)
            )
        }

        // DOWN BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 34.dp, height = 24.dp)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onDown()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Replay Down",
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(20.dp)
            )
        }

        // LEFT BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 24.dp, height = 34.dp)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onLeft()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Replay Left",
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(20.dp)
            )
        }

        // RIGHT BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 24.dp, height = 34.dp)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onRight()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Replay Right",
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(20.dp)
            )
        }

        // CENTER "REPLAY" TEXT
        Text(
            text = "REPLAY",
            color = Color(0xFF6B7280),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onCenter()
            }
        )
    }
}

