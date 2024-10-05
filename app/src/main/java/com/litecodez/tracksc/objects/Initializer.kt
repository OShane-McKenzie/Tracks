package com.litecodez.tracksc.objects

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.services.ConversationService
import com.litecodez.tracksc.services.TCConnectionService
import com.litecodez.tracksc.services.TCNotificationService
import com.litecodez.tracksc.then
import com.litecodez.tracksc.toMessageModel
import com.litecodez.tracksc.toUserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object Initializer {
    private val tagsList = mutableListOf<TagsModel>()
    private var isFullyInitialized = mutableStateOf(false)
    private var processCounter = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun runInit(context: Context, isFirstTimeLogin:Boolean = false, callBack: ()->Unit = {}){
        contentRepository.getAllDocuments(Databases.Collections.TAGS,onFailure = {}){
            it?.forEach {
                try {
                    TagsModel().apply {
                        this.id = it.data?.get("id")?.toString() ?: ""
                        this.userId = it.data?.get("userId")?.toString() ?: ""
                        this.name = it.data?.get("name")?.toString() ?: ""
                        this.type = it.data?.get("type")?.toString() ?: ""
                        tagsList.add(this)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }?.then {
                contentProvider.tags.value = tagsList
            }
        }

        val document = getUserUid()?: ""
        if(document.isNotEmpty()){
            contentRepository.getDocument(Databases.Collections.USERS, document){
                println("isFullyInitialized0: ${isFullyInitialized.value}")
                try {
                    if(!isFirstTimeLogin){
                        contentProvider.userProfile.value = it!!.data!!.toUserModel()
                    }else{
                        Controller.isDelayedProfileDocument.value = true
                    }
                    isFullyInitialized.value = true
                    println("isFullyInitialized1: ${isFullyInitialized.value}")
                    processCounter++
                }catch (e:Exception){
                    getToast(
                        context,
                        "Restart the app or check internet "+e.message,
                        long = true
                    )
                    isFullyInitialized.value = false
                    println("isFullyInitialized2: ${isFullyInitialized.value}")
                    processCounter++
                }
            }
        }else{
            isFullyInitialized.value = false
            println("isFullyInitialized4: ${isFullyInitialized.value}")
            getToast(
                context,
                "Restart the app or check internet",
                long = true
            )
            processCounter++
        }

        contentRepository.getAllUserConversationDocuments(Databases.Collections.CONVERSATIONS){ documentSnapshots ->
            val conversations = mutableListOf<ChatModel>()
            documentSnapshots?.forEach { document ->
                try {
                    conversations.add(
                        ChatModel().apply {
                            this.id = document.data!!["id"] as String
                            this.owners = document.data!!["owners"] as MutableList<String>
                            this.admins = document.data!!["admins"] as MutableList<String>
                            this.ownershipModel = document.data!!["ownershipModel"] as String
                            this.mediaLinks = document.data!!["mediaLinks"] as MutableList<String>
                            this.currentMediaLink = document.data!!["currentMediaLink"] as String
                            this.content = (document.data!!["content"] as MutableList<Map<String, Any>>)
                                .map { it.toMessageModel() }.toMutableList()
                            this.conversationPhoto = document.data!!["conversationPhoto"] as String
                            this.conversationName = document.data!!["conversationName"] as String
                        }
                    )

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            contentProvider.conversations.value = conversations
        }

        scope.launch {
            while (true) {
                if(processCounter <1){
                    continue
                }else{
                    callBack()
                    println("isFullyInitialized8: ${isFullyInitialized.value}")
                    break
                }
            }
        }
    }

    fun initUserProfile(context: Context, callBack: (Boolean)->Unit = {}){
        val document = getUserUid()?: ""
        if(document.isNotEmpty()){
            contentRepository.getDocument(Databases.Collections.USERS, document){
                try {
                    contentProvider.userProfile.value = it!!.data!!.toUserModel()
                    Controller.isDelayedProfileDocument.value = false
                }catch (e:Exception){
                    getToast(
                        context,
                        "Restart the app or check internet "+e.message,
                        long = true
                    )

                }finally {
                    callBack.invoke(Controller.isDelayedProfileDocument.value)
                }
            }
        }else{
            getToast(
                context,
                "Restart the app or check internet",
                long = true
            )
        }
    }
    fun initTags(context: Context, callBack: (Boolean)->Unit = {}){
        contentRepository.getAllDocuments(Databases.Collections.TAGS,onFailure = {}){
            it?.forEach {
                try {
                    TagsModel().apply {
                        this.id = it.data?.get("id")?.toString() ?: ""
                        this.userId = it.data?.get("userId")?.toString() ?: ""
                        this.name = it.data?.get("name")?.toString() ?: ""
                        this.type = it.data?.get("type")?.toString() ?: ""
                        tagsList.add(this)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }?.then {
                contentProvider.tags.value = tagsList
            }
        }
    }

    fun initServices(context: Context){
        if(!Controller.isServiceInitialized.value) {
            val activity = context as Activity
            val intent = Intent(activity, TCNotificationService::class.java)
            context.startService(intent)
            Controller.isServiceInitialized.value = true
        }
        if (!Controller.isService2Initialized.value) {
            val activity = context as Activity
            val intent2 = Intent(activity, ConversationService::class.java)
            context.startService(intent2)
            Controller.isService2Initialized.value = true
        }

        if (!Controller.isService3Initialized.value) {
            val activity = context as Activity
            val intent2 = Intent(activity, TCConnectionService::class.java)
            context.startService(intent2)
            Controller.isService3Initialized.value = true
        }
    }

    fun initConversations(){
        contentRepository.getAllUserConversationDocuments(Databases.Collections.CONVERSATIONS){ documentSnapshots ->
            val conversations = mutableListOf<ChatModel>()
            documentSnapshots?.forEach { document ->
                try {
                    conversations.add(
                        ChatModel().apply {
                            this.id = document.data!!["id"] as String
                            this.owners = document.data!!["owners"] as MutableList<String>
                            this.admins = document.data!!["admins"] as MutableList<String>
                            this.ownershipModel = document.data!!["ownershipModel"] as String
                            this.mediaLinks = document.data!!["mediaLinks"] as MutableList<String>
                            this.currentMediaLink = document.data!!["currentMediaLink"] as String
                            this.content = (document.data!!["content"] as MutableList<Map<String, Any>>)
                                .map { it.toMessageModel() }.toMutableList()
                            this.conversationPhoto = document.data!!["conversationPhoto"] as String
                            this.conversationName = document.data!!["conversationName"] as String
                        }
                    )

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            contentProvider.conversations.value = conversations
            Controller.reloadList.value = !Controller.reloadList.value
        }
    }
}