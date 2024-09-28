package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.reactionsList

@Composable
fun ReactionPicker(modifier: Modifier = Modifier, onReactionSelected: (String) -> Unit={}){
    Row(
        modifier = modifier.wrapContentHeight().wrapContentWidth()
            .padding(horizontal = 5.dp, vertical = 2.dp)
            .background(
                color = setColorIfDarkTheme(
                    darkColor = Color.White.copy(alpha = 0.5f),
                    lightColor = Color.Black.copy(alpha = 0.5f),
                    invert = true
                ),
                shape = CircleShape
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
    ){
        reactionsList.forEach {
            Text(text = it, modifier = Modifier.clickable {onReactionSelected(it)})
        }
    }
}