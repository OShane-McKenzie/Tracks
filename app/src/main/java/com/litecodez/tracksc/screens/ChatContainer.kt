package com.litecodez.tracksc.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.chatContainer
import com.litecodez.tracksc.components.ChatBubble
import com.litecodez.tracksc.components.CustomSnackBar
import com.litecodez.tracksc.components.MessageInput
import com.litecodez.tracksc.components.NavigationDrawer
import com.litecodez.tracksc.components.WallpaperSelector
import com.litecodez.tracksc.components.setColorIfDarkTheme
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.conversationWatcher
import com.litecodez.tracksc.deleteFile
import com.litecodez.tracksc.getCurrentDate
import com.litecodez.tracksc.getCurrentTime
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserName
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.keyFor
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.NotificationModel
import com.litecodez.tracksc.models.ReactionModel
import com.litecodez.tracksc.objects.ContentProvider
import com.litecodez.tracksc.objects.ContentRepository
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.MediaDeleteRequest
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.objects.Watchers
import com.litecodez.tracksc.savePreferences
import com.litecodez.tracksc.sendImageMessage
import com.litecodez.tracksc.toByteArray
import com.litecodez.tracksc.toListMap
import com.litecodez.tracksc.toMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

@Composable
fun ChatContainer(modifier: Modifier = Modifier, operator: Operator) {
    val context = LocalContext.current
    val messages = remember { derivedStateOf { contentProvider.currentChat.value?.content ?: listOf(MessageModel()) } }
    var showSnackBar by remember { mutableStateOf(false) }
    val snackBarInfo by remember { mutableStateOf("") }
    var isFirstTimeLaunch by rememberSaveable { mutableStateOf(true) }
    var showDeleteMessageDialog by rememberSaveable { mutableStateOf(false) }
    var selectedMessageIndex by rememberSaveable { mutableIntStateOf(-1) }
    var wasMessageDeleted by rememberSaveable { mutableStateOf(false) }
    var showNavigationDrawer by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var messageListReady by rememberSaveable {
        mutableStateOf(false)
    }
    
    var removeMask by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(true) {
        //getToast(context, "${ contentProvider.currentChat.value?.id }")
        contentProvider.chatIdFromNotification.value = null
    }
    LaunchedEffect(Unit) {
        updateNotificationStatus(contentProvider, contentRepository)
        Controller.isChatContainerOpen.value = true
    }
    LaunchedEffect(Controller.reloadMessage.value) {
        if (wasMessageDeleted && messages.value.isNotEmpty()) {
            listState.animateScrollToItem(index = messages.value.lastIndex)
            wasMessageDeleted = false
        }
        Controller.isChatContainerOpen.value = true
        if (messages.value.isNotEmpty() && isFirstTimeLaunch) {
            delay(200)
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            isFirstTimeLaunch = false
        }
        val isMessageListEmpty = messages.value.isEmpty()
        if (isMessageListEmpty){
            messageListReady = true
        }
        updateNotificationStatus(contentProvider, contentRepository)

    }
    LaunchedEffect(true) {
        delay(1000)
        if(!Controller.initialConversationWatchAcknowledged.value){
            Controller.initialConversationWatchAcknowledged.value = true
            Controller.mediaPlayerReady.value = true
        }
    }
    LaunchedEffect(true) {
        appNavigator.screenTerminationActionsList[chatContainer] = {
            try {
                contentProvider.currentChat.value = null
                Controller.isChatContainerOpen.value = false
                contentProvider.currentChat.value.ifNotNull {
                    conversationWatcher.stopWatcher(it.id)
                }
            } catch (e: Exception) {
                Log.e("Cleanup", "Error disposing of conversation watcher: ${e.message}")
            }
        }
    }
    LaunchedEffect(
        messageListReady
    ) {
        if(messageListReady && messages.value.isNotEmpty()){
            scope.launch {
                listState.scrollToItem(messages.value.lastIndex)
                delay(200)
                listState.scrollToItem(messages.value.lastIndex)
                delay(200)
                removeMask = true
            }
            if(contentProvider.nowPlaying.value.isNotEmpty()){
                Controller.mediaPlayerReady.value = true
            }
        }else if(messages.value.isEmpty()){
            removeMask = true

        }
    }
    Box(modifier = modifier) {

        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(contentProvider.wallpaper.intValue),
            contentScale = ContentScale.FillBounds,
            contentDescription = ""
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.TWENTY_ONE.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.92f)
                    .background(
                        color = Color.White.copy(alpha = 0.0f),
                        shape = RoundedCornerShape(3)
                    )
                    .padding(top = 21.dp),
                state = listState,
                verticalArrangement = Arrangement.Top
            ) {
                itemsIndexed(
                    items = messages.value,
                    key = { _, message -> message.timestamp }
                ) { index, message ->
                    if (index == 0) {
                        Spacer(modifier = Modifier.height(50.dp))
                    }

                    ChatBubbleWrapper(
                        message = message,
                        index = index,
                        onDeleted = {
                            selectedMessageIndex = it
                            showDeleteMessageDialog = true
                        },
                        onReactionUpdated = { reactionList ->
                            updateMessageReactions(contentProvider, operator, index, reactionList)
                        }
                    )

                    if (index == messages.value.lastIndex) {
                        Spacer(modifier = Modifier.height(50.dp))
                        messageListReady = true
                    }
                }

            }
        }

        MessageInput(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onGetAttachment = { imageBitmap ->
                handleImageAttachment(imageBitmap, context, contentProvider, operator, listState, scope)
            }
        ) { message ->
            handleTextMessage(context, message, contentProvider, operator, listState, scope)
        }

        if (showSnackBar) {
            CustomSnackBar(
                info = snackBarInfo,
                containerColor = setColorIfDarkTheme(lightColor = Color.White, darkColor = Color.Black),
                textColor = setColorIfDarkTheme(lightColor = Color.White, darkColor = Color.Black, invert = false),
                isVisible = true,
                duration = 5000
            ) {
                showSnackBar = false
            }
        }

        if (showDeleteMessageDialog) {
            DeleteMessageDialog(
                onDismiss = {
                    showDeleteMessageDialog = false
                    selectedMessageIndex = -1
                },
                onConfirm = {
                    handleMessageDeletion(context, contentProvider, operator, selectedMessageIndex)
                    showDeleteMessageDialog = false
                    selectedMessageIndex = -1
                    wasMessageDeleted = true
                }
            )
        }
        if(!removeMask){

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        NavigationDrawer(
            showDrawer = showNavigationDrawer,
            operator = operator,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxHeight()
                .fillMaxWidth(0.9f)
        ){
            showNavigationDrawer = it
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(TCDataTypes.Fibonacci.EIGHT.dp)
        ){
            Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.TWENTY_ONE.dp))
            IconButton(
                onClick = {
                    showNavigationDrawer = !showNavigationDrawer
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = contentProvider.textThemeColor.value,
                    modifier = Modifier.background(
                        color = contentProvider.majorThemeColor.value.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT.dp)
                    )
                )
            }
        }

        if(Controller.showWallpaperSelector.value){
            WallpaperSelector(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                onDismiss = {
                    Controller.showWallpaperSelector.value = false
                }
            ){
                Controller.showWallpaperSelector.value = false
                contentProvider.wallpaper.intValue = it
                savePreferences(
                    context = context,
                    key = "wallpaper",
                    value = contentProvider.wallpaperMap.keyFor(it)?:"one"
                )
            }
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            cleanupConversation(contentProvider, conversationWatcher)
        }
    }
}

