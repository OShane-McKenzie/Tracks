package com.litecodez.tracksc.components

import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.litecodez.tracksc.R
import com.litecodez.tracksc.objects.TCDataTypes

@Composable
fun GifImage(
        modifier: Modifier = Modifier,
        contentDescription:String = "",
        contentScale: ContentScale = ContentScale.FillBounds,
        gif:Int = R.drawable.upload
    ){
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = gif)
                .apply(block = fun ImageRequest.Builder.() {
                }).build(),
            imageLoader = imageLoader
        ),
        modifier = modifier
            .height(TCDataTypes.Fibonacci.ONE_HUNDRED_AND_44.dp)
            .width(TCDataTypes.Fibonacci.ONE_HUNDRED_AND_44.dp),
        contentDescription = contentDescription,
        contentScale = contentScale
    )
}