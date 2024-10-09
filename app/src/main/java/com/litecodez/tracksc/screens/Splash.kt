package com.litecodez.tracksc.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appName
import com.litecodez.tracksc.components.CustomSnackBar
import com.litecodez.tracksc.components.TypeWriteText
import com.litecodez.tracksc.components.setColorIfDarkTheme
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.Operator

@Composable
fun SplashScreen(operator: Operator, authenticationManager: AuthenticationManager){
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val isVisible by rememberSaveable { mutableStateOf(false) }
    var showSnackBar by remember { mutableStateOf(false) }
    var snackBarInfo by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                setColorIfDarkTheme(
                    darkColor = Color.Black,
                    lightColor = Color.White,
                    invert = false
                )
            )
    ){
        Image(
            painter = painterResource(id = R.drawable.tc_logo_no_bg),
            contentDescription = "logo",
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(200.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            TypeWriteText(
                text = appName,
                isVisible = isVisible,
                style = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = LocalTextStyle.current.fontSize * 1.5f,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            )
        }

        LaunchedEffect(Unit){
            contentRepository.getVideos(context = context) {
                if(it.isError){
                    snackBarInfo = it.msg
                    showSnackBar = true
                }
                operator.splashOperation()
            }
        }

        if(showSnackBar){
            CustomSnackBar(info = snackBarInfo)
        }

    }
}