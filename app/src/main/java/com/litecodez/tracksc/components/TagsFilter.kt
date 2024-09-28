package com.litecodez.tracksc.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Outline
import androidx.test.core.app.ActivityScenario.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TagsFilter(){
    var text by rememberSaveable { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var typingTimer by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
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
}