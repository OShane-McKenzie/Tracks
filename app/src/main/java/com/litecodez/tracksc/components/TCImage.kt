package com.litecodez.tracksc.components

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.litecodez.tracksc.R
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.saveBitmapToFile
import java.io.File

@Composable
fun TCImage(
    modifier: Modifier = Modifier,
    img: String,
    remoteDatabase: String = Databases.Buckets.USER_UPLOADS
) {
    val context = LocalContext.current
    val path = context.filesDir
    val dir = File(path, Databases.Local.IMAGES_DB)
    val imgFile = File(dir, img)
    val localImageFound = remember { imgFile.exists() }

    var imageData by remember { mutableStateOf<Any>(if (localImageFound) imgFile else "") }

    // Function to get the image from the remote database
    fun getImage() {
        contentRepository.getImageUrl(
            bucket = remoteDatabase,
            imageName = img
        ) { success, url ->
            if (success) {
                imageData = url
            } else {
                getToast(context, "Image not found", long = true)
            }
        }
    }

    // Load the image either from local storage or remotely
    LaunchedEffect(Unit) {
        if (!localImageFound) {
            getImage()
        } else {
            // If local image is found, directly set the imageData to the decoded file
            imageData = BitmapFactory.decodeFile(imgFile.absolutePath)
        }
    }

    // Use ImageInChat to display the image
    ImageInChat(modifier = modifier, img = imageData, defaultImage = R.drawable.user) { bitmap ->
        // Save the bitmap to local storage after it's successfully loaded
        saveBitmapToFile(
            context = context,
            fileLocation = Databases.Local.IMAGES_DB,
            bitmap = bitmap,
            fileName = img
        )
    }
}