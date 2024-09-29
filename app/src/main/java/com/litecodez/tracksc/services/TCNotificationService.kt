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
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.litecodez.tracksc.MainActivity
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appName
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.NotificationModel
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

class TCNotificationService : LifecycleService() {

    private lateinit var notificationManager: NotificationManager



    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startNotificationMonitoring()
        return START_STICKY
    }

    private fun setupNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "TC_NOTIFICATION_CHANNEL",
            appName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startNotificationMonitoring() {
        lifecycleScope.launch {
            getUserUid()?.let { userId ->
                monitorNotifications(userId)
            }
        }
    }

    private suspend fun monitorNotifications(userId: String) {
        notificationWatcher.watch(Databases.Collections.NOTIFICATIONS, userId) { notificationList ->
            val notifications = (notificationList["notifications"] as? List<Map<String, Any>>)?.map { it.toNotificationModel() } ?: return@watch
            contentProvider.listOfNotifications.value = notifications
            processNotifications(notifications)
        }
    }

    private fun processNotifications(notifications: List<NotificationModel>) {
        val notificationMap = mutableMapOf<String, Int>()
        notifications.forEachIndexed { index, notification ->
            when {
                !notification.wasRead && notification.type != TCDataTypes.NotificationType.MESSAGE_DELETION ->
                    processNewMessage(notification, index, notificationMap)
                notification.type == TCDataTypes.NotificationType.MESSAGE_DELETION && !notification.wasRead ->
                    processMessageDeletion(notification)
            }
        }
        contentProvider.notificationMap.value = notificationMap
    }

    private fun processNewMessage(notification: NotificationModel, index: Int, notificationMap: MutableMap<String, Int>) {
        contentRepository.getChat(chatId = notification.chatId) { updatedChat ->
            updatedChat?.let { chat ->
                try {
                    val newMessage = chat.content[notification.messageIndex]
                    notificationMap[chat.id] = index
                    updateConversationList(chat)
                    if (contentProvider.currentChat.value?.id != chat.id) {
                        showNewMessageNotification(newMessage, chat.id)
                    }else if (!Controller.isChatContainerOpen.value) {
                        showNewMessageNotification(newMessage, chat.id)
                    }
                } catch (e: IndexOutOfBoundsException) {
                    handleNotificationError(notification, e)
                }
            }
            Controller.reloadList.value = !Controller.reloadList.value
        }
    }

    private fun processMessageDeletion(notification: NotificationModel) {
        contentRepository.getChat(chatId = notification.chatId) { chat ->
            chat?.let { updatedChat ->
                updateConversationList(updatedChat)
                markNotificationAsRead(notification)
            }
        }
    }

    private fun updateConversationList(updatedChat: ChatModel) {
        val conversations = contentProvider.conversations.value.toMutableList()
        val index = conversations.indexOfFirst { it.id == updatedChat.id }
        if (index != -1) {
            conversations[index] = updatedChat
        } else {
            conversations.add(updatedChat)
        }
        contentProvider.conversations.value = conversations
    }

    private fun showNewMessageNotification(message: MessageModel, chatId: String) {
        val title = message.senderName
        val text = when (message.type) {
            TCDataTypes.MessageType.TEXT -> message.content
            TCDataTypes.MessageType.IMAGE -> "Sent an image"
            TCDataTypes.MessageType.VIDEO -> "Sent a video"
            else -> "Sent an audio"
        }
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("ACTION", chatId)
        }
        val notificationId = message.sender.stringToUniqueInt()
        val pendingIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, "TC_NOTIFICATION_CHANNEL")
            .setSmallIcon(R.drawable.tc2)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    private fun handleNotificationError(notification: NotificationModel, error: Exception) {
        error.printStackTrace()
        markNotificationAsRead(notification)
    }

    private fun markNotificationAsRead(notification: NotificationModel) {
        notification.wasRead = true
        val updatedNotifications = contentProvider.listOfNotifications.value.toMutableList()
        val index = updatedNotifications.indexOfFirst { it == notification }
        if (index != -1) {
            updatedNotifications[index] = notification
            contentRepository.updateDocument(
                collectionPath = Databases.Collections.NOTIFICATIONS,
                documentId = getUserUid() ?: return,
                data = mapOf("notifications" to updatedNotifications.toListMap())
            ) { success, error ->
                if (!success) {
                    error?.let { Log.e("TCNotificationService", "Error updating notification: ${it.message}") }
                }
                Controller.reloadList.value = !Controller.reloadList.value
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationWatcher.stopAllWatchers()
    }
}