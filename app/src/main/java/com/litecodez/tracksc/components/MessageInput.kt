package com.litecodez.tracksc.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.contentProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import network.chaintech.cmpimagepickncrop.CMPImagePickNCropDialog
import network.chaintech.cmpimagepickncrop.imagecropper.rememberImageCropper

@Composable
fun MessageInput(
    modifier: Modifier = Modifier,
    onGetAttachment: (ImageBitmap?) -> Unit = {},
    onSent: (String) -> Unit = {}
) {
    var message by rememberSaveable { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()
    var selectedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var openImagePicker by rememberSaveable { mutableStateOf(value = false) }

    CMPImagePickNCropDialog(
        imageCropper = imageCropper,
        openImagePicker = openImagePicker,
        imagePickerDialogHandler = {
            openImagePicker = it
        },
        selectedImageCallback = {
            selectedImage = it
            onGetAttachment(selectedImage)
        }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White.copy(alpha = 0.0f), shape = CircleShape)
            .padding(8.dp)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .background(color = Color.White.copy(alpha = 0.0f), shape = CircleShape)
                .weight(0.8f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(text = "Message")
            },
            value = message,
            onValueChange = {
                message = it
            },
            colors = OutlinedTextFieldDefaults.colors().copy(
                focusedContainerColor = contentProvider.majorThemeColor.value.copy(alpha = 0.6f),
                focusedTextColor = contentProvider.textThemeColor.value,
            )
        )
        // attachment button
        IconButton(
            modifier = Modifier
                .weight(0.1f)
                .background(color = Color.White.copy(alpha = 0.0f))
                .padding(0.dp)
                .size(34.dp),
            onClick = {
                openImagePicker = true
            }
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = "attachment",
                modifier = Modifier
                    .background(color = Color.White.copy(alpha = 0.0f))
                    .padding(0.dp)
                    .size(34.dp)
            )
        }

        // send button
        IconButton(
            modifier = Modifier
                .weight(0.1f)
                .background(color = Color.White.copy(alpha = 0.0f))
                .padding(0.dp)
                .size(34.dp),
            onClick = {
                if (message.isNotEmpty()) {
                    scope.launch {
                        focusManager.clearFocus()
                        delay(200)
                        onSent(message)
                        message = ""
                    }

                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "send",
                modifier = Modifier
                    .padding(0.dp)
                    .size(34.dp)
            )
        }
    }
}
