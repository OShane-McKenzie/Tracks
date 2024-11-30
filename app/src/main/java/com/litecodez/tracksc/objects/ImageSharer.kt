package com.litecodez.tracksc.objects

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class ImageSharer {
    /**
     * Shares a bitmap image to other apps
     *
     * @param context The current context
     * @param bitmap The bitmap image to be shared
     * @param fileName Name of the file to be created (should end with .jpg, .png etc.)
     * @param chooserTitle Title of the share intent chooser
     */
    fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        fileName: String = "shared_image.jpg",
        chooserTitle: String = "Share Image"
    ) {
        // Create a temporary file in the app's cache directory
        val file = File(context.cacheDir, fileName)

        try {
            // Write bitmap to file
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Get URI using FileProvider for secure sharing
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Create share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Start chooser
            context.startActivity(Intent.createChooser(shareIntent, chooserTitle))

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle potential errors (e.g., unable to write file)
        }
    }

    /**
     * Clean up cached shared files (call in onDestroy or when no longer needed)
     *
     * @param context The current context
     */
    fun clearSharedFiles(context: Context) {
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("shared_image")) {
                file.delete()
            }
        }
    }
}