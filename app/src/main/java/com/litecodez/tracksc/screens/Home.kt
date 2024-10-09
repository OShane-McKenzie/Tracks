package com.litecodez.tracksc.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.chatContainer
import com.litecodez.tracksc.components.ChatList
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.Initializer
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.toMap


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun HomeScreen(operator: Operator, authenticationManager: AuthenticationManager){

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit){
        if(Controller.isDelayedProfileDocument.value){
            Initializer.initUserProfile(context){}
        }
        val userTag = contentProvider.tags.value.find { it.id == contentProvider.userProfile.value.tag }
        if(userTag == null){
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
    }
}

