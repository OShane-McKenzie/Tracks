package com.litecodez.tracksc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import com.litecodez.tracksc.components.SimpleAnimator
import com.litecodez.tracksc.components.YouTubePlayerTc
import com.litecodez.tracksc.models.YouTubePlayerViewModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.screens.ChatContainer
import com.litecodez.tracksc.screens.HomeScreen
import com.litecodez.tracksc.screens.LoginScreen
import com.litecodez.tracksc.screens.ProfileScreen
import com.litecodez.tracksc.screens.SplashScreen
import kotlin.math.roundToInt


@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun Root(operator: Operator, authenticationManager: AuthenticationManager,viewModel: YouTubePlayerViewModel){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as Activity
    var moveX by remember { mutableFloatStateOf(0f) }
    var moveY by remember { mutableFloatStateOf(0f) }

    appNavigator.GetBackHandler(
        context = context,
        lifecycleOwner =lifecycleOwner
    )

    LaunchedEffect(
        Controller.autoLockScreenOrientation.value
    ) {
        if(Controller.autoLockScreenOrientation.value){
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ){
        when(appNavigator.getView()){
            splash->{
                SimpleAnimator(
                    style = AnimationStyle.SCALE_IN_CENTER,
                ) {
                    SplashScreen(operator, authenticationManager)
                }

            }
            login->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    LoginScreen(operator, authenticationManager)
                }

            }
            profile->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    ProfileScreen(operator)
                }
            }
            home->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    HomeScreen(operator, authenticationManager)
                }
            }
            chatContainer->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    ChatContainer(modifier = Modifier.fillMaxSize(), operator = operator)
                }
            }
        }
        if(Controller.isHomeLoaded.value) {
            YouTubePlayerTc(modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .offset { IntOffset(moveX.roundToInt(), moveY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        moveX += dragAmount.x
                        moveY += dragAmount.y
                    }
                    detectTapGestures { }
                }, viewModel = viewModel
            )
        }
    }
}