@Composable
private fun ChatBubbleWrapper(
    message: MessageModel,
    index: Int,
    onDeleted: (Int) -> Unit,
    onReactionUpdated: (List<ReactionModel>) -> Unit
) {
    val composition by rememberUpdatedState(message)

    key(message.timestamp) {
        ChatBubble(
            message = composition,
            index = index,
            modifier = Modifier.fillMaxWidth(),
            onDeleted = onDeleted,
            onReacted = onReactionUpdated
        )
    }
}

private fun updateNotificationStatus(
    contentProvider: ContentProvider,
    contentRepository: ContentRepository
) {
    var notificationFound = false
    try {
        // Filter all notifications that match the current chat ID
        val updatedNotifications = contentProvider.listOfNotifications.value.map { notification ->
            if (notification.chatId == contentProvider.currentChat.value?.id) {
                notificationFound = true
                notification.copy(wasRead = true) // Create a copy with updated wasRead field
            } else {
                notification // Leave the notification unchanged if it doesn't match
            }
        }

        // Update the list in the content provider
        contentProvider.listOfNotifications.value = updatedNotifications

        if(notificationFound){
            // Update the document in the repository
            contentRepository.updateDocument(
                collectionPath = Databases.Collections.NOTIFICATIONS,
                documentId = getUserUid()!!,
                data = mapOf("notifications" to updatedNotifications.toListMap())
            )
        }

    } catch (e: Exception) {
        Log.d("Get notification", "Error getting notifications ${e.message}")
    }
}


private fun updateMessageReactions(
    contentProvider: ContentProvider,
    operator: Operator,
    index: Int,
    reactionList: List<ReactionModel>
) {
    contentProvider.currentChat.value?.let { chat ->
        val updatedMessage = chat.content[index].copy(reactions = reactionList.toMutableList())
        chat.content[index] = updatedMessage
        operator.updateConversationOperation(id = chat.id)
    }
}

