package com.litecodez.tracksc.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.contentProvider
import kotlin.math.*

@Composable
fun WaveEffect(
    modifier: Modifier = Modifier,
    waveColor: Color = contentProvider.textThemeColor.value,
    waveHeight: Float = 100f,
    waves: Int = 2
) {
    // Create infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Animate phase shift of the wave
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(waveHeight.dp)
    ) {
        val width = size.width
        val height = size.height
        val path = Path()

        // Draw multiple waves with phase differences
        for (i in 0 until waves) {
            val phaseShift = phase + (i * PI.toFloat() / waves)
            drawWave(path, width, height, phaseShift, waveColor.copy(alpha = 1f / (i + 1)))
        }
    }
}

private fun DrawScope.drawWave(
    path: Path,
    width: Float,
    height: Float,
    phase: Float,
    color: Color
) {
    path.reset()

    // Wave parameters
    val amplitude = height * 0.2f
    val frequency = 0.02f

    // Start from bottom-left
    path.moveTo(0f, height)

    // Create wave path
    for (x in 0..width.toInt()) {
        val y = height / 2 + amplitude *
                sin(x * frequency + phase)
        path.lineTo(x.toFloat(), y)
    }

    // Complete the path
    path.lineTo(width, height)
    path.lineTo(0f, height)
    path.close()

    // Draw the wave
    drawPath(path = path, color = color)
}