package com.litecodez.tracksc.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.R
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getCurrentDate
import com.litecodez.tracksc.getCurrentTime
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserName
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.YouTubePlayerViewModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerTc(
    modifier: Modifier = Modifier,
    viewModel: YouTubePlayerViewModel,
    operator: Operator
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var songDetails by rememberSaveable {
        mutableStateOf("")
    }
    var showSongDetails by rememberSaveable {
        mutableStateOf(false)
    }
    var hasVideoEnded by rememberSaveable {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.bindService(context, lifecycle)
    }

    LaunchedEffect(contentProvider.nowPlaying.value) {
        if (contentProvider.nowPlaying.value.isNotEmpty() && Controller.mediaPlayerReady.value) {
            viewModel.loadVideo(contentProvider.nowPlaying.value, context)
            viewModel.play()
            hasVideoEnded = viewModel.hasVideoEnded()
        }
    }
    LaunchedEffect(contentProvider.playerState.intValue) {
        if(contentProvider.playerState.intValue == 0){
            viewModel.pause()
            println("PlayerStateReflected: "+viewModel.isTcPlayerPlaying)
        }
        Log.d("PlayerStateReflected", contentProvider.playerState.intValue.toString())
    }
    LaunchedEffect(contentProvider.currentSong.value) {
        contentProvider.currentSong.value.ifNotNull {
            songDetails = it.title + ": " + it.artist
            showSongDetails = true
            scope.launch {
                delay(8000)
                showSongDetails = false
            }
        }
    }
    Box(
        modifier = modifier
            .height(if (!showSongDetails) TCDataTypes.Fibonacci.FIFTY_FIVE.dp else TCDataTypes.Fibonacci.TWO_HUNDRED_AND_33.dp)
            .width(if (!showSongDetails) TCDataTypes.Fibonacci.FIFTY_FIVE.dp else TCDataTypes.Fibonacci.TWO_HUNDRED_AND_33.dp)
    ) {

        if(showSongDetails){
            SimpleAnimator(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.75f),
                style = AnimationStyle.RIGHT
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = contentProvider.majorThemeColor.value.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(TCDataTypes.Fibonacci.THREE)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        songDetails,
                        color = contentProvider.textThemeColor.value,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }
        }
        Image(
            painter = painterResource(
                if (viewModel.isTcPlayerPlaying) R.drawable.pause
                else R.drawable.play
            ),
            contentDescription = if (viewModel.isTcPlayerPlaying) "Pause" else "Play",
            modifier = Modifier
                .height(TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                .width(TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                .align(Alignment.CenterEnd)
                .clickable {
                    viewModel.togglePlayPause()
                    if(viewModel.isTcPlayerPlaying){
                        scope.launch {
                            withContext(Dispatchers.Main){
                                showSongDetails = true
                            }
                            delay(8000)
                            withContext(Dispatchers.Main){
                                showSongDetails = false
                            }
                        }
                    }else{
                        contentProvider.currentChat.value.ifNotNull {
                            val newMessage = MessageModel(
                                chatId = contentProvider.currentChat.value!!.id,
                                sender = getUserUid() ?: "",
                                senderName = getUserName(),
                                content = "${getUserName()} paused $songDetails",
                                type = TCDataTypes.MessageType.MEDIA_NOTIFICATION,
                                timestamp = "${getCurrentDate()} ${getCurrentTime()}",
                                reactions = mutableListOf()
                            )
                            operator.sendMessageToStagingOperation(
                                messageModel = newMessage,
                                id = contentProvider.currentChat.value?.id + getCurrentDate() + getCurrentTime()
                            ) { result ->
                                if (result.isError) {
                                    scope.launch(Dispatchers.Main) {
                                        getToast(context, "Error sending message: ${result.msg}")
                                    }
                                }
                            }
                        }
                    }
                }
        )

    }
}