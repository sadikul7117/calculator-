package com.example.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.calculator.model.*

val DialogBg = Color(0xFF1F2937)
val DialogItemBg = Color(0xFF374151)
val DialogBorder = Color(0xFF4B5563)

/**
 * Mode Selection Dialog (COMP, CMPLX, STAT, BASE-N, EQN, MATRIX, VECTOR, TABLE)
 */
@Composable
fun ModeSelectDialog(
    currentMode: CalcMode,
    onSelectMode: (CalcMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT MODE",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                CalcMode.values().forEachIndexed { index, mode ->
                    Surface(
                        onClick = {
                            onSelectMode(mode)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (mode == currentMode) DialogItemBg else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}:",
                                color = LabelShiftYellow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Column {
                                Text(
                                    text = mode.label,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = mode.desc,
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Setup Dialog for Angle Units (DEG, RAD, GRAD) and Display Format
 */
@Composable
fun SetupDialog(
    angleMode: AngleMode,
    onSetAngleMode: (AngleMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SETUP (Angle Unit)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    Triple(1, "Deg", AngleMode.DEG),
                    Triple(2, "Rad", AngleMode.RAD),
                    Triple(3, "Gra", AngleMode.GRAD)
                ).forEach { (num, name, mode) ->
                    Surface(
                        onClick = {
                            onSetAngleMode(mode)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (mode == angleMode) DialogItemBg else Color.Transparent,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$num:",
                                color = LabelShiftYellow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                text = "$name (Angle)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calculation History Overlay Sheet
 */
@Composable
fun CalculationHistoryDialog(
    history: List<CalculationHistory>,
    onRecall: (CalculationHistory) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CALCULATION HISTORY",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color(0xFFFF8A80))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No calculations yet", color = Color(0xFF90A4AE), fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(history.reversed()) { item ->
                            Surface(
                                onClick = {
                                    onRecall(item)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = DialogItemBg,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = item.expression,
                                        color = Color(0xFFB0BEC5),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "= ${item.result}",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scientific Constants (CONST 01-40) Dialog
 */
@Composable
fun ConstantsDialog(
    onSelectConstant: (ScientificConstant) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f).padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCIENTIFIC CONSTANTS",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(ConstantsCatalog.ALL) { c ->
                        Surface(
                            onClick = {
                                onSelectConstant(c)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = DialogItemBg,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "%02d".format(c.code),
                                        color = LabelShiftYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(26.dp)
                                    )
                                    Text(
                                        text = c.symbol,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(42.dp)
                                    )
                                    Text(
                                        text = c.name,
                                        color = Color(0xFFCFD8DC),
                                        fontSize = 11.5.sp
                                    )
                                }
                                Text(
                                    text = c.unit,
                                    color = Color(0xFF90A4AE),
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Unit Conversions (CONV 01-40) Dialog
 */
@Composable
fun ConversionsDialog(
    currentVal: Double,
    onConvert: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DialogBg),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f).padding(4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UNIT CONVERSION (CONV)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    text = "Convert: $currentVal",
                    color = LabelShiftYellow,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(ConversionsCatalog.ALL) { conv ->
                        Surface(
                            onClick = {
                                val res = conv.convert(currentVal)
                                onConvert(res)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = DialogItemBg,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "%02d".format(conv.code),
                                        color = LabelShiftYellow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(26.dp)
                                    )
                                    Text(
                                        text = "${conv.fromUnit} ▶ ${conv.toUnit}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = conv.category,
                                    color = Color(0xFF90A4AE),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
