package com.litecodez.tracksc

import androidx.compose.ui.graphics.Color
import com.litecodez.tracksc.objects.TCDataTypes

const val appName: String = "Tracks"
const val login: String = "login"
const val splash: String = "splash"
const val profile:String = "profile"
const val home:String = "home"
const val chatContainer:String = "chatContainer"


const val baseApi:String = "https://oshane-mckenzie.github.io/TracksRepo/app"
const val PLAY = "▶\uFE0F"
const val PLAY_LIST = "\uD83D\uDCC3"
val reactionsList = listOf(
    TCDataTypes.Reactions.HAPPY,
    TCDataTypes.Reactions.HEART,
    TCDataTypes.Reactions.KISS,
    TCDataTypes.Reactions.TONGUE,
    TCDataTypes.Reactions.KOOL,
    TCDataTypes.Reactions.SAD,
    TCDataTypes.Reactions.ANGRY,
    TCDataTypes.Reactions.THUMBS_UP
)