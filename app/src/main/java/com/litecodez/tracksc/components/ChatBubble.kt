package com.litecodez.tracksc.components

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.R
import com.litecodez.tracksc.audioPlayer
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserName
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.ifNull
import com.litecodez.tracksc.models.AudioPlayer
import com.litecodez.tracksc.models.ReactionModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.saveBitmapToFile
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(modifier: Modifier = Modifier,
               message:MessageModel,
               index:Int,
               onClick:(Any) ->Unit ={},
               onDeleted: (Int) -> Unit = {},
               onReacted: (MutableList<ReactionModel>) -> Unit = {}
               ){

    var imageReady by rememberSaveable {mutableStateOf(false)}
    var localImageFound by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val path = context.filesDir
    val dir = File(path, Databases.Local.IMAGES_DB)
    val imgFile = File(dir, message.content + ".png")
    var imageData by remember { mutableStateOf<Any>(if (localImageFound) imgFile else "") }
    val pathA: File = context.filesDir
    val dirA = File(pathA, Databases.Local.AUDIO_DB)
    val audioFile = File(dirA, message.content)

    var audioReady by remember {
        mutableStateOf(false)
    }
    val reactionList = remember{ mutableStateListOf<ReactionModel>() }

    var showReactionPicker by rememberSaveable { mutableStateOf(false) }
    var reactionListInitialized by rememberSaveable {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        localImageFound = imgFile.exists()
        reactionList.clear()
        reactionList.addAll(message.reactions)
        reactionListInitialized = true
        fun getImage() {
            contentRepository.getImageUrl(
                bucket = Databases.Buckets.USER_UPLOADS,
                imageName = message.content + ".png"
            ) { success, url ->
                if (success) {
                    imageData = url
                    imageReady = true
                } else {
                    getToast(context, "Image not found", long = true)
                }
            }
        }
        if(!imageReady) {
            if (message.type == TCDataTypes.MessageType.IMAGE && !localImageFound) {
                getImage()
            } else if (message.type == TCDataTypes.MessageType.IMAGE) {
                if (imgFile.exists()) {
                    imageData = BitmapFactory.decodeFile(imgFile.absolutePath)
                    imageReady = true
                }
            }
        }

        if(message.type == TCDataTypes.MessageType.AUDIO){
            if(audioFile.exists()){
                audioReady = true
            }else{
                contentRepository.downloadAudio(message.content, dirA){
                    if (audioFile.exists()){
                        audioReady = true
                    }
                }
            }
        }
    }
    LaunchedEffect(Controller.reloadMessage.value) {
        if(reactionListInitialized){
            val chat = contentProvider.conversations.value.find {
                it.id == message.chatId
            }
            chat.ifNotNull { chatModel ->
                val thisMessage = chatModel.content.find { it.timestamp == message.timestamp }
                thisMessage.ifNotNull {
                    reactionList.clear()
                    reactionList.addAll(it.reactions)
                }
            }
        }
    }

    LaunchedEffect(Controller.reloadMessage.value) {
        delay(200)
        reactionList.clear()
        reactionList.addAll(message.reactions)
    }
    //if(TCDataTypes.UserType.isThisUser(message.sender)) Arrangement.End else Arrangement.Start
    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if(
                TCDataTypes.UserType.isThisUser(message.sender) &&
                message.type!=TCDataTypes.MessageType.MEDIA_NOTIFICATION
                ){
                Arrangement.spacedBy(8.dp, Alignment.End)
            }else if(
                !TCDataTypes.UserType.isThisUser(message.sender) &&
                message.type!=TCDataTypes.MessageType.MEDIA_NOTIFICATION
                ){
                Arrangement.spacedBy(8.dp, Alignment.Start)
            }else{
                Arrangement.Center
            }
        ){
            if(
                TCDataTypes.UserType.isThisUser(message.sender) &&
                message.type != TCDataTypes.MessageType.MEDIA_NOTIFICATION  &&
                message.type != TCDataTypes.MessageType.MESSAGE_USER_BLOCKED
                ){
                Column {
                    IconButton(
                        onClick = {
                            showReactionPicker = !showReactionPicker
                        },
                        modifier = Modifier
                            .size(21.dp)
                            .padding(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "emoji selector",
                            tint = if (showReactionPicker) contentProvider.textThemeColor.value else contentProvider.textThemeColor.value.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(21.dp)
                        )
                    }
                }
            }
            Box(Modifier.wrapContentWidth()) {
                SimpleAnimator(style = AnimationStyle.UP){
                    if(
                        message.type != TCDataTypes.MessageType.MEDIA_NOTIFICATION &&
                        message.type != TCDataTypes.MessageType.MESSAGE_USER_BLOCKED
                        ) {
                        Card(
                            modifier = Modifier

                                .padding(horizontal = 13.dp, vertical = 8.dp)
                                .combinedClickable(
                                    onClick = {
                                        if (message.type == TCDataTypes.MessageType.IMAGE) {
                                            onClick(imageData)
                                        }
                                    },
                                    onLongClick = {
                                        onDeleted(index)
                                    }
                                )
                                .defaultMinSize(minWidth = 120.dp)
                                .wrapContentHeight(),
                            colors = CardDefaults.cardColors().copy(
                                containerColor = if (TCDataTypes.UserType.isThisUser(message.sender)) {
                                    contentProvider.majorThemeColor.value
                                } else {
                                    contentProvider.minorThemeColor.value
                                },
                                contentColor = Color.Black
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 3.dp
                            ),
                            shape = if(message.type == TCDataTypes.MessageType.TEXT && TCDataTypes.UserType.isThisUser(message.sender)){
                                RoundedCornerShape(18, 0, 30, 18)
                            }else if(message.type == TCDataTypes.MessageType.TEXT && !TCDataTypes.UserType.isThisUser(message.sender)){
                                RoundedCornerShape(0, 18, 18, 20)
                            }else{
                                RoundedCornerShape(8)
                            }
                        ) {
                            when (
                                message.type
                            ) {
                                TCDataTypes.MessageType.TEXT -> {
                                    val additionalModifier = if(message.content.length >= 35){
                                        Modifier.fillMaxWidth(0.8f)
                                    }else{
                                        Modifier
                                    }
                                    MarkdownText(
                                        markdown = message.content,
                                        isTextSelectable = true,
                                        linkColor = Color.Blue,
                                        modifier = Modifier
                                            .then(additionalModifier)
                                            .padding(13.dp),
                                        style = LocalTextStyle.current.copy(
                                            color = if (TCDataTypes.UserType.isThisUser(message.sender)) {
                                                contentProvider.textThemeColor.value
                                            } else {
                                                Color.Black
                                            }
                                        )
                                    )
                                }
                                TCDataTypes.MessageType.AUDIO-> {
                                    val isPlaying by audioPlayer.isPlaying.collectAsState()
                                    val error by audioPlayer.error.collectAsState()
                                    Row(
                                        Modifier
                                            .width(TCDataTypes.Fibonacci.ONE_HUNDRED_AND_44.dp)
                                            .wrapContentHeight(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ){
                                        if(isPlaying && contentProvider.currentVoiceNote.value == message.content) {
                                            WaveEffect(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .width(TCDataTypes.Fibonacci.ONE_HUNDRED_AND_44.dp)
                                                    .height(TCDataTypes.Fibonacci.FIFTY_FIVE.dp),
                                            )
                                        }else{

                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .width(TCDataTypes.Fibonacci.ONE_HUNDRED_AND_44.dp),
                                                thickness = 1.dp,
                                                color = contentProvider.textThemeColor.value
                                            )

                                        }

                                        // Handle errors
                                        LaunchedEffect(error) {
                                            error?.let { errorMessage ->
                                                Log.e("AudioPlayerControl", "Error playing audio: $errorMessage")
                                            }
                                        }

                                        if (audioReady) {
                                            IconButton(
                                                onClick = {
                                                    if (!isPlaying) {
                                                        contentProvider.currentVoiceNote.value = message.content
                                                        audioPlayer.playAudio(
                                                            file = audioFile,
                                                            onCompletion = {
                                                                onClick(false)
                                                            },
                                                            onError = { errorMessage ->
                                                                onClick(false)
                                                                Log.e("AudioPlayerControl", "Error playing audio: $errorMessage")
                                                            }
                                                        )
                                                        onClick(true)
                                                    } else {
                                                        audioPlayer.stopAudio()
                                                        onClick(false)
                                                    }
                                                },
                                                modifier = modifier
                                                    .weight(0.3f)
                                                    .padding(0.dp)
                                                    .height(TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                                                    .width(TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlaying && contentProvider.currentVoiceNote.value == message.content) {
                                                        Icons.Default.Stop
                                                    }else{
                                                        Icons.Default.PlayArrow
                                                    },
                                                    tint = contentProvider.textThemeColor.value,
                                                    contentDescription = if (isPlaying) "Stop" else "Play",
                                                    modifier = Modifier.padding(0.dp)
                                                )
                                            }
                                        } else {
                                            Text("Loading...", color = contentProvider.textThemeColor.value)
                                        }
                                    }
                                }
                                TCDataTypes.MessageType.IMAGE -> {
                                    if (imageReady) {
                                        ImageInChat(
                                            img = if (imageData is String) imageData else imgFile,
                                            modifier = Modifier.size(250.dp)
                                        ) {
                                            if (!localImageFound) {
                                                scope.launch {
                                                    withContext(Dispatchers.IO) {
                                                        saveBitmapToFile(
                                                            context = context,
                                                            fileLocation = Databases.Local.IMAGES_DB,
                                                            bitmap = it,
                                                            fileName = message.content + ".png"
                                                        )
                                                    }
                                                    localImageFound = true
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }else{
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(13.dp),){
                            Row(
                                Modifier
                                    .fillMaxWidth(0.5f)
                                    .align(Alignment.Center)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            onDeleted(index)
                                        }
                                    )
                                    .wrapContentHeight()
                                    .background(
                                        color = contentProvider.majorThemeColor.value.copy(alpha = if (message.type == TCDataTypes.MessageType.MESSAGE_USER_BLOCKED) 1f else 0.6f),
                                        shape = RoundedCornerShape(TCDataTypes.Fibonacci.FIVE)
                                    )
                                    .padding(TCDataTypes.Fibonacci.FIVE.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                MarkdownText(
                                    markdown = message.content,
                                    isTextSelectable = false,
                                    linkColor = Color.Blue,
                                    modifier = Modifier
                                        //.basicMarquee()
                                        .wrapContentHeight(),
                                    style = LocalTextStyle.current.copy(
                                        color = contentProvider.textThemeColor.value,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
                if(message.type != TCDataTypes.MessageType.MEDIA_NOTIFICATION && message.type != TCDataTypes.MessageType.MESSAGE_USER_BLOCKED){
                    Row(
                        modifier = Modifier
                            .width(110.dp)

                            .align(Alignment.BottomStart)
                            .padding(top = 19.dp, start = 19.dp, bottom = 3.dp),
                        horizontalArrangement = if(TCDataTypes.UserType.isThisUser(message.sender)) Arrangement.Start else Arrangement.End
                    ) {
                        Text(
                            message.timestamp.split(".")[0],
                            fontSize = 9.sp,
                            color = if (TCDataTypes.UserType.isThisUser(message.sender)) {
                                Color.LightGray
                            } else {
                                Color.Gray
                            },
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }

            if(!TCDataTypes.UserType.isThisUser(message.sender) &&
                message.type != TCDataTypes.MessageType.MEDIA_NOTIFICATION  &&
                message.type != TCDataTypes.MessageType.MESSAGE_USER_BLOCKED
                ){
                Column {
                    IconButton(
                        onClick = {
                            showReactionPicker = !showReactionPicker
                        },
                        modifier = Modifier
                            .size(21.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "emoji selector",
                            tint = if (showReactionPicker) contentProvider.textThemeColor.value else contentProvider.textThemeColor.value.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(21.dp)
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(start = 13.dp, end = 13.dp, top = 13.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if(TCDataTypes.UserType.isThisUser(message.sender)) Arrangement.End else Arrangement.Start
        ){
            if(showReactionPicker){
                reactionList.forEach {
                    Text(
                        text = it.reaction,
                        fontSize = 16.sp
                    )
                }
            }else{
                reactionList.forEach {
                    Text(
                        text = it.reaction,
                        fontSize = 16.sp
                    )
                }
            }
        }
        if(showReactionPicker){
            SimpleAnimator(
                modifier = Modifier.align(
                    alignment = if(TCDataTypes.UserType.isThisUser(message.sender)) Alignment.BottomEnd else Alignment.BottomStart),
                style =AnimationStyle.SCALE_IN_CENTER
            ) {
                Row(
                    modifier = Modifier
                        .align(
                            alignment = if (TCDataTypes.UserType.isThisUser(message.sender)) Alignment.BottomEnd else Alignment.BottomStart
                        )
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = if(TCDataTypes.UserType.isThisUser(message.sender)) Arrangement.End else Arrangement.Start
                ){
                    ReactionPicker(
                        onReactionSelected = {
                            showReactionPicker = false
                            if(!reactionList.contains(ReactionModel(reactor = getUserUid()?:"" ,reaction = it))){
                                reactionList.add(ReactionModel(reactor = getUserUid()?:"" ,reaction = it))
                                onReacted(reactionList.toMutableStateList())
                            }else{
                                reactionList.remove(ReactionModel(reactor = getUserUid()?:"" ,reaction = it))
                                onReacted(reactionList.toMutableStateList())
                            }
                        }
                    )
                }
            }
        }
    }
}

