package com.litecodez.tracksc.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.chatContainer
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.loading
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.ConversationEditModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes
import kotlinx.coroutines.delay


@Composable
fun ChatList(modifier: Modifier = Modifier, operator: Operator) {
    var conversations by remember { mutableStateOf(contentProvider.conversations.value) }

    var filteredRequests by remember { mutableStateOf(emptyList<TagsModel>()) }

    var firstTimeLaunch by remember { mutableStateOf(true) }

    LaunchedEffect(Controller.reloadList.value, contentProvider.conversations.value) {
        conversations = contentProvider.conversations.value
    }
    LaunchedEffect(Controller.reloadChatListView.value) {
        if(!firstTimeLaunch){
            appNavigator.setViewState(loading)
        }else{
            firstTimeLaunch = false
        }
    }

    LaunchedEffect(contentProvider.listOfConnectionRequests.value) {
        filteredRequests = contentProvider.listOfConnectionRequests.value.filter { req ->
            conversations.none {
                conv -> conv.owners.contains(req.userId)
            }
        }
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
                        //val setIndex by remember { mutableIntStateOf(index) }
                        ChatListItemWrapper(
                            chat = conversation,
                            index = index,
                            onLongClick = { conversation ->
                                val editModel = ConversationEditModel(
                                    conversationId = conversation.id,
                                    action = TCDataTypes.ConversationManagementType.DELETE,
                                    requester = getUserUid()!!
                                )
                                operator.sendConversationManagementRequest(editModel){
                                    Controller.reloadChatListView.value = !Controller.reloadChatListView.value
                                }
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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .wrapContentSize()
        )

        val connectionTipScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(8.dp)
                .height(TCDataTypes.Fibonacci.ONE_HUNDRED_AND_44.dp)
                .verticalScroll(connectionTipScrollState)
                .align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(3.dp, alignment = Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            filteredRequests.forEach { request ->
                SimpleAnimator(style = AnimationStyle.UP) {
                    Card(
                        colors = CardDefaults.cardColors().copy(
                            containerColor = Color.White,
                            contentColor = Color.Blue
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 5.dp
                        ),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text("Connecting to ${request.name}...", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            Controller.reloadChatListView.value = !Controller.reloadChatListView.value
            conversations = emptyList()
        }
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

