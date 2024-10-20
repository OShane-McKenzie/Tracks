package com.litecodez.tracksc.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun EnhancedZoomableContent(
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    onScaleChanged: (Float) -> Unit = {},
    resetOnDoubleTap: Boolean = true,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
        offsetX += panChange.x
        offsetY += panChange.y
        onScaleChanged(scale)
    }

    val doubleTapModifier = if (resetOnDoubleTap) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    scale = 1f
                    offsetX = 0f
                    offsetY = 0f
                }
            ) {

            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(doubleTapModifier)
            .transformable(
                state = transformableState,
                lockRotationOnZoomPan = true
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offsetX
                translationY = offsetY
            }
    ) {
        content()
    }
}