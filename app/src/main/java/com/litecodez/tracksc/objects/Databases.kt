package com.litecodez.tracksc.objects

import com.litecodez.tracksc.models.AudioRecorder

object Databases {
    object Collections{
        const val USERS = "users"
        const val STAGING = "staging"
        const val NOTIFICATIONS = "notifications"
        const val CONVERSATIONS = "conversations"
        const val TAGS = "tags"
        const val GROUP_MANAGEMENT = "groupManagement"
        const val CHAT_MANAGEMENT = "chatManagement"
        const val MEDIA_MANAGEMENT = "mediaManagement"
        const val CROSS_NOTIFICATIONS = "crossNotifications"
        const val ESTABLISHED_CONNECTIONS = "establishedConnections"
        const val CONNECTION_REQUESTS = "connectionRequests"
        const val CONNECTION_REQUEST_OUTCOME = "connectionRequestOutcome"
        const val RESTRICTIONS = "restrictions"
        const val ACCOUNT_DELETION_REQUESTS = "accountDeletionRequests"
    }

    object Documents{
        const val VIDEOS = "videos"
        const val DELETE_MEDIA = "deleteMedia"
        const val NOTIFICATIONS = "notifications"
    }

    object Fields{
        const val DELETION_LIST = "deletionList"
        const val REQUESTS = "requests"
        const val GROUPS = "groups"
        const val PEOPLE = "people"
        const val BLOCKED_USERS = "blockedUsers"
    }

    object Buckets{
        const val USER_PROFILE_IMAGES = "userProfileImages"
        const val USER_UPLOADS = "userUploads"
        const val AUDIO = "audio"
    }

    object Local {
        const val MEDIA_DB = "media.db"
        const val IMAGES_DB = "$MEDIA_DB/images"
        const val AUDIO_DB = "$MEDIA_DB/audio"
        const val IMAGE_DATA = "images.json"
    }
}