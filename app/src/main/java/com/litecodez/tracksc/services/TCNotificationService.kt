package com.litecodez.tracksc.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.litecodez.tracksc.MainActivity
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appName
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.notificationWatcher
import com.litecodez.tracksc.objects.ContentProvider
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.stringToUniqueInt
import com.litecodez.tracksc.toListMap
import com.litecodez.tracksc.toNotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TCNotificationService:Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainScope = CoroutineScope(Dispatchers.Main)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun newMessageNotification(title: String, text:String, id:Int = 1, chatID:String = "", notificationManager: NotificationManager){
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.putExtra("ACTION", chatID)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(this, "TC_NOTIFICATION_CHANNEL")
            .setSmallIcon(R.drawable.tc2)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(id, notification)
    }
    private var notificationId = 0
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            "TC_NOTIFICATION_CHANNEL",
            appName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)
        var userId = getUserUid()
        scope.launch {
            while (true){
                if(userId != null){
                    notificationWatcher.watch(Databases.Collections.NOTIFICATIONS, userId!!){ notificationList ->
                        val notifications = (notificationList["notifications"] as MutableList<Map<String, Any>>).map { notification ->
                            notification.toNotificationModel()
                        }

                        contentProvider.listOfNotifications.value = notifications
                        val notificationMap = mutableMapOf<String, Int>()
                        //var updatedChat: ChatModel? = null
                        //var chatIndex = 0
                        if(contentProvider.listOfNotifications.value.isEmpty()) {
                            return@watch
                        }
                        contentProvider.listOfNotifications.value.forEachIndexed { index, notification ->
                            if(!notification.wasRead && notification.type != TCDataTypes.NotificationType.MESSAGE_DELETION) {
                                contentRepository.getChat(chatId = notification.chatId) { updChat ->
                                    // = updChat
                                    if(updChat != null) {
                                        try{
                                            val newMessage =
                                                updChat.content[notification.messageIndex]

                                            notificationMap[updChat.id] = index
                                            val senderName = newMessage.senderName
                                            val chat =
                                                contentProvider.conversations.value.find {
                                                    it.id == notification.chatId
                                                }
                                            if (chat != null) {
                                                val tempList = contentProvider
                                                    .conversations.value
                                                    .toMutableList()
                                                tempList[contentProvider.conversations.value.indexOf(
                                                    chat
                                                )] =
                                                    updChat
                                                mainScope.launch {
                                                    contentProvider.conversations.value =
                                                        tempList.toList()
                                                }

                                            } else {
                                                mainScope.launch {
                                                    contentProvider.conversations.value =
                                                        contentProvider.conversations.value.toMutableList()
                                                            .apply {
                                                                add(updChat)
                                                            }.toList()
                                                }
                                            }
                                            if (contentProvider.currentChat.value?.id != updChat.id) {
                                                newMessageNotification(
                                                    title = senderName,
                                                    text = when (newMessage.type) {
                                                        TCDataTypes.MessageType.TEXT -> {
                                                            newMessage.content
                                                        }

                                                        TCDataTypes.MessageType.IMAGE -> {
                                                            "Sent an image"
                                                        }

                                                        TCDataTypes.MessageType.VIDEO -> {
                                                            "Sent a video"
                                                        }

                                                        else -> {
                                                            "Sent an audio"
                                                        }
                                                    },
                                                    id = newMessage.sender.stringToUniqueInt(),
                                                    chatID = updChat.id,
                                                    notificationManager = notificationManager
                                                )

                                            }else{

                                                return@getChat
                                            }
                                        }catch (e:IndexOutOfBoundsException){
                                            println("Index out of bounds exception $e")
                                            e.printStackTrace()
                                            try {
                                                notification.wasRead = true
                                                val tempList =
                                                    contentProvider.listOfNotifications.value.toMutableList()
                                                tempList[tempList.indexOf(notification)] =
                                                    notification
                                                contentRepository.updateDocument(
                                                    collectionPath = Databases.Collections.NOTIFICATIONS,
                                                    documentId = getUserUid()!!,
                                                    data = mapOf("notifications" to tempList.toListMap())
                                                ){
                                                    success, error ->
                                                }
                                            } catch (e: Exception) {
                                                Log.d("Get notification", "Error getting notification ${e.message}")
                                            }
                                        }
                                    }
                                    Controller.reloadList.value = !Controller.reloadList.value
                                }

                            }else if(
                                notification.type == TCDataTypes.NotificationType.MESSAGE_DELETION &&
                                !notification.wasRead
                            ){
                                contentRepository.getChat(chatId = notification.chatId){ chat ->
                                    //updatedChat = chat

                                    chat.ifNotNull { updChat ->
                                        val localChat = contentProvider.conversations.value.find {
                                            it.id == updChat.id
                                        }

                                        localChat.ifNotNull { lclChat ->
                                            val tempList = contentProvider.conversations.value.toMutableList()
                                            tempList[contentProvider.conversations.value.indexOf(lclChat)] = updChat
                                            contentProvider.conversations.value = tempList.toList()
                                        }

                                        try {
                                            notification.wasRead = true
                                            val tempList =
                                                contentProvider.listOfNotifications.value.toMutableList()
                                            tempList[tempList.indexOf(notification)] =
                                                notification
                                            contentRepository.updateDocument(
                                                collectionPath = Databases.Collections.NOTIFICATIONS,
                                                documentId = getUserUid()!!,
                                                data = mapOf("notifications" to tempList.toListMap())
                                            ){
                                                    success, error ->
                                                Controller.reloadList.value = !Controller.reloadList.value
                                            }
                                        } catch (e: Exception) {
                                            Log.d("Get notification", "Error getting notification ${e.message}")
                                        }

                                    }
                                }
                            }
                            contentProvider.notificationMap.value = notificationMap
                        }
                    }
                    break
                }else{
                    userId = getUserUid()
                    delay(1000)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}