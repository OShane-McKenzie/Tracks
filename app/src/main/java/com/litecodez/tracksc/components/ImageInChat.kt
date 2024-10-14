package com.litecodez.tracksc.components

import android.graphics.Bitmap
import android.graphics.Picture
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.litecodez.tracksc.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


@Composable
fun ImageInChat(
    modifier: Modifier = Modifier,
    img:Any,
    shape:RoundedCornerShape = RoundedCornerShape(3),
    defaultImage:Int = R.drawable.loading,
    contentScale: ContentScale = ContentScale.Fit,
    onSuccess:(Bitmap) -> Unit = {}){

    val scope = rememberCoroutineScope()
    Box(modifier = modifier.background(color = Color.White, shape = shape)){
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(img)
                .crossfade(true)
                .build(),
            placeholder = painterResource(defaultImage),
            contentDescription = "",
            contentScale = contentScale,
            modifier = Modifier

                .clip(shape)
                .fillMaxSize()
                .padding(5.dp),
            onSuccess = {
                onSuccess(it.result.drawable.toBitmap())
            }
        )
    }
}
