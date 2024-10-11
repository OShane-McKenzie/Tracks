package com.litecodez.tracksc.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.conversationWatcher
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.toMessageModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConversationService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startConversationMonitoring()
        return START_STICKY
    }

    private fun startConversationMonitoring() {
        lifecycleScope.launch {
            while (true) {
                val chat = contentProvider.currentChat.value
                if (chat != null) {
                    monitorConversation(chat.id)
                }
                delay(1000)
            }
        }
    }

    private suspend fun monitorConversation(chatId: String) {
        conversationWatcher.watch(Databases.Collections.CONVERSATIONS, chatId) { data ->
            if (conversationWatcher.activeListeners.containsKey(chatId)) {
                updateCurrentChat(data)
            } else {
                if(contentProvider.chatIdFromNotification.value.isNullOrEmpty()) {
                    contentProvider.currentChat.value = null
                }
            }
        }
    }

    private fun updateCurrentChat(data: Map<String, Any>) {
        val updatedChat = ChatModel(
            id = data["id"] as String,
            owners = data["owners"] as MutableList<String>,
            admins = data["admins"] as MutableList<String>,
            ownershipModel = data["ownershipModel"] as String,
            mediaLinks = data["mediaLinks"] as MutableList<String>,
            currentMediaLink = data["currentMediaLink"] as String,
            content = (data["content"] as MutableList<Map<String, Any>>).map { it.toMessageModel() }.toMutableList(),
            conversationPhoto = data["conversationPhoto"] as String,
            conversationName = data["conversationName"] as String
        )

        if(Controller.isChatContainerOpen.value && contentProvider.currentChat.value?.id == updatedChat.id) {
            contentProvider.currentChat.value = updatedChat
            contentProvider.currentPlaylist.value = updatedChat.mediaLinks
            if(Controller.initialConversationWatchAcknowledged.value) {
                contentProvider.nowPlaying.value = updatedChat.currentMediaLink
            }
        }
        Controller.reloadMessage.value = !Controller.reloadMessage.value
    }

    override fun onDestroy() {
        super.onDestroy()
        conversationWatcher.stopAllWatchers()
    }
}