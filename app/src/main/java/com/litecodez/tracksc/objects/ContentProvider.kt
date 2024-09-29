package com.litecodez.tracksc.objects

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseUser
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.LocalImages
import com.litecodez.tracksc.models.NotificationModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.models.TrackConnectionRequestModel
import com.litecodez.tracksc.models.UserModel
import com.litecodez.tracksc.models.Videos

class ContentProvider() {
    val currentUser = mutableStateOf<FirebaseUser?>(null)
    val videos = mutableStateOf(Videos())
    val imageByteArray = mutableStateOf<ByteArray?>(null)
    val userProfile = mutableStateOf<UserModel>(UserModel())
    val tags = mutableStateOf<List<TagsModel>>(listOf())
    val currentChat = mutableStateOf<ChatModel?>(null)
    val conversations = mutableStateOf<List<ChatModel>>(listOf())
    val currentPlaylist = mutableStateOf<List<String>>(emptyList())
    val nowPlaying = mutableStateOf<String>("")
    val incrementer = mutableIntStateOf(0)
    val listOfNotifications = mutableStateOf<List<NotificationModel>>(emptyList())
    val notificationMap = mutableStateOf<Map<String,Int>>(mapOf())
    val localImages = mutableStateOf(LocalImages())
    val loadedImageBytes = mutableStateMapOf<String, ByteArray>()
    val notificationCounter = mutableStateOf(mapOf<String,Int>())
    val requestedConnections = mutableStateOf(listOf<TrackConnectionRequestModel>())


}