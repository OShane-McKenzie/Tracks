package com.litecodez.tracksc.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getCurrentDate
import com.litecodez.tracksc.getCurrentTime
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserName
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.RestrictionType
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.registerEnableDisablePlaylistAutoplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoreOptions(modifier: Modifier = Modifier, operator: Operator,onDismiss: (Boolean)->Unit = {}){
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Column(
        modifier = modifier
            .padding(TCDataTypes.Fibonacci.EIGHT.dp)
            .fillMaxSize()
            .background(
                color = contentProvider.majorThemeColor.value.copy(alpha = 0.7f),
                shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                onDismiss(false)
            }
        ){
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close",
                tint = contentProvider.textThemeColor.value)
        }
        contentProvider.currentChat.value.ifNotNull { chat ->
            val interlocutor = chat.owners.find { it != getUserUid()!! }?:""
            val interlocutorName = contentProvider.tags.value.find { it.userId == interlocutor }?.name?:""
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TCDataTypes.Fibonacci.EIGHT.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                var isUserBlocked = contentProvider.restrictedUsers.value.contains(
                    interlocutor
                )
                LaunchedEffect(Controller.reloadRestrictions){
                    isUserBlocked = contentProvider.restrictedUsers.value.contains(
                        interlocutor
                    )
                }
                Button(
                    onClick = {
                        if(chat.ownershipModel == TCDataTypes.OwnershipType.DUAL){
                            operator.restrictUserOperation(id = interlocutor, RestrictionType.BLOCK){ response ->
                                if(response){
                                    val newMessage = MessageModel(
                                        chatId = chat.id,
                                        sender = getUserUid() ?: "",
                                        senderName = getUserName(),
                                        content = "${getUserName()} blocked $interlocutorName\n" +
                                                "$interlocutorName can still receive messages from ${getUserName()} unless they also block ${getUserName()}",
                                        type = TCDataTypes.MessageType.MESSAGE_USER_BLOCKED,
                                        timestamp = "${getCurrentDate()} ${getCurrentTime()}",
                                        reactions = mutableListOf()
                                    )
                                    operator.sendMessageToStagingOperation(
                                        messageModel = newMessage,
                                        id = chat.id + getCurrentDate() + getCurrentTime()
                                    ) { result ->
                                        if (result.isError) {
                                            scope.launch(Dispatchers.Main) {
                                                getToast(context, "Error sending message")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = contentProvider.textThemeColor.value,
                        contentColor = contentProvider.majorThemeColor.value),
                    enabled = !isUserBlocked,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.4f)
                ) {
                    Text(text = "Block ${contentProvider.tags.value.find { it.userId == interlocutor }?.name}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.basicMarquee()
                    )
                }
                Button(
                    onClick = {
                        if(chat.ownershipModel == TCDataTypes.OwnershipType.DUAL){
                            operator.restrictUserOperation(id = interlocutor, RestrictionType.UNBLOCK)
                        }
                    },
                    colors = ButtonDefaults.buttonColors().copy(
                        containerColor = contentProvider.textThemeColor.value,
                        contentColor = contentProvider.majorThemeColor.value),
                    enabled = isUserBlocked,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.4f)
                ) {
                    Text(text = "Unblock ${contentProvider.tags.value.find { it.userId == interlocutor }?.name}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.basicMarquee()
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
        ){
            Text("Play List", color = contentProvider.textThemeColor.value)
            IconButton(onClick = {
                getToast(context, "Saving playlist, please wait...")
                contentProvider.currentChat.value.ifNotNull {  chat ->
                    operator.updateConversationOperation(id = chat.id, updateMedia = true){
                        if(it){
                            getToast(context, "Playlist saved.")
                        }else{
                            getToast(context, "Error saving playlist.")
                        }
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = contentProvider.textThemeColor.value
                )
            }
        }
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .height(TCDataTypes.Fibonacci.TWO_HUNDRED_AND_33.dp)
                .border(
                    width = 2.dp,
                    shape = RoundedCornerShape(8.dp),
                    color = contentProvider.textThemeColor.value
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            contentProvider.currentPlaylist.value.forEach { videoId ->
                val video = contentProvider.videos.value.videos.find { it.id == videoId}
                video.ifNotNull { videoItem ->
                    VideoListItem(video = videoItem){
                        contentProvider.nowPlaying.value = it
                        contentProvider.currentChat.value.ifNotNull {  chat ->
                            operator.updateConversationOperation(id = chat.id, updateMedia = true)
                        }
                        Controller.mediaPlayerReady.value = true
                    }
                }
            }
        }
        Controller.isPlayListEnabled.value.let {
            Button(
                onClick = {
                    contentProvider.currentChat.value.ifNotNull {
                        registerEnableDisablePlaylistAutoplay(
                            it.id,
                            !contentProvider.playlistAutoplayEnabledDisabledRegister.value.contains(it.id)
                        )
                    }

                    if(Controller.isPlayListEnabled.value) {
                        contentProvider.playerState.intValue = 0
                    }
                  },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = contentProvider.textThemeColor.value,
                    contentColor = contentProvider.majorThemeColor.value
                )
            ){
                Text(text = if(it)"Disable Playlist Autoplay" else "Enable Playlist Autoplay")
            }
        }
    }
}