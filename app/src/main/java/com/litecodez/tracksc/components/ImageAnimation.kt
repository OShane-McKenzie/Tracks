package com.litecodez.tracksc.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


@Composable
fun ImageAnimation(
    modifier: Modifier = Modifier,
    image: Int,
    maxXOffset: Int = 20,
    maxYOffset: Int = 20,
    size: Int = 80,
    initialRotationDeg: Float = 0f,
    targetRotationDeg: Float = 90f,
    colorAnim:Boolean = true,
    rotateAnim: Boolean = false,
    offsetAnim: Boolean = true,
    startDelay: Float? = null,
    firstColor: Color = Color.Blue,
    secondColor: Color = Color.Black){

    val randomInt = remember{ startDelay ?: (1000..1500).random().toFloat() }
    val xOffset = remember{ (1..maxXOffset).random().toFloat() }
    val yOffset = remember{ (1..maxYOffset).random().toFloat() }
    val randomStart = remember{ (0..10).random().toFloat() }
    var startAnimation by remember {
        mutableStateOf(false)
    }
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val color by infiniteTransition.animateColor(
        initialValue = secondColor,
        targetValue = firstColor,
        animationSpec = infiniteRepeatable(
            tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = initialRotationDeg,
        targetValue = targetRotationDeg,
        animationSpec = infiniteRepeatable(
            tween(4000),
            repeatMode = RepeatMode.Reverse
            ),
        label = ""
    )

    val animatedDp1 by infiniteTransition.animateFloat(
        initialValue = randomStart,
        targetValue = xOffset,
        animationSpec = infiniteRepeatable(
            tween(4000),
            repeatMode = RepeatMode.Reverse),
        label = ""
    )
    val animatedDp2 by infiniteTransition.animateFloat(
        initialValue = randomStart,
        targetValue = yOffset,
        animationSpec = infiniteRepeatable(
            tween(4000),
            repeatMode = RepeatMode.Reverse),
        label = ""
    )
    LaunchedEffect(Unit){
        delay(randomInt.toLong())
        startAnimation = true
    }
    if(startAnimation){
        Icon(
            modifier = modifier
                .offset(x = if(offsetAnim) animatedDp1.dp else 0.dp, y = if(offsetAnim) animatedDp2.dp else 0.dp)
                .rotate(if(rotateAnim) rotation else 0f)
                .size(size.dp),
            painter = painterResource(id = image),
            contentDescription = "dancing note",
            tint = if(colorAnim) color else Color.Unspecified
        )
    }

}