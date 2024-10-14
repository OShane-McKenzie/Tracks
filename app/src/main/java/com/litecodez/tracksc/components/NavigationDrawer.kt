package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.extractVideoId
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes

@Composable
fun NavigationDrawer(modifier: Modifier = Modifier, showDrawer:Boolean, operator: Operator,onMore: () -> Unit = {},onDismiss: (Boolean) -> Unit = {}){

    val localShowDrawer by rememberUpdatedState(newValue = showDrawer)
    var videoListSearchText by rememberSaveable { mutableStateOf("") }
    var customLink by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val videoList = remember{
        derivedStateOf {
            contentProvider.videos.value.videos.sortedBy { it.artist }.filter {
                it.title.contains(videoListSearchText, ignoreCase = true) || it.artist.contains(
                    videoListSearchText,
                    ignoreCase = true
                )|| it.genre.contains(videoListSearchText, ignoreCase = true)
            }
        }
    }
    if(localShowDrawer) {
        SimpleAnimator(
            style = AnimationStyle.LEFT,
        ) {
            Box(modifier = modifier
                .background(
                    color = contentProvider.majorThemeColor.value
                )
                .padding(TCDataTypes.Fibonacci.FIVE.dp)
            ) {
                Column(modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                    .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(TCDataTypes.Fibonacci.EIGHT.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(
                            text = "Wallpaper",
                            fontSize = TCDataTypes.Fibonacci.TWENTY_ONE.sp,
                            color = contentProvider.textThemeColor.value,
                            modifier = Modifier.clickable {
                                Controller.showWallpaperSelector.value = true
                                onDismiss(false)
                            }
                        )
                        Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHT.dp))
                        OutlinedTextField(
                            value = videoListSearchText,
                            onValueChange = {
                                videoListSearchText = it
                            },
                            label = {
                                Text(text = "Search for music", color = contentProvider.textThemeColor.value)
                            },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = OutlinedTextFieldDefaults.colors().copy(
                                focusedTextColor = contentProvider.textThemeColor.value,
                            )
                        )
                        Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHT.dp))
                        Row(modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            Text(
                                text = "Title",
                                color = contentProvider.textThemeColor.value,
                                fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Artist",
                                color = contentProvider.textThemeColor.value,
                                fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp
                            )
                            Text(
                                text = "Genre",
                                color = contentProvider.textThemeColor.value,
                                fontSize = TCDataTypes.Fibonacci.THIRTEEN.sp,
                                fontWeight = FontWeight.Thin,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHT.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = contentProvider.textThemeColor.value,
                                shape = RoundedCornerShape(TCDataTypes.Fibonacci.THREE)
                            )
                            .fillMaxHeight(0.5f),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(videoList.value){ index, video ->
                            VideoListItem(video = video){
                                contentProvider.nowPlaying.value = it
                                contentProvider.currentChat.value.ifNotNull {  chat ->
                                    operator.updateConversationOperation(id = chat.id, updateMedia = true)
                                }
                                Controller.mediaPlayerReady.value = true
                                onDismiss(false)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(TCDataTypes.Fibonacci.EIGHT.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextField(
                            value = customLink,
                            onValueChange = {
                                customLink = it
                            },
                            label = {
                                Text(text = "Add custom link (youtube)")
                            },
                            colors = TextFieldDefaults.colors().copy(
                                focusedTextColor = contentProvider.textThemeColor.value,
                                focusedContainerColor = contentProvider.majorThemeColor.value
                            )
                        )
                        Text(
                            text = "Add",
                            color = contentProvider.textThemeColor.value,
                            modifier = Modifier.clickable {
                                val id = extractVideoId(customLink)
                                if(!id.isError){
                                    contentProvider.nowPlaying.value = id.msg
                                    //getToast(context, "Loading media, please wait...", long = true)
                                    Controller.mediaPlayerReady.value = true
                                    contentProvider.currentChat.value.ifNotNull {  chat ->
                                        operator.updateConversationOperation(id = chat.id, updateMedia = true)
                                    }
                                    onDismiss(false)

                                }else{
                                    getToast(context, id.msg)
                                }
                            }
                        )
                    }
                    contentProvider.currentChat.value.ifNotNull {
                        if(it.ownershipModel == TCDataTypes.OwnershipType.DUAL){
                            Button(
                                onClick = {
                                    onMore()
                                    onDismiss(false)
                                },
                                colors = ButtonDefaults.buttonColors().copy(
                                    containerColor = contentProvider.textThemeColor.value,
                                    contentColor = contentProvider.majorThemeColor.value)
                            ){
                                Text("More")
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Select theme")
                    Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHT.dp))
                    ThemeSelector()
                }
            }
        }

    }
}