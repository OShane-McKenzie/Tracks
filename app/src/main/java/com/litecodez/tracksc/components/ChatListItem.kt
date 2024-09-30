package com.litecodez.tracksc.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.litecodez.tracksc.R
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserName
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.saveBitmapToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ChatListItem(
    modifier: Modifier = Modifier,
    chatModel: ChatModel,
    index: Int,
    saveChatImage: Boolean = false,
    onClick: (ChatModel) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val getChatModel by remember {
        derivedStateOf {
            contentProvider.conversations.value[index]
        }
    }
    val interlocutor by remember(getChatModel) {
        derivedStateOf {
            //getChatModel.content.find { it.sender != getUserUid() }?.senderName ?: "Unknown"
            contentProvider.tags.value.find{
                getChatModel.owners.contains(it.userId) && it.userId != getUserUid()
            }?.name?:"Unknown"
        }
    }

    val userName by remember { mutableStateOf(getUserName()) }

    var localImageFound by rememberSaveable { mutableStateOf(false) }
    var chatImage by rememberSaveable { mutableStateOf("") }
    var imageReady by rememberSaveable { mutableStateOf(false) }
    var imageData by rememberSaveable { mutableStateOf<Any>("") }

    var notifications by remember {
        mutableIntStateOf(0)
    }
    LaunchedEffect(contentProvider.listOfNotifications.value, contentProvider.conversations.value, Unit) {
        notifications = 0
        //notifications = contentProvider.listOfNotifications.value.count { it.chatId == getChatModel.id && !it.wasRead }
        contentProvider.listOfNotifications.value.forEach {
            if (it.chatId == getChatModel.id && !it.wasRead) {
                notifications++
            }
        }
    }


    LaunchedEffect(getChatModel) {
        chatImage = when (getChatModel.ownershipModel) {
            TCDataTypes.OwnershipType.MULTI -> getChatModel.conversationName
            TCDataTypes.OwnershipType.DUAL -> getChatModel.owners.find { it != getUserUid() } ?: "Unknown"
            else -> getUserUid() ?: "unknown"
        }

        val path = context.filesDir
        val dir = File(path, Databases.Local.IMAGES_DB)
        val imgFile = File(dir, "$chatImage.png")
        localImageFound = imgFile.exists()

        contentRepository.getImageUrl(
            bucket = Databases.Buckets.USER_PROFILE_IMAGES,
            imageName = "$chatImage.png"
        ) { success, url ->
            if (success) {
                imageData = url
                imageReady = true
            } else {
                getToast(context, "Image not found $url", long = true)
            }
        }
    }

    Box(Modifier.padding(3.dp)) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = contentProvider.minorThemeColor.value,
                    shape = RoundedCornerShape(5)
                )
                .padding(3.dp)
                .defaultMinSize(minHeight = 80.dp)
                .clickable {
                    val chatIndex = contentProvider.conversations.value.find { it.id == getChatModel.id }?.let {
                        contentProvider.conversations.value.indexOf(it)
                    }?:index
                    contentProvider.currentChat.value = contentProvider.conversations.value[chatIndex]
                    onClick(contentProvider.currentChat.value!!)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SimpleAnimator(style = AnimationStyle.LEFT) {
                ImageInChat(
                    img = imageData,
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(TCDataTypes.Fibonacci.FIVE),
                    contentScale = ContentScale.FillBounds,
                    defaultImage = R.drawable.user
                ) { bitmap ->
                    if (saveChatImage && !localImageFound) {
                        scope.launch(Dispatchers.IO) {
                            saveBitmapToFile(
                                context = context,
                                fileLocation = Databases.Local.IMAGES_DB,
                                bitmap = bitmap,
                                fileName = "$chatImage.png"
                            )
                            localImageFound = true
                        }
                    }
                }
            }

            Column(modifier = Modifier.wrapContentSize()) {
                Text(
                    text = when (getChatModel.ownershipModel) {
                        TCDataTypes.OwnershipType.MULTI -> getChatModel.conversationName
                        TCDataTypes.OwnershipType.DUAL -> interlocutor
                        else -> userName
                    },
                    fontSize = 20.sp,
                    maxLines = 1,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(5.dp))

                // Use key parameter to force recomposition when the last message changes
                key(getChatModel.content.lastOrNull()) {
                    when (getChatModel.content.lastOrNull()?.type) {
                        TCDataTypes.MessageType.TEXT -> {
                            SimpleAnimator(
                                style = AnimationStyle.UP
                            ) {
                                Text(
                                    text = getChatModel.content.lastOrNull()?.content ?: "",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                            }
                        }
                        TCDataTypes.MessageType.IMAGE -> {
                            SimpleAnimator(
                                style = AnimationStyle.UP
                            ) {
                                Text(
                                    text = "\uD83D\uDCF7",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                        }
                        TCDataTypes.MessageType.VIDEO -> {
                            SimpleAnimator(
                                style = AnimationStyle.UP
                            ) {
                                Text(
                                    text = "\uD83C\uDFA5",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = contentProvider.textThemeColor.value
                                )
                            }
                        }
                        TCDataTypes.MessageType.AUDIO -> {
                            SimpleAnimator(
                                style = AnimationStyle.UP
                            ) {
                                Text(
                                    text = "\uD83C\uDFA7",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = contentProvider.textThemeColor.value
                                )
                            }
                        }
                        null -> {
                            SimpleAnimator(
                                style = AnimationStyle.UP
                            ) {
                                Text(
                                    text = "No messages",
                                    fontWeight = FontWeight.Thin,
                                    fontStyle = FontStyle.Italic,
                                    color = contentProvider.textThemeColor.value
                                )
                            }
                        }
                    }
                }
            }
        }
        if (notifications > 0) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.Red, shape = CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    "$notifications",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}