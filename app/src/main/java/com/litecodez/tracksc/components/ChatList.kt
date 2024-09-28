package com.litecodez.tracksc.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.chatContainer
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.TCDataTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChatList(modifier: Modifier = Modifier) {

    var conversations by remember { mutableStateOf(contentProvider.conversations.value) }

    var localReload by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(Controller.reloadList.value) {
        conversations = listOf()
        delay(10)
        conversations = contentProvider.conversations.value
        localReload = !localReload
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter)
        ) {
            Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHTY_NINE.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            ) {
                itemsIndexed(
                    items = conversations,
                    key = { _,conversation -> conversation.id }
                ) { index, conversation ->
                    key(conversation.id) {
                        val setIndex by remember { mutableIntStateOf(index) }
                        ChatListItemWrapper(chat = conversation, index = setIndex) {
                            contentProvider.currentChat.value = conversation
                            appNavigator.setViewState(chatContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItemWrapper(
    modifier: Modifier = Modifier,
    chat: ChatModel,
    saveChatImage: Boolean = false,
    index: Int,
    onClick: (ChatModel) -> Unit = {}
) {
    val conversation by rememberUpdatedState(chat)
    ChatListItem(
        modifier = modifier,
        chatModel = conversation,
        saveChatImage = saveChatImage,
        index = index,
        onClick = onClick
    )
}