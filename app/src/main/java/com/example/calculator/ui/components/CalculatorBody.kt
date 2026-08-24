package com.example.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Technical Dashboard / Data Grid Chassis Colors
val ChassisBg = Color(0xFFD1D5DB)         // Crisp technical gray casing
val ChassisBorder = Color(0xFF9CA3AF)     // Defined border gray
val ChassisOuterBg = Color(0xFFE5E7EB)    // Page background
val ChassisAccent = Color(0xFF6B7280)

/**
 * Technical Dashboard / Data Grid Calculator Shell / Casing
 */
@Composable
fun CalculatorChassis(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChassisOuterBg)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                .border(1.5.dp, ChassisBorder, RoundedCornerShape(12.dp))
                .background(ChassisBg, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )

            // Bottom accent strip from Technical Dashboard design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color(0xFF9CA3AF))
            )
        }
    }
}

/**
 * Technical Top Header
 */
@Composable
fun CalculatorHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color(0xFF9CA3AF),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT BRANDING & TECHNICAL HEADER
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SCIENTIFIC CALCULATOR",
                color = Color(0xFF4B5563),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
            Text(
                text = "This Calculator Created by Sadikul",
                color = Color(0xFF1F2937),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 1.dp)
            )
        }

        // RIGHT SOLAR / POWER MODULE
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(68.dp)
                    .height(18.dp)
                    .shadow(1.dp, RoundedCornerShape(2.dp))
                    .border(1.dp, Color(0xFF374151), RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF374151), Color(0xFF4B5563), Color(0xFF374151))
                        ),
                        RoundedCornerShape(2.dp)
                    )
                    .drawBehind {
                        val step = size.width / 4
                        for (k in 1..3) {
                            val x = k * step
                            drawLine(
                                color = Color(0xFF1F2937),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
            )
            Text(
                text = "TWO WAY POWER",
                color = Color(0xFF6B7280),
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

