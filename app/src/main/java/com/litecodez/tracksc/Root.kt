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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import com.litecodez.tracksc.components.BackgroundDetector
import com.litecodez.tracksc.components.NoConnectionInfo
import com.litecodez.tracksc.components.SimpleAnimator
import com.litecodez.tracksc.components.YouTubePlayerTc
import com.litecodez.tracksc.models.YouTubePlayerViewModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Dependencies
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.screens.ChatContainer
import com.litecodez.tracksc.screens.Delete
import com.litecodez.tracksc.screens.HomeScreen
import com.litecodez.tracksc.screens.Loading
import com.litecodez.tracksc.screens.LoginScreen
import com.litecodez.tracksc.screens.ProfileScreen
import com.litecodez.tracksc.screens.SplashScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt


@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun Root(dependencies: Dependencies){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = context as Activity
    var moveX by remember { mutableFloatStateOf(0f) }
    var moveY by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    appNavigator.GetBackHandler(
        context = context,
        lifecycleOwner =lifecycleOwner
    )
    BackgroundDetector(lifecycleOwner = lifecycleOwner, onForeground = {
        Controller.isInBackground.value = false
    }){
        Controller.isInBackground.value = true
    }
    LaunchedEffect(
        Controller.autoLockScreenOrientation.value
    ) {
        if(Controller.autoLockScreenOrientation.value){
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    LaunchedEffect(
        contentProvider.playlistAutoplayEnabledDisabledRegister.value,
        contentProvider.currentChat.value
    ) {
        contentProvider.currentChat.value.ifNotNull {
            Controller.isPlayListEnabled.value = contentProvider.playlistAutoplayEnabledDisabledRegister.value.contains(it.id)
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO){
                while (true){
                    networkManager.isInternetConnected(context).let{
                        withContext(Dispatchers.Main){
                            Controller.isInternetConnected.value = it
                        }
                    }
                    delay(2000)
                }
            }
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
                    SplashScreen(dependencies.operator, dependencies.authenticationManager)
                }

            }
            login->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    LoginScreen(dependencies.operator, dependencies.authenticationManager)
                }

            }
            profile->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    ProfileScreen(dependencies.operator, Controller.isUpdatingUserProfile.value)
                }
            }
            home->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    HomeScreen(dependencies.operator, dependencies.authenticationManager)
                }
            }
            chatContainer->{
                SimpleAnimator(
                    style = AnimationStyle.RIGHT
                ) {
                    ChatContainer(modifier = Modifier.fillMaxSize(), audioRecorder = dependencies.audioRecorder,operator = dependencies.operator)
                }
            }
            loading->{
                SimpleAnimator(
                    style = AnimationStyle.LEFT
                ) {
                    Loading()
                }
            }

            delete->{
                SimpleAnimator(
                    style = AnimationStyle.LEFT
                ) {
                    Delete()
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
                }, viewModel = dependencies.tcYouTubePlayerViewModel, dependencies.operator
            )
        }
        if(!Controller.isInternetConnected.value){
            SimpleAnimator(
                style = AnimationStyle.UP,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                NoConnectionInfo(modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }
}


