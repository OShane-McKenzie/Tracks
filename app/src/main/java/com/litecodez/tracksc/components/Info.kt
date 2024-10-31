package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun Info(modifier: Modifier = Modifier, info:String, callBack: (Boolean)->Unit = {}){
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.padding(5.dp)){

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
                .verticalScroll(scrollState)
        ) {
            MarkdownText(markdown = info, modifier = Modifier.padding(5.dp))
        }

        IconButton(
            onClick = {
                callBack.invoke(false)
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
    }
}