private fun handleImageAttachment(
    imageBitmap: ImageBitmap?,
    context: Context,
    contentProvider: ContentProvider,
    operator: Operator,
    listState: LazyListState,
    scope: CoroutineScope
) {
    if (imageBitmap != null) {
        val imageName = contentProvider.currentChat.value?.id + getCurrentDate() + getCurrentTime()
        getToast(context = context, msg = "Sending image...", long = true)

        scope.launch(Dispatchers.IO) {
            sendImageMessage(imageName, imageBitmap.toByteArray()) { outcomeModel ->
                if (!outcomeModel.isError) {
                    val newMessage = MessageModel(
                        chatId = contentProvider.currentChat.value!!.id,
                        sender = getUserUid() ?: "",
                        senderName = getUserName(),
                        content = imageName,
                        type = TCDataTypes.MessageType.IMAGE,
                        timestamp = "${getCurrentDate()} ${getCurrentTime()}",
                        reactions = mutableListOf()
                    )

                    contentProvider.currentChat.value?.content?.add(newMessage)
                    Controller.reloadMessage.value = !Controller.reloadMessage.value
                    scope.launch {
                        listState.animateScrollToItem(index = contentProvider.currentChat.value?.content?.lastIndex ?: 0)
                    }
                    
                    operator.sendMessageToStagingOperation(
                        messageModel = newMessage,
                        id = imageName
                    ) { result ->
                        if (result.isError) {
                            getToast(context, "Error sending message: ${result.msg}")
                        }
                    }
                }
            }
        }
    }
}

private fun handleTextMessage(
    context: Context,
    message: String,
    contentProvider: ContentProvider,
    operator: Operator,
    listState: LazyListState,
    scope: CoroutineScope
) {
    val trimmedMessage = message.trim()
    if (trimmedMessage.isNotEmpty()) {
        val newMessage = MessageModel(
            chatId = contentProvider.currentChat.value!!.id,
            sender = getUserUid() ?: "",
            senderName = getUserName(),
            content = trimmedMessage,
            type = TCDataTypes.MessageType.TEXT,
            timestamp = "${getCurrentDate()} ${getCurrentTime()}",
            reactions = mutableListOf()
        )

        contentProvider.currentChat.value?.content?.add(newMessage)
        Controller.reloadMessage.value = !Controller.reloadMessage.value

        scope.launch {
            listState.animateScrollToItem(index = contentProvider.currentChat.value?.content?.lastIndex ?: 0)
        }

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

private fun handleMessageDeletion(context: Context, contentProvider: ContentProvider, operator: Operator, selectedMessageIndex: Int) {
    contentProvider.currentChat.value?.let { chat ->
        if (selectedMessageIndex in chat.content.indices) {

            val currentMessage = chat.content[selectedMessageIndex].copy()
            val messageType = currentMessage.type
            val mediaDeletionRequest = MediaDeleteRequest(
                chatId = chat.id,
                userId = getUserUid()!!,
                mediaId = currentMessage.content + ".png",
                mediaLocation = Databases.Buckets.USER_UPLOADS
            )

            chat.content.removeAt(selectedMessageIndex)
            operator.updateConversationOperation(id = chat.id) { success ->
                if (success) {
                    if(messageType == TCDataTypes.MessageType.IMAGE) {
                        operator.sendMediaDeletionRequest(mediaDeletionRequest){
                            operator.operationScope.launch {
                                try{
                                    deleteFile(context, Databases.Local.IMAGES_DB, currentMessage.content + ".png")
                                }catch (e:IOException){
                                    e.printStackTrace()
                                }
                            }
                        }
                    }

                    chat.owners.forEach { owner ->
                        if(owner != getUserUid()) {
                            operator.operationScope.launch {
                                contentRepository.createDocument(
                                    collectionPath = Databases.Collections.CROSS_NOTIFICATIONS,
                                    documentId = owner,
                                    data = NotificationModel(
                                        chatId = chat.id,
                                        recipientId = owner,
                                        messageIndex = selectedMessageIndex,
                                        type = TCDataTypes.NotificationType.MESSAGE_DELETION,
                                        wasRead = false
                                    ).toMap()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun cleanupConversation(contentProvider: ContentProvider, conversationWatcher: Watchers) {
    try {
        contentProvider.chatIdFromNotification.value = null
        contentProvider.currentChat.value?.let { chat ->
            conversationWatcher.stopWatcher(chat.id)
        }
        contentProvider.currentChat.value = null
        Controller.isChatContainerOpen.value = false
    } catch (e: Exception) {
        Log.e("Cleanup", "Error disposing of conversation watcher: ${e.message}")
    }
}

@Composable
fun DeleteMessageDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete message") },
        text = { Text("Do you want to delete this message?") },
        confirmButton = {
            Text("Delete", color = Color.Red, modifier = Modifier.clickable(onClick = onConfirm))
        },
        dismissButton = {
            Text("Cancel", modifier = Modifier.clickable(onClick = onDismiss))
        }
    )
}