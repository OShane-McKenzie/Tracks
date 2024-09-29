package com.litecodez.tracksc.models

data class TrackConnectionRequestModel(
    var senderId:String = "",
    var targetId:String = "",
    var targetType:String = "",
    var targetName:String = ""
)