package com.litecodez.tracksc.models

data class MessageModel(
    var chatId:String = "",
    var sender: String = "",
    var senderName: String = "",
    var type: String = "",
    var content: String = "",
    var timestamp: String = "",
    var reactions: MutableList<ReactionModel> = mutableListOf()
)
