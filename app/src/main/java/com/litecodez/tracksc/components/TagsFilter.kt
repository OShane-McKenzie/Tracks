package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario.launch
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.models.TrackConnectionRequestModel
import com.litecodez.tracksc.objects.AnimationStyle
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.TCDataTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TagsFilter(modifier: Modifier = Modifier, operator: Operator){
    var text by rememberSaveable { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var typingTimer by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    val filteredTags = contentProvider.tags.value.filter {
        it.id .contains(text.trim(), ignoreCase = true) &&
        text.trim().isNotEmpty() &&
        text.trim().length > 1
    }
    Column(
        modifier = modifier
            .wrapContentSize()
            .padding(top = TCDataTypes.Fibonacci.EIGHT.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = text,
            placeholder = {
                Text(text = "Discover people")
            },
            onValueChange = {
                text = it
                isTyping = true
                typingTimer?.cancel()
                typingTimer = scope.launch {
                    delay(1000)}
            }
        )

        if(text.trim().isNotEmpty() && text.trim().length > 1){
            SimpleAnimator(
                style = AnimationStyle.UP
            ){
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .defaultMinSize(minHeight = TCDataTypes.Fibonacci.FIFTY_FIVE.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.White.copy(alpha = 0.0f),
                                shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = Color.White.copy(alpha = 0.0f),
                                    shape = RoundedCornerShape(TCDataTypes.Fibonacci.EIGHT.dp)
                                )
                                .blur(TCDataTypes.Fibonacci.TWELVE.dp)
                        ) {}
                        Column(
                            modifier = Modifier.wrapContentSize()
                        ) {
                            filteredTags.forEach { tag ->
                                Connect(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    tag
                                ) { target ->
                                    if (target.userId != getUserUid()) {
                                        getUserUid().ifNotNull {
                                            operator.sendConnectionRequest(
                                                TrackConnectionRequestModel(
                                                    senderId = it,
                                                    targetId = target.userId,
                                                    targetType = target.type,
                                                    targetName = target.name
                                                )
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(TCDataTypes.Fibonacci.EIGHT.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}