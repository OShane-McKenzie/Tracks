package com.litecodez.tracksc.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.R
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.TCDataTypes

@Composable
fun WallpaperSelector(modifier: Modifier = Modifier, onSelect: (Int) -> Unit = {}){
    val interactionSource = remember { MutableInteractionSource() }
    val scrollState = rememberScrollState()
    val wallpaperList = remember {
        listOf(
            R.drawable.tracks_bg_1,
            R.drawable.tracks_bg_2,
            R.drawable.tracks_bg_3,
            R.drawable.tracks_bg_4,
            R.drawable.tracks_bg_5,
            R.drawable.tracks_bg_6,
            R.drawable.tracks_bg_7,
            R.drawable.tracks_bg_8,
        )
    }
    SimpleAnimator(
        style = AnimationStyle.SCALE_IN_CENTER,
    ){
        Row(
            modifier = modifier
                .background(
                    color = Color.Black.copy(alpha = 0.7f)
                )
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {

                }
                .padding(TCDataTypes.Fibonacci.EIGHT.dp)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(TCDataTypes.Fibonacci.TWENTY_ONE.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            wallpaperList.forEach {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = "wallpaper",
                    modifier = Modifier
                        .fillMaxSize(0.6f)
                        .clip(RoundedCornerShape(TCDataTypes.Fibonacci.FIVE.dp))
                        .clickable {
                            onSelect(it)
                        },
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}