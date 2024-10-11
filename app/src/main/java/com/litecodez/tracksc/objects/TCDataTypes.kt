package com.litecodez.tracksc.objects

import com.litecodez.tracksc.getUserUid

object TCDataTypes {
    object OwnershipType{
        const val DUAL = "dual"
        const val MULTI = "multi"
        const val SINGLE = "single"
    }
    object MessageType{
        const val TEXT = "text"
        const val VIDEO = "video"
        const val IMAGE = "image"
        const val AUDIO = "audio"
        const val MEDIA_NOTIFICATION = "media_notification"
    }

    object UserType{
        const val USER = "user"
        const val ADMIN = "admin"
        fun isThisUser (id:String):Boolean{
            return id == getUserUid()
        }
    }
    // List Fibonacci sequence from 1 to 900
    // 1 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987
    object Fibonacci{
        const val ONE = 1
        const val TWO = 2
        const val THREE = 3
        const val FIVE = 5
        const val EIGHT = 8
        const val TWELVE = 13
        const val TWENTY_ONE = 21
        const val FIFTY_FIVE = 55
        const val EIGHTY_NINE = 89
        const val ONE_HUNDRED_AND_44 = 144
        const val TWO_HUNDRED_AND_33 = 233
        const val THREE_HUNDRED_AND_77 = 377
        const val SIX_HUNDRED_AND_10 = 610
        const val NINE_HUNDRED_AND_87 = 987
    }

    object NotificationType {
        const val MESSAGE_DELETION = "message_deletion"
        const val MESSAGE_EDIT = "message_edit"
        const val MESSAGE_REPLY = "message_reply"
        const val MESSAGE_MENTION = "message_mention"
        const val MESSAGE_LIKE = "message_like"
        const val MESSAGE_REACTION = "message_reaction"
        const val MESSAGE_RECEIVED = "message_received"
        const val MESSAGE_DENIED = "message_denied"
    }
    object TagType {
        const val GROUP = "Group"
        const val PERSON = "Person"
    }
    object Reactions{
        const val HAPPY = "\uD83D\uDE03"
        const val HEART = "❤\uFE0F"
        const val KISS = "\uD83D\uDE18"
        const val TONGUE = "\uD83D\uDE1B"
        const val KOOL = "\uD83D\uDE0E"
        const val SAD = "\uD83D\uDE14"
        const val ANGRY = "\uD83D\uDE21"
        const val THUMBS_UP = "\uD83D\uDC4D"
    }
}