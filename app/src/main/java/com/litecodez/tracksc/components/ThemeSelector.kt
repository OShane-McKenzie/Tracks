package com.litecodez.tracksc.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.savePreferences

@Composable

fun ThemeSelector(
    modifier: Modifier = Modifier
){
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(3.dp)
            .wrapContentHeight()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.Absolute.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ThemeButton(
            minorColor = Color(0xFFA5A5A5),
            majorColor = Color(0xFF000000),
            textColor = Color(0xFFFFFFFF),
            size = 50.0f
        ){
            savePreferences("minorColor","0xFFA5A5A5",context)
            savePreferences("majorColor","0xFF000000",context)
            savePreferences("textThemeColor","0xFFFFFFFF",context)
        }
        ThemeButton(
            minorColor = Color(0xFFC8E6C9),
            majorColor = Color(0xFF00897B),
            textColor = Color(0xFFFFFFFF),
            size = 50.0f
        ){
            savePreferences("minorColor","0xFFC8E6C9",context)
            savePreferences("majorColor","0xFF00897B",context)
            savePreferences("textThemeColor","0xFFFFFFFF",context)
        }
        ThemeButton(
            minorColor = Color(0xFFEF9A9A),
            majorColor = Color(0xFFF4511E),
            textColor = Color(0xFF000000),
            size = 50.0f
        ){
            savePreferences("minorColor","0xFFEF9A9A",context)
            savePreferences("majorColor","0xFFF4511E",context)
            savePreferences("textThemeColor","0xFF000000",context)
        }
        ThemeButton(
            minorColor = Color(0xFFBBDEFB),
            majorColor = Color(0xFF3949AB),
            textColor = Color(0xFFFFFFFF),
            size = 50.0f
        ){
            savePreferences("minorColor","0xFFBBDEFB",context)
            savePreferences("majorColor","0xFF3949AB",context)
            savePreferences("textThemeColor","0xFFFFFFFF",context)
        }
        ThemeButton(
            minorColor = Color(0xFFE1BEE7),
            majorColor = Color(0xFF8E24AA),
            textColor = Color(0xFFFFFFFF),
            size = 50.0f
        ){
            savePreferences("minorColor","0xFFE1BEE7",context)
            savePreferences("majorColor","0xFF8E24AA",context)
            savePreferences("textThemeColor","0xFFFFFFFF",context)
        }
        ThemeButton(
            minorColor = Color(0xFFFFACC8),  // Light Pink
            majorColor = Color(0xFFFF02D1),  // Bright Pink
            textColor = Color(0xFFFFFFFF),   // White for contrast
            size = 50.0f
        ) {
            savePreferences("minorColor", "0xFFFFACC8", context)  // Light Pink
            savePreferences("majorColor", "0xFFFF02D1", context)  // Bright Pink
            savePreferences("textThemeColor", "0xFFFFFFFF", context)  // White
        }
        ThemeButton(
            minorColor = Color(0xFFFFE0B2),
            majorColor = Color(0xFFFB8C00),
            textColor = Color(0xFF000000),
            size = 50.0f
        ){
            savePreferences("minorColor","0xFFFFE0B2",context)
            savePreferences("majorColor","0xFFFB8C00",context)
            savePreferences("textThemeColor","0xFF000000",context)
        }
        ThemeButton(
            minorColor = Color(0xFFAC9D9D),
            majorColor = Color(0xFFB17E01),
            textColor = Color(0xFF000000),
            size = 50.0f
        ){
            savePreferences("minorColor","0xFFAC9D9D",context)
            savePreferences("majorColor","0xFFB17E01",context)
            savePreferences("textThemeColor","0xFF000000",context)
        }
    }
}