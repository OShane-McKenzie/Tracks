package com.litecodez.tracksc.models

data class NotificationModel(
    var recipientId: String = "",
    var chatId: String = "",
    var messageIndex: Int = 0,
    var wasRead: Boolean = false,
    var type: String = ""
)