package com.litecodez.tracksc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.litecodez.tracksc.objects.Controller
import java.io.ByteArrayOutputStream

/**
 * Helper class for picking images from the device's external storage.
 *
 * @param context The context in which the image picker is used.
 */
class ImagePicker(private val context: Context) {

    private val pickImageLauncher =
        (context as? ComponentActivity)?.activityResultRegistry?.register(
            "key",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri = result.data?.data!!
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                val stream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                imageByteArray = stream.toByteArray()

                if(imageByteArray != null) {
                    contentProvider.imageByteArray.value = imageByteArray
                    Controller.imageReady.value = true
                }
            }
        }

    private var imageByteArray: ByteArray? = null


    /**
     * Retrieves the selected image as a byte array.
     *
     * @return The selected image as a byte array.
     */
    fun getImage():ByteArray?{
        return imageByteArray
    }

    /**
     * Composable function to open the image picker.
     */
    @Composable
    fun OpenImagePicker() {
        val currentContext = LocalContext.current
        val imagePicker = rememberUpdatedState(this)
        imagePicker.value.openImagePickerInternal(currentContext)
    }

    private fun openImagePickerInternal(context: Context) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher?.launch(intent)
    }
}

class ObserverWrapper(private val observer: (ByteArray?) -> Unit) : DefaultLifecycleObserver {
    fun onCleared(owner: LifecycleOwner) {
        // Call the observer when the composable is cleared (destroyed)
        observer(null)
    }
}

class ObserverWrapper2(private val observer: (Activity?) -> Unit) : DefaultLifecycleObserver {
    fun onCleared(owner: LifecycleOwner) {
        // Call the observer when the composable is cleared (destroyed)
        observer(null)
    }
}