package com.example.calculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.ui.components.*

/**
 * Main Scientific Calculator Screen replicating physical handheld Casio fx-115ES
 */
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    CalculatorChassis(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. BRANDING & SOLAR PANEL HEADER
            CalculatorHeader(modifier = Modifier.padding(bottom = 4.dp))

            // 2. LARGE NATURAL DISPLAY LCD SCREEN
            LcdScreen(
                expression = uiState.expression,
                resultStr = uiState.result,
                angleMode = uiState.angleMode,
                calcMode = uiState.calcMode,
                isShift = uiState.isShift,
                isAlpha = uiState.isAlpha,
                isHyp = uiState.isHyp,
                isSto = uiState.isSto,
                isRcl = uiState.isRcl,
                hasMemory = uiState.hasMemory,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 3. TOP CONTROLS & CENTRAL REPLAY DISC ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Controls: SHIFT & ALPHA
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TopControlKey(
                        primaryText = "SHIFT",
                        topLabel = if (uiState.isShift) "▼ S" else "",
                        labelColor = LabelShiftYellow,
                        isActive = uiState.isShift,
                        onClick = { viewModel.onKeyClick("SHIFT") }
                    )
                    TopControlKey(
                        primaryText = "ALPHA",
                        topLabel = if (uiState.isAlpha) "▼ A" else "",
                        labelColor = LabelAlphaRed,
                        isActive = uiState.isAlpha,
                        onClick = { viewModel.onKeyClick("ALPHA") }
                    )
                }

                // Central Replay 4-Way D-Pad Disc
                CentralReplayPad(
                    onUp = { viewModel.onReplayUp() },
                    onDown = { viewModel.onReplayDown() },
                    onLeft = { viewModel.onReplayLeft() },
                    onRight = { viewModel.onReplayRight() },
                    onCenter = { viewModel.onReplayCenter() }
                )

                // Right Controls: MODE/SETUP & ON
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TopControlKey(
                        primaryText = "MODE",
                        topLabel = "SETUP",
                        labelColor = LabelShiftYellow,
                        onClick = { viewModel.onKeyClick("MODE") }
                    )
                    TopControlKey(
                        primaryText = "ON",
                        topLabel = "",
                        onClick = { viewModel.onKeyClick("ON") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 4. UPPER SCIENTIFIC FUNCTION SECTION
            // Function Row 1: CALC, ∫dx, x⁻¹, log_ab
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "CALC",
                    shiftText = "SOLVE",
                    alphaText = "=",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("CALC") }
                )
                PhysicalCalcKey(
                    primaryText = "∫dx",
                    shiftText = "d/dx",
                    alphaText = ":",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("INTEGRAL") }
                )
                PhysicalCalcKey(
                    primaryText = "x⁻¹",
                    shiftText = "x!",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("INV") }
                )
                PhysicalCalcKey(
                    primaryText = "log_■",
                    shiftText = "Σ",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("LOG_AB") }
                )
            }

            // Function Row 2: a/b, √, x², xʸ, log, ln
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "a/b",
                    shiftText = "d/c",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("FRAC") }
                )
                PhysicalCalcKey(
                    primaryText = "√",
                    shiftText = "∛",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("SQRT") }
                )
                PhysicalCalcKey(
                    primaryText = "x²",
                    shiftText = "x³",
                    baseText = "DEC",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("SQUARE") }
                )
                PhysicalCalcKey(
                    primaryText = "xʸ",
                    shiftText = "ʸ√",
                    baseText = "HEX",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("POWER") }
                )
                PhysicalCalcKey(
                    primaryText = "log",
                    shiftText = "10ˣ",
                    baseText = "BIN",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("LOG") }
                )
                PhysicalCalcKey(
                    primaryText = "ln",
                    shiftText = "eˣ",
                    baseText = "OCT",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("LN") }
                )
            }

            // Function Row 3: (-), °'", hyp, sin, cos, tan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "(-)",
                    shiftText = "∠",
                    alphaText = "A",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("NEG") }
                )
                PhysicalCalcKey(
                    primaryText = "° ' \"",
                    shiftText = "←",
                    alphaText = "B",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("DMS") }
                )
                PhysicalCalcKey(
                    primaryText = "hyp",
                    shiftText = "Abs",
                    alphaText = "C",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("HYP") }
                )
                PhysicalCalcKey(
                    primaryText = "sin",
                    shiftText = "sin⁻¹",
                    alphaText = "D",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("SIN") }
                )
                PhysicalCalcKey(
                    primaryText = "cos",
                    shiftText = "cos⁻¹",
                    alphaText = "E",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("COS") }
                )
                PhysicalCalcKey(
                    primaryText = "tan",
                    shiftText = "tan⁻¹",
                    alphaText = "F",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("TAN") }
                )
            }

            // Function Row 4: RCL, ENG, (, ), S<=>D, M+
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "RCL",
                    shiftText = "STO",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("RCL") }
                )
                PhysicalCalcKey(
                    primaryText = "ENG",
                    shiftText = "←",
                    alphaText = "i",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("ENG") }
                )
                PhysicalCalcKey(
                    primaryText = "(",
                    shiftText = "%",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("LPAREN") }
                )
                PhysicalCalcKey(
                    primaryText = ")",
                    shiftText = ",",
                    alphaText = "X",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("RPAREN") }
                )
                PhysicalCalcKey(
                    primaryText = "S↔D",
                    shiftText = "a b/c",
                    alphaText = "Y",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("SD") }
                )
                PhysicalCalcKey(
                    primaryText = "M+",
                    shiftText = "M-",
                    alphaText = "M",
                    variant = KeyVariant.FUNCTION,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("M_PLUS") }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. MAIN NUMBER KEYPAD (5 COLUMNS SCIENTIFIC LAYOUT)
            // Number Row 1: 7, 8, 9, DEL, AC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "7",
                    shiftText = "CONST",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("7") }
                )
                PhysicalCalcKey(
                    primaryText = "8",
                    shiftText = "CONV",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("8") }
                )
                PhysicalCalcKey(
                    primaryText = "9",
                    shiftText = "CLR",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("9") }
                )
                PhysicalCalcKey(
                    primaryText = "DEL",
                    shiftText = "INS",
                    variant = KeyVariant.MAROON,
                    height = 42.dp,
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("DEL") }
                )
                PhysicalCalcKey(
                    primaryText = "AC",
                    shiftText = "OFF",
                    variant = KeyVariant.MAROON,
                    height = 42.dp,
                    fontSize = 15,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("AC") }
                )
            }

            // Number Row 2: 4, 5, 6, ×, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "4",
                    shiftText = "MATRIX",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("4") }
                )
                PhysicalCalcKey(
                    primaryText = "5",
                    shiftText = "VECTOR",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("5") }
                )
                PhysicalCalcKey(
                    primaryText = "6",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("6") }
                )
                PhysicalCalcKey(
                    primaryText = "×",
                    shiftText = "nPr",
                    variant = KeyVariant.OPERATOR,
                    height = 42.dp,
                    fontSize = 20,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("MUL") }
                )
                PhysicalCalcKey(
                    primaryText = "÷",
                    shiftText = "nCr",
                    variant = KeyVariant.OPERATOR,
                    height = 42.dp,
                    fontSize = 20,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("DIV") }
                )
            }

            // Number Row 3: 1, 2, 3, +, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "1",
                    shiftText = "STAT",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("1") }
                )
                PhysicalCalcKey(
                    primaryText = "2",
                    shiftText = "CMPLX",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("2") }
                )
                PhysicalCalcKey(
                    primaryText = "3",
                    shiftText = "BASE",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("3") }
                )
                PhysicalCalcKey(
                    primaryText = "+",
                    shiftText = "Pol",
                    variant = KeyVariant.OPERATOR,
                    height = 42.dp,
                    fontSize = 20,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("ADD") }
                )
                PhysicalCalcKey(
                    primaryText = "−",
                    shiftText = "Rec",
                    variant = KeyVariant.OPERATOR,
                    height = 42.dp,
                    fontSize = 20,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("SUB") }
                )
            }

            // Number Row 4: 0, ., ×10ˣ, Ans, =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhysicalCalcKey(
                    primaryText = "0",
                    shiftText = "Rnd",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("0") }
                )
                PhysicalCalcKey(
                    primaryText = ".",
                    shiftText = "Ran#",
                    alphaText = "RanInt",
                    height = 42.dp,
                    fontSize = 18,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("DOT") }
                )
                PhysicalCalcKey(
                    primaryText = "×10ˣ",
                    shiftText = "π",
                    alphaText = "e",
                    height = 42.dp,
                    fontSize = 13,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("EXP") }
                )
                PhysicalCalcKey(
                    primaryText = "Ans",
                    shiftText = "DRG▶",
                    height = 42.dp,
                    fontSize = 14,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("ANS") }
                )
                PhysicalCalcKey(
                    primaryText = "=",
                    variant = KeyVariant.OPERATOR,
                    height = 42.dp,
                    fontSize = 22,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onKeyClick("EQUALS") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Interactive Dialogs & Overlays
    if (uiState.showModeDialog) {
        ModeSelectDialog(
            currentMode = uiState.calcMode,
            onSelectMode = { viewModel.setCalcMode(it) },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }

    if (uiState.showSetupDialog) {
        SetupDialog(
            angleMode = uiState.angleMode,
            onSetAngleMode = { viewModel.setAngleMode(it) },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }

    if (uiState.showHistoryDialog) {
        CalculationHistoryDialog(
            history = uiState.history,
            onRecall = { viewModel.recallHistoryItem(it) },
            onClear = { viewModel.clearHistory() },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }

    if (uiState.showConstantsDialog) {
        ConstantsDialog(
            onSelectConstant = { viewModel.insertConstant(it) },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }

    if (uiState.showConversionsDialog) {
        ConversionsDialog(
            currentVal = uiState.decimalVal ?: 0.0,
            onConvert = { viewModel.applyConversionResult(it) },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }
}
