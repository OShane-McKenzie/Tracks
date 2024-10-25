package com.litecodez.tracksc.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardOptionKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.chatContainer
import com.litecodez.tracksc.components.ChatList
import com.litecodez.tracksc.components.ExternalOptions
import com.litecodez.tracksc.components.SimpleAnimator
import com.litecodez.tracksc.components.TCImage
import com.litecodez.tracksc.components.WaveEffect
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.Initializer
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.toMap
import dev.jeziellago.compose.markdowntext.MarkdownText


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun HomeScreen(operator: Operator, authenticationManager: AuthenticationManager){

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showExternalOptions by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit){
        if(Controller.isDelayedProfileDocument.value){
            Initializer.initUserProfile(context){}
        }

        if(contentProvider.userTag.value == null){
            contentProvider.userTag.value = TagsModel(
                id = contentProvider.userProfile.value.tag,
                userId = contentProvider.userProfile.value.id,
                name = "${ contentProvider.userProfile.value.firstName } ${ contentProvider.userProfile.value.lastName }",
                type = TCDataTypes.TagType.PERSON,
                photoUrl = contentProvider.userProfile.value.profileImage
            )

            contentRepository.createDocument(
                collectionPath = Databases.Collections.TAGS,
                documentId = contentProvider.userProfile.value.tag,
                data = TagsModel(
                    id = contentProvider.userProfile.value.tag,
                    userId = contentProvider.userProfile.value.id,
                    name = "${ contentProvider.userProfile.value.firstName } ${ contentProvider.userProfile.value.lastName }",
                    type = TCDataTypes.TagType.PERSON,
                    photoUrl = contentProvider.userProfile.value.profileImage
                ).toMap()
            ){
                if(!it.isError){
                    Initializer.initTags(context){}
                }
            }
        }

        Initializer.initServices(context)

        contentProvider.chatIdFromNotification.value.ifNotNull {
            //getToast(context, "ChatId: $it")
            contentProvider.currentChat.value = contentProvider.conversations.value.find { chat ->
                chat.id == it
            }
            contentProvider.currentChat.value.ifNotNull {
                appNavigator.setViewState(chatContainer)
            }
        }
        Controller.isHomeLoaded.value = true
    }

    LaunchedEffect(contentProvider.chatIdFromNotification.value) {
        contentProvider.chatIdFromNotification.value.ifNotNull {
            //getToast(context, "ChatId: $it")
            contentProvider.currentChat.value = contentProvider.conversations.value.find { chat ->
                chat.id == it
            }
            contentProvider.currentChat.value.ifNotNull {
                appNavigator.setViewState(chatContainer)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.tracks_bg_9),
            contentScale = ContentScale.FillBounds,
            contentDescription = ""
        )
        ChatList(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            operator = operator
        )

        //WaveEffect(modifier = Modifier.fillMaxWidth().height(13.dp).align(Alignment.Center))
        if(showExternalOptions){
            SimpleAnimator(
                style = AnimationStyle.DOWN
            ) {
                ExternalOptions(modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter), operator)
            }
        }
        Box(modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()){
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                    .background(
                        color = contentProvider.majorThemeColor.value,
                        shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT)
                    )
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
            ) {
                val userTag = contentProvider.userTag.value

                userTag.ifNotNull {
                    TCImage(
                        img = it.photoUrl,
                        modifier = Modifier
                            .size(TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                            .clip(CircleShape),
                        remoteDatabase = Databases.Buckets.USER_PROFILE_IMAGES
                    )
                    Spacer(modifier = Modifier.width(13.dp))
                    Text(
                        text = it.name,
                        color = contentProvider.textThemeColor.value,
                        modifier = Modifier.basicMarquee()
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    MarkdownText(
                        markdown = it.id,
                        isTextSelectable = true,
                        linkColor = Color.Blue,
                        modifier = Modifier.padding(3.dp),
                        style = LocalTextStyle.current.copy(
                            color = contentProvider.textThemeColor.value
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = TCDataTypes.Fibonacci.TWENTY_ONE.dp)

            ){
                IconButton(onClick = { showExternalOptions = !showExternalOptions }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardOptionKey,
                        contentDescription = "",
                        tint = contentProvider.textThemeColor.value,
                        modifier = Modifier
                            .padding(5.dp)
                            .size(TCDataTypes.Fibonacci.TWENTY_ONE.dp)
                    )
                }
            }
        }
    }
}

