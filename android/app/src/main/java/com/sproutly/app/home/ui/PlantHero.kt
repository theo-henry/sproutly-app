package com.sproutly.app.home.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.sproutly.app.core.design.BgElevated
import com.sproutly.app.core.design.LeafDeep
import com.sproutly.app.core.design.LeafGreen
import com.sproutly.app.core.design.LeafMint
import kotlin.math.sin

/**
 * Native Compose animated plant hero — Canvas-based.
 * Extension point: swap to a 3D renderer (Filament/SceneView) by replacing this composable.
 */
@Composable
fun PlantHero(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "plant")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "phase"
    )

    Box(
        modifier = modifier
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(listOf(BgElevated, LeafDeep.copy(alpha = 0.6f)))
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val baseY = size.height * 0.85f

            // Pot
            drawRoundRect(
                color = Color(0xFF2B1A12),
                topLeft = Offset(cx - 60.dp.toPx(), baseY),
                size = androidx.compose.ui.geometry.Size(120.dp.toPx(), 36.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            )

            // Stem
            val stemTop = Offset(cx, baseY - 120.dp.toPx())
            drawLine(
                color = LeafGreen,
                start = Offset(cx, baseY),
                end = stemTop,
                strokeWidth = 6.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )

            // Leaves — three layers, each gently rotating with the phase
            drawLeaf(cx - 20.dp.toPx(), baseY - 50.dp.toPx(), 70f, LeafGreen, phase, density = 1f)
            drawLeaf(cx + 20.dp.toPx(), baseY - 80.dp.toPx(), -60f, LeafMint, phase + 1f, density = 0.9f)
            drawLeaf(cx, baseY - 120.dp.toPx(), 0f, LeafMint.copy(alpha = 0.9f), phase + 2f, density = 0.8f)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeaf(
    x: Float, y: Float, baseAngle: Float, color: Color, phase: Float, density: Float,
) {
    val sway = sin(phase) * 4f
    rotate(degrees = baseAngle + sway, pivot = Offset(x, y)) {
        val path = Path().apply {
            moveTo(x, y)
            cubicTo(
                x + 30.dp.toPx() * density, y - 18.dp.toPx(),
                x + 50.dp.toPx() * density, y - 20.dp.toPx(),
                x + 60.dp.toPx() * density, y
            )
            cubicTo(
                x + 50.dp.toPx() * density, y + 18.dp.toPx(),
                x + 30.dp.toPx() * density, y + 18.dp.toPx(),
                x, y
            )
            close()
        }
        drawPath(path, color = color)
    }
}

