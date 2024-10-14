package com.litecodez.tracksc.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.models.Video
import com.litecodez.tracksc.objects.TCDataTypes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItem(modifier: Modifier = Modifier, video: Video, onVideoItemClick: (String) -> Unit = {}) {
    Row(modifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable {
        onVideoItemClick(video.id) }
        .padding(TCDataTypes.Fibonacci.EIGHT.dp)
        .background(color = contentProvider.majorThemeColor.value, shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT))
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TCDataTypes.Fibonacci.FIVE.dp, Alignment.Start)
    ){
        Text(
            text = video.title+":",
            color = contentProvider.textThemeColor.value,
            fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).basicMarquee()
        )
        Text(
            text = video.artist,
            color = contentProvider.textThemeColor.value,
            fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
            modifier = Modifier.weight(0.8f).basicMarquee()
        )
        Text(
            text = video.genre,
            color = contentProvider.textThemeColor.value,
            fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
            fontWeight = FontWeight.Thin,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.weight(0.5f).basicMarquee()
        )
    }
}