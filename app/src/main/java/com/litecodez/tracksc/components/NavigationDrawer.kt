package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes

@Composable
fun NavigationDrawer(modifier: Modifier = Modifier, showDrawer:Boolean, onDismiss: (Boolean) -> Unit = {}){

    val localShowDrawer by rememberUpdatedState(newValue = showDrawer)
    if(localShowDrawer) {
        SimpleAnimator(
            style = AnimationStyle.LEFT,
        ) {
            Box(modifier = modifier.background(
                color = contentProvider.majorThemeColor.value
            )) {
                Column(modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                    .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable {
                                Controller.showWallpaperSelector.value = true
                                onDismiss(false)
                            }
                            .padding(TCDataTypes.Fibonacci.EIGHT.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = "Wallpaper",
                            fontSize = TCDataTypes.Fibonacci.TWENTY_ONE.sp,
                            color = contentProvider.textThemeColor.value
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Select theme")
                    Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHT.dp))
                    ThemeSelector()
                }
            }
        }

    }
}