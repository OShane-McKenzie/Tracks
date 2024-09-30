package com.litecodez.tracksc.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.lightPink
import com.litecodez.tracksc.lightPurple
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
               onDeleted: (Int) -> Unit = {},
               onReacted: (MutableList<ReactionModel>) -> Unit = {}
               ){
    var imageData by rememberSaveable {
        mutableStateOf<Any>("")
    }
    var imageReady by rememberSaveable {mutableStateOf(false)}
    var localImageFound by rememberSaveable {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    val reactionList = remember{ mutableStateListOf<ReactionModel>() }

    var showReactionPicker by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val path = context.filesDir
        val dir = File(path, Databases.Local.IMAGES_DB)
        val imgFile = File(dir, message.content + ".png")
        localImageFound = imgFile.exists()
        reactionList.clear()
        reactionList.addAll(message.reactions)
        if(!imageReady) {
            if (message.type == TCDataTypes.MessageType.IMAGE && !localImageFound) {
                contentRepository.getImageUrl(
                    bucket = Databases.Buckets.USER_UPLOADS,
                    imageName = message.content + ".png"
                ) { success, url ->
                    if (success) {
                        imageData = url
                        imageReady = true

                    } else {
                        getToast(context, "Image not found $url", long = true)
                    }
                }
            } else if (message.type == TCDataTypes.MessageType.IMAGE) {
                if (imgFile.exists()) {
                    imageData = BitmapFactory.decodeFile(imgFile.absolutePath)
                    imageReady = true
                }
            }
        }
    }

    LaunchedEffect(Controller.reloadMessage.value) {
        delay(200)
        reactionList.clear()
        reactionList.addAll(message.reactions)
    }

    Box(
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if(TCDataTypes.UserType.isThisUser(message.sender)) Arrangement.End else Arrangement.Start
        ){
            if(TCDataTypes.UserType.isThisUser(message.sender)){
                Column(){
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
                            tint = if (showReactionPicker) Color.Blue else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(21.dp)
                        )
                    }
                }
            }
            Box(){
                SimpleAnimator(style = AnimationStyle.UP){
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 13.dp, vertical = 8.dp)
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    onDeleted(index)
                                }
                            )
                            .defaultMinSize(minWidth = 100.dp)
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
                        shape = RoundedCornerShape(5)
                    ) {
                        when (
                            message.type
                        ) {
                            TCDataTypes.MessageType.TEXT -> {
                                MarkdownText(
                                    markdown = message.content,
                                    isTextSelectable = true,
                                    linkColor = Color.Blue,
                                    modifier = Modifier.padding(13.dp),
                                    style = LocalTextStyle.current.copy(
                                        color = if (TCDataTypes.UserType.isThisUser(message.sender)){
                                            contentProvider.textThemeColor.value
                                        }else{
                                            Color.Black
                                        }
                                    )
                                )
                            }

                            TCDataTypes.MessageType.IMAGE -> {
                                if (imageReady) {
                                    ImageInChat(img = imageData, modifier = Modifier.size(250.dp)) {
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
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(top = 19.dp, start = 19.dp, bottom = 3.dp)
                ){
                    Text(
                        message.timestamp.split(".")[0],
                        fontSize = 9.sp,
                        color = if (TCDataTypes.UserType.isThisUser(message.sender)){
                            Color.LightGray
                        }else{
                            Color.Gray
                        }
                    )
                }
            }

            if(!TCDataTypes.UserType.isThisUser(message.sender)){
                Column(){
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
                            tint = if (showReactionPicker) Color.Blue else Color.Gray.copy(alpha = 0.5f),
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