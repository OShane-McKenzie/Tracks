package com.litecodez.tracksc.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.conversationWatcher
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.toMessageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConversationService: Service() {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            while (true) {
                if (contentProvider.currentChat.value != null) {
                    val chatId = contentProvider.currentChat.value!!.copy().id
                    withContext(Dispatchers.Main){
                        conversationWatcher.watch(
                            Databases.Collections.CONVERSATIONS,
                            chatId
                        ){ data ->
                            if(conversationWatcher.activeListeners.keys.contains(chatId)) {
                                contentProvider.currentChat.value = ChatModel(
                                    id = data["id"] as String,
                                    owners = data["owners"] as MutableList<String>,
                                    admins = data["admins"] as MutableList<String>,
                                    ownershipModel = data["ownershipModel"] as String,
                                    mediaLinks = data["mediaLinks"] as MutableList<String>,
                                    currentMediaLink = data["currentMediaLink"] as String,
                                    content = (data["content"] as MutableList<Map<String, Any>>)
                                        .map { it.toMessageModel() }.toMutableList(),
                                    conversationPhoto = data["conversationPhoto"] as String,
                                    conversationName = data["conversationName"] as String
                                ).apply {
                                    contentProvider.currentPlaylist.value = this.mediaLinks
                                    contentProvider.nowPlaying.value = this.currentMediaLink
                                    Controller.reloadMessage.value = !Controller.reloadMessage.value
                                }
                            }else{
                                contentProvider.currentChat.value = null
                                return@watch
                            }
                        }
                    }
                    //break
//                    delay(4000)
//                    mainScope.launch {
//                        getToast(this@ConversationService, chatId)
//                    }
                }else{
//                    delay(4000)
//                    mainScope.launch {
//                        getToast(this@ConversationService, "No chat selected")
//                    }
                    //Controller.reloadMessage.value = !Controller.reloadMessage.value
                    continue
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}