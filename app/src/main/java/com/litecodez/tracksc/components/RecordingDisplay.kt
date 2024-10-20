package com.litecodez.tracksc.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.R
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getCurrentDate
import com.litecodez.tracksc.getCurrentTime
import com.litecodez.tracksc.models.AudioRecorder
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.TCDataTypes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun RecordingDisplay(
    modifier: Modifier = Modifier,
    onCancel: (Boolean) -> Unit = {},
    audioRecorder: AudioRecorder,
    onComplete: (Boolean, File) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "recording_animation")

    // State management
    var timer by rememberSaveable { mutableIntStateOf(3) }
    var ready by rememberSaveable { mutableStateOf(false) }
    val isRecording by audioRecorder.isRecording.collectAsState()
    val error by audioRecorder.error.collectAsState()

    // Animation scale
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    // Timer countdown effect
    LaunchedEffect(Unit) {
        try {
            while (timer > 0) {
                delay(1000)
                timer--
            }
            ready = true
        } catch (e: CancellationException) {
            // Handle cancellation if needed
            ready = false
            timer = 3
        }
    }

    // Start recording when ready
    LaunchedEffect(ready) {
        if (ready) {
            audioRecorder.startRecording(getCurrentDate() + getCurrentTime(pattern = "HH:mm:ss"))
                .onFailure { throwable ->

                    ready = false
                    timer = 3

                }
        }
    }


    Box(
        modifier = modifier
            .background(
                Color.White,
                shape = RoundedCornerShape(TCDataTypes.Fibonacci.THIRTEEN.dp)
            )
    ) {
        // Note animations
        SimpleAnimator(
            style = AnimationStyle.SCALE_IN_CENTER,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            ImageAnimation(
                modifier = Modifier.align(Alignment.TopStart),
                image = R.drawable.note2,
                colorAnim = true,
                rotateAnim = true,
                initialRotationDeg = -10f,
                targetRotationDeg = 10f,
                firstColor = contentProvider.minorThemeColor.value,
                secondColor = contentProvider.majorThemeColor.value,
                size = 89,
                startDelay = 0f
            )
        }

        SimpleAnimator(
            style = AnimationStyle.SCALE_IN_CENTER,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ImageAnimation(
                modifier = Modifier.align(Alignment.BottomEnd),
                image = R.drawable.note1,
                colorAnim = true,
                rotateAnim = true,
                initialRotationDeg = 10f,
                targetRotationDeg = -10f,
                firstColor = contentProvider.majorThemeColor.value,
                secondColor = contentProvider.minorThemeColor.value,
                size = 89,
                startDelay = 0f
            )
        }

        // Timer or Recording UI
        if (!ready) {
            Text(
                "$timer",
                fontSize = TCDataTypes.Fibonacci.FIFTY_FIVE.sp,
                color = contentProvider.majorThemeColor.value,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "record",
                tint = Color.Red,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .fillMaxSize(0.8f)
                    .clickable {
                        scope.launch {
                            audioRecorder.stopRecording().onSuccess {
                                onComplete(true, it)
                                //ready = false
                                //timer = 3
                            }
                        }
                    }
            )
        }

        // Control buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        if (isRecording) {
                            audioRecorder.stopRecording()
                        }
                        //ready = false
                        //timer = 3
                        onCancel(false)
                    }
                }
            ) {
                Text(
                    "Cancel",
                    color = Color.Red,
                    fontSize = 21.sp,
                    textAlign = TextAlign.Center
                )
            }

            TextButton(
                onClick = {
                    scope.launch {
                        audioRecorder.stopRecording().onSuccess {
                            onComplete(true, it)
                            //ready = false
                            //timer = 3
                        }
                    }
                }
            ) {
                Text(
                    "Done",
                    color = Color.Black,
                    fontSize = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(
                            color = Color.Green,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}