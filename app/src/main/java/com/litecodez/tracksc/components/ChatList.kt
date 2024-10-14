package com.litecodez.tracksc.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardOptionKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.chatContainer
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.ConversationEditModel
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatList(modifier: Modifier = Modifier, operator: Operator) {

    var conversations by remember { mutableStateOf(contentProvider.conversations.value) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Controller.reloadList.value, contentProvider.conversations.value) {
        conversations = emptyList()
        delay(10)
        conversations = contentProvider.conversations.value
    }

    Box(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.ONE_HUNDRED_AND_44.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
            ) {
                itemsIndexed(
                    items = conversations,
                    key = { _,conversation -> conversation.id }
                ) { index, conversation ->
                    if (conversations.isEmpty()) return@itemsIndexed
                    key(conversation.id) {
                        val setIndex by remember { mutableIntStateOf(index) }
                        ChatListItemWrapper(
                            chat = conversation,
                            index = setIndex,
                            onLongClick = { conversation ->
                                val editModel = ConversationEditModel(
                                    conversationId = conversation.id,
                                    action = TCDataTypes.ConversationManagementType.DELETE,
                                    requester = getUserUid()!!
                                )
                                operator.sendConversationManagementRequest(editModel)
                            }
                        ) {
                            appNavigator.setViewState(chatContainer)
                        }
                    }
                }
            }
        }
        TagsFilter(
            operator = operator,
            modifier = Modifier.align(Alignment.TopCenter).wrapContentSize()
        )

    }
}

@Composable
private fun ChatListItemWrapper(
    modifier: Modifier = Modifier,
    chat: ChatModel,
    saveChatImage: Boolean = false,
    index: Int,
    onLongClick: (ChatModel) -> Unit = {},
    onClick: (ChatModel) -> Unit = {}
) {
    val conversation by rememberUpdatedState(chat)
    ChatListItem(
        modifier = modifier,
        chatModel = conversation,
        saveChatImage = saveChatImage,
        index = index,
        onLongClick = onLongClick,
        onClick = onClick
    )
}

