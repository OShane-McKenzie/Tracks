package com.litecodez.tracksc.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.litecodez.tracksc.MainActivity
import com.litecodez.tracksc.R
import com.litecodez.tracksc.appName
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.launchers.ConversationLauncher
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.NotificationModel
import com.litecodez.tracksc.notificationWatcher
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.TCDataTypes
import com.litecodez.tracksc.toListMap
import com.litecodez.tracksc.toNotificationModel
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.launch

class TCNotificationService : LifecycleService() {

    private lateinit var notificationManager: NotificationManager
    private val FOREGROUND_SERVICE_ID = 1001
    private val FOREGROUND_CHANNEL_ID = "TC_FOREGROUND_CHANNEL"

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startNotificationMonitoring()
        return START_STICKY
    }

    private fun setupNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(foregroundChannel)

            val userNotificationChannel = NotificationChannel(
                "TC_NOTIFICATION_CHANNEL",
                appName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(userNotificationChannel)
        }
    }

    private fun startForegroundService() {
        val notification = createForegroundNotification()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FOREGROUND_SERVICE_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification)
        }
    }

    private fun createForegroundNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Tracks Messaging Service")
            .setContentText("Alive")
            .setSmallIcon(R.drawable.tc2)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .build()
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
        val notificationId = getTimeMillis().toInt() // Or generate some unique ID

        val notificationIntent = Intent(this, ConversationLauncher::class.java).apply {
            putExtra("ACTION", chatId)
            putExtra("NOTIFICATION_ID", notificationId)  // Pass notificationId to ConversationLauncher
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(this, "TC_NOTIFICATION_CHANNEL")
            .setSmallIcon(R.drawable.tc2)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)  // This auto-cancels the notification after tap
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