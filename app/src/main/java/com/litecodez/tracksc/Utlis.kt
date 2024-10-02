package com.litecodez.tracksc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.firebase.auth.FirebaseAuth
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.LocalImages
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.NotificationModel
import com.litecodez.tracksc.models.UserModel
import com.litecodez.tracksc.objects.HasId
import com.litecodez.tracksc.models.OutcomeModel
import com.litecodez.tracksc.models.ReactionModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.models.TrackConnectionRequestModel
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.MediaDeleteRequest
import kotlinx.serialization.json.Json
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.intellij.lang.annotations.Pattern
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Calendar
import java.util.Locale
import java.util.Random


//================================================================
// System functions
//================================================================

/**
 * Displays a toast message with the specified text and duration.
 *
 * @param context The context in which the toast should be displayed.
 * @param msg The text message to display in the toast.
 * @param long Indicates whether the toast should have a longer duration. Default is `false` (short duration).
 */
fun getToast(context: Context, msg:String, long:Boolean = false){
    // Display a toast message with the specified text and duration
    Toast.makeText(
        context,
        msg,
        if(long){
            Toast.LENGTH_LONG}else{
            Toast.LENGTH_SHORT}
    ).show()
}

/**
 * Saves user credentials to SharedPreferences.
 *
 * @param value The value to be saved.
 * @param context The context of the application.
 */
fun savePreferences(key:String = "tos", value:String="", context: Context){
    val sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(key, value)
    editor.apply()
}


/**
 * Loads user credentials from SharedPreferences.
 *
 * @param context The context of the application.
 * @return The loaded user credentials, or an empty string if not found.
 */
fun loadPreferences(context: Context, key:String = "tos", default:String = ""):String{
    val sharedPreferences = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
    println(sharedPreferences.getString(key, ""))
    return sharedPreferences.getString(key, "") ?: default
}

fun createDir(context: Context, dirPath: String) {
    val path = context.filesDir
    val letDirectory = File(path, dirPath)

    if (letDirectory.exists()) {
        println("Directory already exists.")
    } else {
        val resultMkdirs: Boolean = letDirectory.mkdirs()
        if (resultMkdirs) {
            println("Directory created successfully.")
        } else {
            println("Failed to create directory.")
        }
    }
}
fun fileExists(context: Context, fileLocation: String, fileName: String): Boolean {
    val path = context.filesDir
    val letDirectory = File(path, fileLocation)
    val file = File(letDirectory, fileName)
    return file.exists() && file.isFile
}

