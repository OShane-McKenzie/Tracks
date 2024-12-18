package com.litecodez.tracksc.objects

import android.nfc.Tag
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseUser
import com.litecodez.tracksc.R
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.LocalImages
import com.litecodez.tracksc.models.NotificationModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.models.TrackConnectionRequestModel
import com.litecodez.tracksc.models.UserModel
import com.litecodez.tracksc.models.Values
import com.litecodez.tracksc.models.Video
import com.litecodez.tracksc.models.Videos

class ContentProvider {

    val currentUser = mutableStateOf<FirebaseUser?>(null)
    val videos = mutableStateOf(Videos())
    val imageByteArray = mutableStateOf<ByteArray?>(null)
    val userProfile = mutableStateOf<UserModel>(UserModel())
    val tags = mutableStateOf<List<TagsModel>>(listOf())
    val userTag = mutableStateOf<TagsModel?>(null)
    val currentChat = mutableStateOf<ChatModel?>(null)
    val conversations = mutableStateOf<List<ChatModel>>(listOf())
    val currentPlaylist = mutableStateOf<List<String>>(emptyList())
    val nowPlaying = mutableStateOf<String>("")
    val incrementer = mutableIntStateOf(0)
    val listOfNotifications = mutableStateOf<List<NotificationModel>>(emptyList())
    val notificationMap = mutableStateOf<Map<String,Int>>(mapOf())
    val requestedConnections = mutableStateOf(listOf<TrackConnectionRequestModel>())
    val majorThemeColor = mutableStateOf(Color(0xFFFB8C00))
    val minorThemeColor = mutableStateOf(Color(0xFFFFE0B2))
    val textThemeColor = mutableStateOf(Color(0xFF000000))
    val listOfConnectionRequests = mutableStateOf(listOf<TagsModel>())
    val currentSong = mutableStateOf<Video?>(null)
    val playerState = mutableIntStateOf(-1)
    val values = mutableStateOf<Values>(Values())

    val wallpaperMap = mapOf(
        "one" to R.drawable.tracks_bg_1,
        "two" to R.drawable.tracks_bg_2,
        "three" to R.drawable.tracks_bg_3,
        "four" to R.drawable.tracks_bg_4,
        "five" to R.drawable.tracks_bg_5,
        "six" to R.drawable.tracks_bg_6,
        "seven" to R.drawable.tracks_bg_7,
        "eight" to R.drawable.tracks_bg_8
    )

    val wallpaper = mutableIntStateOf(R.drawable.tracks_bg_2)
    val chatIdFromNotification = mutableStateOf<String?>(null)
    val restrictedUsers = mutableStateOf<List<String>>(listOf())
    val playlistAutoplayEnabledDisabledRegister = mutableStateOf<List<String>>(listOf())
    val currentVoiceNote = mutableStateOf<String>("")
    val deleteAccountMessage = mutableStateOf<String>("Deleting Account...")
    val termsOfService = mutableStateOf("")
    val privacyPolicy = mutableStateOf("")
    val help = mutableStateOf("")
}