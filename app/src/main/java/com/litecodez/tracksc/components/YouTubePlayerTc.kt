package com.litecodez.tracksc.components

import android.annotation.SuppressLint
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
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.YouTubePlayerViewModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Controller
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
    viewModel: YouTubePlayerViewModel
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
    LaunchedEffect(hasVideoEnded) {
        scope.launch {
            withContext(Dispatchers.IO){
                while (!hasVideoEnded){
                    withContext(Dispatchers.Main){
                        hasVideoEnded = viewModel.hasVideoEnded()
                    }
                    delay(3000)
                }
                viewModel.isTcPlayerPlaying = false
            }
        }
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
                }
        )

    }
}