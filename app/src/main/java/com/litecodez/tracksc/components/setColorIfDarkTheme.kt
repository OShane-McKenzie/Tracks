package com.litecodez.tracksc.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

@Composable
fun setColorIfDarkTheme(
    lightColor: Color = Color.White,
    darkColor: Color,
    invert: Boolean = true
): Color {
    return if(invert){
        if (isSystemInDarkTheme()) {
            lightColor
        } else {
            darkColor
        }
    }else{
        if (isSystemInDarkTheme()) {
            darkColor
        } else {
            lightColor
        }
    }

}