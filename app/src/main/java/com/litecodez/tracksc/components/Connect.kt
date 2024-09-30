package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.objects.TCDataTypes

@Composable
fun Connect(
    modifier: Modifier = Modifier,
    tag:TagsModel,
    onConnect: (TagsModel) -> Unit = {}
){
    var connectionButtonEnabled by rememberSaveable {
        mutableStateOf(true)
    }
    Row(
        modifier = modifier
            .padding(
                TCDataTypes.Fibonacci.THREE.dp
            )
            .background(
                color = contentProvider.minorThemeColor.value.copy(alpha = 0.5f)
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = tag.name,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "<${ tag.type }>",
            modifier = Modifier.weight(1f),
            maxLines = 1,
            color = Color.Black,
            fontWeight = FontWeight.Light,
            fontStyle = FontStyle.Italic
        )

        Button(
            onClick = {
                onConnect(tag)
                connectionButtonEnabled = false
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = contentProvider.majorThemeColor.value,
                contentColor = contentProvider.textThemeColor.value
            ),
            enabled = connectionButtonEnabled
        ){
            Text(text = "Connect")
        }
    }
}