fun saveBitmapToFile(
    context: Context,
    bitmap: Bitmap,
    fileLocation: String,
    fileName: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
) {
    try {
        // Create the directory if it doesn't exist
        val path = context.filesDir
        val dir = File(path, fileLocation)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // Create the file using the location and file name
        val file = File(dir, fileName)

        // Write the bitmap to the file
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(format, quality, outputStream)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun deleteFile(
    context: Context,
    fileLocation: String,
    fileName: String
): Boolean {
    val path = context.filesDir
    val letDirectory = File(path, fileLocation)
    val file = File(letDirectory, fileName)

    return if (file.exists()) {
        file.delete()
    } else {
        false
    }
}

fun createFile(
    context: Context,
    fileLocation: String,
    fileName: String,
    content: String? = null,
    overwrite: Boolean = false
): File {
    val path = context.filesDir
    val letDirectory = File(path, fileLocation)

    if (!letDirectory.exists()) {
        letDirectory.mkdirs()

    }

    //getToast(context, letDirectory.absolutePath)

    val file = File(letDirectory, fileName)



    if (overwrite || !file.exists()) {
        file.createNewFile()
        content.ifNotNull {
            file.writeText(it)
        }
    }


    return file
}

fun readImagesFile(
    context: Context,
    fileLocation: String,
    fileName: String,
    callback: (LocalImages) -> Unit = {}
): LocalImages {

    val path = context.filesDir
    val letDirectory = File(path, fileLocation)

    val file = File(letDirectory, fileName)

    // Read file content and deserialize it
    val fileContent = file.readText()

    if (fileContent.isEmpty()) {
        callback(LocalImages())
        return LocalImages()
    } else {
        val localImages = Json.decodeFromString<LocalImages>(fileContent)
        callback(localImages)
        return localImages
    }
}

//================================================================
// Date and time functions
//================================================================

/**
 * Checks if the given date string is in a valid format.
 *
 * @param input The date string to be validated.
 * @return True if the date is valid, false otherwise.
 */
fun isValidDate(input: String): Boolean {
    val pattern = "dd/MM/yyyy"
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    dateFormat.isLenient = false

    return try {
        if (input.length != pattern.length || input[2] != '/' || input[5] != '/') {
            // The input length doesn't match the pattern length, or slashes are not at correct positions
            false
        } else {
            val year = input.substring(6, 10).toIntOrNull()
            val month = input.substring(3, 5).toIntOrNull()
            val day = input.substring(0, 2).toIntOrNull()

            if (year == null || month == null || day == null) {
                // One or more segments are not valid integers
                false
            } else {
                val calendar = Calendar.getInstance()
                calendar.isLenient = false
                calendar.set(year, month - 1, day)

                // Check if the parsed date matches the input components
                val parsedYear = calendar.get(Calendar.YEAR)
                val parsedMonth = calendar.get(Calendar.MONTH) + 1
                val parsedDay = calendar.get(Calendar.DAY_OF_MONTH)

                parsedYear == year && parsedMonth == month && parsedDay == day
            }
        }
    } catch (e: Exception) {
        false
    }
}

/**
 * Checks if the given time string is in a valid format.
 *
 * @param input The time string to be validated.
 * @return True if the time is valid, false otherwise.
 */
fun isValidTime(input: String): Boolean {
    val pattern = "HH:mm"
    val timeFormat = SimpleDateFormat(pattern, Locale.getDefault())
    timeFormat.isLenient = false

    return try {
        if (input.length != pattern.length) {
            // The input length doesn't match the pattern length
            false
        } else {
            val hour = input.substring(0, 2).toIntOrNull()
            val minute = input.substring(3, 5).toIntOrNull()

            if (hour == null || minute == null) {
                // One or both segments are not valid integers
                false
            } else if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                // Hour or minute is out of valid range
                false
            } else {
                timeFormat.parse(input)
                true
            }
        }
    } catch (e: ParseException) {
        false
    }
}

fun getCurrentDate(pattern: String = "yyyy-MM-dd"): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return currentDateTime.format(formatter)
}

fun getCurrentTime(pattern: String = "HH:mm:ss.SSS"): String {
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return currentTime.format(formatter)
}


//================================================================
// Extension functions
//================================================================

fun String.isValidEmail(): Boolean {
    // Regular expression pattern for validating email addresses
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\$")
    // Return whether the email matches the pattern
    return this.matches(emailRegex)
}

inline fun Unit.then(block: Unit.() -> Unit) {
    val result = this
    result.block()
    let {  }
}

inline fun <T> T?.ifNotNull(block: (T) -> Unit):T? {
    if (this != null) {
        block(this)
    }
    return this
}


fun UserModel.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "firstName" to firstName,
        "lastName" to lastName,
        "email" to email,
        "profileImage" to profileImage,
        "isVerified" to isVerified,
        "isFirstTimeLogin" to isFirstTimeLogin,
        "tag" to tag
    )
}

fun Map<String, Any>.toUserModel(): UserModel {
    return UserModel(
        id = this["id"] as String,
        firstName = this["firstName"] as String,
        lastName = this["lastName"] as String,
        email = this["email"] as String,
        profileImage = this["profileImage"] as String,
        isVerified = this["isVerified"] as Boolean,
        isFirstTimeLogin = this["isFirstTimeLogin"] as Boolean,
        tag = this["tag"] as String
    )
}

fun ReactionModel.toMap(): Map<String, Any> {
    return mapOf(
        "reactor" to reactor,
        "reaction" to reaction
    )
}

fun MessageModel.toMap(): Map<String, Any> {
    return mapOf(
        "chatId" to chatId,
        "sender" to sender,
        "senderName" to senderName,
        "type" to type,
        "content" to content,
        "timestamp" to timestamp,
        "reactions" to reactions.map { it.toMap() }
    )
}

fun ChatModel.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "owners" to owners,
        "admins" to admins,
        "ownershipModel" to ownershipModel,
        "mediaLinks" to mediaLinks,
        "currentMediaLink" to currentMediaLink,
        "content" to content.map { it.toMap() },
        "conversationPhoto" to conversationPhoto,
        "conversationName" to conversationName
    )
}

