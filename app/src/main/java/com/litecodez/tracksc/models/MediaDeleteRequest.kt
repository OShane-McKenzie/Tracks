package com.litecodez.tracksc.models

data class MediaDeleteRequest(
    var chatId:String = "",
    var userId:String = "",
    var mediaId:String = "",
    var mediaLocation:String = "",
)
