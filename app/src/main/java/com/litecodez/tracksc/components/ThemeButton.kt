package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.contentProvider

@Composable
fun ThemeButton(modifier: Modifier = Modifier, minorColor: Color, majorColor: Color, textColor: Color, size:Float = 30.0f, onClick:()->Unit={}){
    Column(
        modifier = modifier.wrapContentSize().clip(CircleShape)
            .clickable {
                contentProvider.majorThemeColor.value = majorColor
                contentProvider.minorThemeColor.value = minorColor
                contentProvider.textThemeColor.value = textColor
                onClick()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier
                .height((size/2).dp)
                .width(size.dp)
                .background(color = minorColor,
                    shape = RoundedCornerShape(
                        topEndPercent = 50,
                        topStartPercent = 50,
                        bottomEndPercent = 0,
                        bottomStartPercent = 0
                    )
                )
                .clip(
                    RoundedCornerShape(
                    topEndPercent = 50,
                    topStartPercent = 50,
                    bottomEndPercent = 0,
                    bottomStartPercent = 0
                    )
                )
        ){

        }
        Box(
            modifier = Modifier
                .height((size/2).dp)
                .width(size.dp)
                .background(color = majorColor,
                    shape = RoundedCornerShape(
                        topEndPercent = 0,
                        topStartPercent = 0,
                        bottomEndPercent = 50,
                        bottomStartPercent = 50
                    )
                ).clip(
                    RoundedCornerShape(
                    topEndPercent = 0,
                    topStartPercent = 0,
                    bottomEndPercent = 50,
                    bottomStartPercent = 50
                )
                )
        ){

        }
    }
}