fun Map<String, Any?>.toReactionModel(): ReactionModel {
    return ReactionModel(
        reactor = this["reactor"] as String,
        reaction = this["reaction"] as String
    )
}

fun Map<String, Any>.toMessageModel(): MessageModel {
    return MessageModel(
        chatId = this["chatId"] as String,
        sender = this["sender"] as String,
        senderName = this["senderName"] as String,
        type = this["type"] as String,
        content = this["content"] as String,
        timestamp = this["timestamp"] as String,
        reactions = (this["reactions"] as List<Map<String, Any?>>).map { it.toReactionModel() }.toMutableList()
    )
}

fun Map<String, Any>.toChatModel(): ChatModel {
    return ChatModel(
        id = this["id"] as String,
        owners = (this["owners"] as List<String>).toMutableList(),
        admins = (this["admins"] as List<String>).toMutableList(),
        ownershipModel = this["ownershipModel"] as String,
        mediaLinks = (this["mediaLinks"] as List<String>).toMutableList(),
        currentMediaLink = this["currentMediaLink"] as String,
        content = (this["content"] as List<Map<String, Any>>).map { it.toMessageModel() }.toMutableList(),
        conversationPhoto = this["conversationPhoto"] as String,
        conversationName = this["conversationName"] as String
    )
}

fun NotificationModel.toMap(): Map<String, Any> {
    return mapOf(
        "recipientId" to recipientId,
        "chatId" to chatId,
        "messageIndex" to messageIndex,
        "wasRead" to wasRead,
        "type" to type
    )
}

fun List<NotificationModel>.toListMap(): List<Map<String, Any>> {
    return this.map { it.toMap() }
}

fun Map<String, Any>.toNotificationModel(): NotificationModel {
    return NotificationModel(
        recipientId = this["recipientId"] as String,
        chatId = this["chatId"] as String,
        messageIndex = this["messageIndex"].toString().toInt(),
        wasRead = this["wasRead"] as Boolean,
        type = this["type"] as String
    )
}

fun TagsModel.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "type" to type,
        "photoUrl" to photoUrl
    )
}

fun Map<String, Any>.toTagsModel(): TagsModel {
    return TagsModel(
        id = this["id"] as String,
        userId = this["userId"] as String,
        name = this["name"] as String,
        type = this["type"] as String,
        photoUrl = this["photoUrl"] as String
    )
}

fun MediaDeleteRequest.toMap(): Map<String, Any> {
    return mapOf(
        "chatId" to chatId,
        "userId" to userId,
        "mediaId" to mediaId,
        "mediaLocation" to mediaLocation
    )
}


fun TrackConnectionRequestModel.toMap(): Map<String, Any> {
    return mapOf(
        "senderId" to senderId,
        "targetId" to targetId,
        "targetType" to targetType,
        "targetName" to targetName
    )
}

fun Map<String, Any>.toTrackConnectionRequestModel(): TrackConnectionRequestModel {
    return TrackConnectionRequestModel(
        senderId = this["senderId"] as String,
        targetId = this["targetId"] as String,
        targetType = this["targetType"] as String,
        targetName = this["targetName"] as String
    )
}

fun ImageBitmap.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
    val bitmap = this.asAndroidBitmap()
    val stream = ByteArrayOutputStream()
    bitmap.compress(format, quality, stream)
    return stream.toByteArray()
}

fun String.toDecompressedString(): String {
    val compressedBytes = Base64.getDecoder().decode(this)
    val inputStream = compressedBytes.inputStream()
    val decompressorInputStream = CompressorStreamFactory().createCompressorInputStream(
        CompressorStreamFactory.GZIP, inputStream
    )

    val outputBytes = decompressorInputStream.readBytes()
    decompressorInputStream.close()
    return outputBytes.toString(Charsets.UTF_8)
}
fun String.toCompressedString(): String {
    val inputBytes = this.toByteArray()
    val outputStream = ByteArrayOutputStream()
    val compressorOutputStream = CompressorStreamFactory().createCompressorOutputStream(
        CompressorStreamFactory.GZIP, outputStream
    )
    compressorOutputStream.write(inputBytes)
    compressorOutputStream.close()
    outputStream.close()

    val compressedBytes = outputStream.toByteArray()
    return Base64.getEncoder().encodeToString(compressedBytes)
}

