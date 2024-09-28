package com.litecodez.tracksc.models

import com.litecodez.tracksc.objects.HasId

data class ChatModel(
    override var id: String = "",
    var owners: MutableList<String> = mutableListOf(),
    var admins: MutableList<String> = mutableListOf(),
    var ownershipModel: String = "",
    var mediaLinks: MutableList<String> = mutableListOf(),
    var currentMediaLink: String = "",
    var content: MutableList<MessageModel> = mutableListOf(),
    var conversationPhoto: String = "",
    var conversationName: String = "",
):HasId
