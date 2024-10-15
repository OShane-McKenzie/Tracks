package com.litecodez.tracksc.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.PLAY
import com.litecodez.tracksc.PLAY_LIST
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.models.Video
import com.litecodez.tracksc.objects.TCDataTypes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItem(modifier: Modifier = Modifier, video: Video, onVideoItemClick: (String) -> Unit = {}) {
    val context = LocalContext.current
    Box(Modifier.padding(TCDataTypes.Fibonacci.EIGHT.dp)){
        Row(modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    onVideoItemClick(video.id)
                },
                onLongClick = {
                    contentProvider.currentPlaylist.value
                        .contains(video.id)
                        .let {
                            if (!it) {
                                val list = contentProvider.currentPlaylist.value.toMutableList()
                                list.add(video.id)
                                contentProvider.currentPlaylist.value = list.toList()
                                getToast(context, "${video.title} added to play list")
                            } else {
                                val list = contentProvider.currentPlaylist.value.toMutableList()
                                list.remove(video.id)
                                contentProvider.currentPlaylist.value = list.toList()
                                getToast(context, "${video.title} removed from play list")
                            }
                        }
                }
            )

            .background(
                color = contentProvider.majorThemeColor.value,
                shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT)
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                TCDataTypes.Fibonacci.FIVE.dp,
                Alignment.Start
            )
        ) {
            Text(
                text = video.title + ":",
                color = contentProvider.textThemeColor.value,
                fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee()
            )
            Text(
                text = video.artist,
                color = contentProvider.textThemeColor.value,
                fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
                modifier = Modifier
                    .weight(0.8f)
                    .basicMarquee()
            )
            Text(
                text = video.genre,
                color = contentProvider.textThemeColor.value,
                fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
                fontWeight = FontWeight.Thin,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .weight(0.5f)
                    .basicMarquee()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterEnd),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End)
        ){
            if(video.id == contentProvider.nowPlaying.value){
                Text(PLAY)
            }
            if(contentProvider.currentPlaylist.value.contains(video.id)){
                Text(PLAY_LIST)
            }
        }
    }
}