fun ByteArray.toBase64String(): String {
    return Base64.getEncoder().encodeToString(this)
}

fun String.toBase64ByteArray(): ByteArray {
    return Base64.getDecoder().decode(this)
}

fun String.stringToUniqueInt(): Int {
    // Take the last 4 characters of the input string
    val lastFour = this.takeLast(4)

    // Function to convert a letter to its position in the alphabet
    fun letterToPosition(char: Char): String {
        return if (char.isLetter()) {
            (char.lowercaseChar() - 'a' + 1).toString().padStart(2, '0')
        } else {
            char.toString()
        }
    }

    // Process each character, join the results, and convert to Int
    return lastFour.map { letterToPosition(it) }.joinToString("").toInt()
}
fun Map<String, Any>.keyFor(value: Any): String? {
    return this.entries.find { it.value == value }?.key
}

//================================================================
// Other functions
//================================================================

fun generateUniqueID(dataSet: List<Any>, length: Int = 8, prefix: String = "TRACKS-"): String {
    val random = Random()
    val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    var randomID: String
    var isUnique: Boolean

    do {
        // Generate a random ID of the specified length
        val idBuilder = StringBuilder()
        for (i in 0 until length) {
            val randomChar = chars[random.nextInt(chars.length)]
            idBuilder.append(randomChar)
        }
        randomID = "$prefix$idBuilder"

        // Check if the generated ID is unique
        isUnique = dataSet.none { if (it is HasId) {
            it.id == randomID
        } else {
            false
        } }
    } while (!isUnique)

    return randomID
}

fun getUserEmail():String{
    return FirebaseAuth.getInstance().currentUser?.email?:""
}

fun getUserUid():String?{
    return FirebaseAuth.getInstance().currentUser?.uid
}

fun getUserName():String{
 return "${ contentProvider.userProfile.value.firstName } ${ contentProvider.userProfile.value.lastName }"
}

fun extractVideoId(link: String): OutcomeModel {
    val outcome = OutcomeModel()

    // Validate the YouTube URL format
    val singleVideoPattern = "https://www\\.youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})"
    val playlistVideoPattern = "https://www\\.youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})&list=[a-zA-Z0-9_-]+"

    val singleVideoRegex = Regex(singleVideoPattern)
    val playlistVideoRegex = Regex(playlistVideoPattern)

    when {
        singleVideoRegex.matches(link) -> {
            val matchResult = singleVideoRegex.find(link)
            if (matchResult != null) {
                outcome.msg = matchResult.groupValues[1]
            } else {
                outcome.isError = true
                outcome.msg = "Failed to extract video ID from single video link."
            }
        }
        playlistVideoRegex.matches(link) -> {
            val matchResult = playlistVideoRegex.find(link)
            if (matchResult != null) {
                outcome.msg = matchResult.groupValues[1]
            } else {
                outcome.isError = true
                outcome.msg = "Failed to extract video ID from playlist link."
            }
        }
        else -> {
            outcome.isError = true
            outcome.msg = "Invalid YouTube link format."
        }
    }

    return outcome
}

fun nextVideo(){
    if(contentProvider.incrementer.intValue < contentProvider.currentPlaylist.value.size){
        contentProvider.nowPlaying.value = contentProvider.currentPlaylist.value[contentProvider.incrementer.intValue]
        contentProvider.incrementer.intValue++
    }else{
        contentProvider.incrementer.intValue = 0
        contentProvider.nowPlaying.value = contentProvider.currentPlaylist.value[contentProvider.incrementer.intValue]
        contentProvider.incrementer.intValue++
    }
}

fun getIndexOfVideo():Int{
    return contentProvider.currentPlaylist.value.indexOf(contentProvider.nowPlaying.value)
}

fun sendImageMessage(imageName:String, imageBytes:ByteArray, onUpload:(OutcomeModel)->Unit = {}){
    contentRepository.uploadImage(Databases.Buckets.USER_UPLOADS,imageBytes,imageName){
        onUpload(it)
    }
}



fun createBitmapFromPicture(picture: Picture): Bitmap {
    val bitmap = Bitmap.createBitmap(
        picture.width,
        picture.height,
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawPicture(picture)
    return bitmap
}
fun createByteArrayFromBitmap(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}