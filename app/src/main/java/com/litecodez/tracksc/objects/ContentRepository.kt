package com.litecodez.tracksc.objects

import android.content.Context
import android.util.JsonToken
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.litecodez.tracksc.baseApi
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.models.ApiModel
import com.litecodez.tracksc.models.ChatModel
import com.litecodez.tracksc.models.OutcomeModel
import com.litecodez.tracksc.models.UserModel
import com.litecodez.tracksc.models.Video
import com.litecodez.tracksc.models.Videos
import com.litecodez.tracksc.toMessageModel
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.util.InternalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


class ContentRepository {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val mainScope = CoroutineScope(Dispatchers.Main)
    private val db = FirebaseCenter.getDatabase()
    private val storageRef = FirebaseCenter.getStorage()
    private val imageUrlCache: SnapshotStateMap<String, String> = mutableStateMapOf()
    private val gson = Gson()
    val activeListeners = mutableListOf<String>()
    private var listenerRegistration: ListenerRegistration? = null

    suspend fun fetchApiData(apiModel: ApiModel, apiCallBack: (String) -> Unit = {}) {

        val client = HttpClient()

        try {

            val api = buildString {
                append(apiModel.base)
                append(apiModel.endPoint)
                apiModel.params.forEach {
                    append(it)
                }
            }

            val apiData = withContext(Dispatchers.IO) {
                client.get(api).bodyAsText()
            }

            apiCallBack(apiData)
        } catch (e: Exception) {
            apiCallBack("")
        } finally {
            client.close()
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun fetchData(url: String, callBack: (ByteArray?) -> Unit = {}): ByteArray? {
        val client = HttpClient()

        return try {
            withContext(Dispatchers.IO) {
                val data  = client.get(url).readBytes()
                callBack(data)
                data
            }
        } catch (e: Exception) {
            callBack(null)
            null
        } finally {
            client.close()
        }
    }

    fun uploadImage(bucket:String = "images", imageBytes: ByteArray, imageName:String, type:String = ".png",onComplete: (OutcomeModel) -> Unit = {}) {
        val storageRef = FirebaseCenter.getStorage()

        val imageFileName = "${imageName}$type"
        val imageRef = storageRef.child("$bucket/$imageFileName")

        // Upload image to Firebase Storage
        val uploadTask = imageRef.putBytes(imageBytes)
        uploadTask.addOnSuccessListener {
            // Image upload successful, get the download URL
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                imageUrlCache[imageFileName] = uri.toString()
                onComplete(
                    OutcomeModel(
                        isError = false,
                        msg = uri.toString()
                    )
                )
            }
        }.addOnFailureListener {
            onComplete(
                OutcomeModel(
                    isError = true,
                    msg = it.message.toString()
                )
            )
        }
    }

    fun deleteImage(imagePath: String, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val storageRef = FirebaseCenter.getStorage()

        // Get reference to the image in Firebase Storage
        val imageRef = storageRef.child(imagePath)

        // Delete the image from Firebase Storage
        imageRef.delete()
            .addOnSuccessListener {
                onComplete(true, "Image deleted successfully")
            }
            .addOnFailureListener { exception ->
                onComplete(false, "Failed to delete image: ${exception.message}")
            }
    }

    fun getImageUrl(bucket:String, imageName:String, onComplete: (Boolean,String) -> Unit = {_,_->}) {

        if (imageUrlCache.containsKey(imageName)) {
            onComplete(true,imageUrlCache[imageName]!!)
        }else{
            // Get reference to the image in Firebase Storage
            val imageRef = storageRef.child("$bucket/$imageName")

            // Get download URL of the image
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                imageUrlCache[imageName] = uri.toString()
                onComplete(true,uri.toString())
            }.addOnFailureListener {
                onComplete(false,"$it")
            }
        }

    }
    fun downloadImageAsByteArray(bucket:String, imageName:String, onFailure: (Exception) -> Unit = {}, onSuccess: (ByteArray) -> Unit = {},) {
        storageRef.child("$bucket/$imageName")

        // Specify the maximum size (in bytes) to download
        val maxDownloadSizeBytes: Long = 20 * 1024 * 1024

        storageRef.getBytes(maxDownloadSizeBytes).addOnSuccessListener { bytes ->
            // Call the onSuccess callback with the byte array
            onSuccess(bytes)
        }.addOnFailureListener { exception ->
            // Call the onFailure callback with the exception
            onFailure(exception)
        }
    }
//    fun getVideos(fetchType: String = "remote", context: Context,callBack: (OutcomeModel) -> Unit) {
//        val factory = JsonFactory()
//        val mapper = ObjectMapper(factory).registerModule(
//            KotlinModule.Builder().build()  // Use KotlinModule.Builder
//        )
//
//        scope.launch {
//            fetchApiData(
//                apiModel = ApiModel(
//                    base = baseApi,
//                    endPoint = "/vids_v2.json"
//                )
//            ) {
//                try {
//                    val videos = mutableListOf<Video>()
//
//                    // Function to parse the video data using streaming
//                    fun parseVideos(jsonData: String) {
//                        val inputStream = jsonData.byteInputStream()
//                        factory.createParser(inputStream).use { parser ->
//                            while (parser.nextToken() != com.fasterxml.jackson.core.JsonToken.END_ARRAY) {
//                                val video = mapper.readValue(parser, Video::class.java)
//                                mainScope.launch { getToast(context, video.artist) }
//                                videos.add(video)  // Add each video to the list
//                            }
//                        }
//                    }
//
//                    // Use the appropriate data source based on fetchType
//                    if (fetchType == "local") {
//                        parseVideos(TCVideoData.getData())  // Parse local data
//                    } else {
//                        parseVideos(it)  // Parse remote data from the API
//                    }
//
//                    // Update the content provider with the list of videos
//                    contentProvider.videos.value = Videos(videos = videos)
//
//                    // Success callback
//                    callBack(
//                        OutcomeModel(
//                            isError = false,
//                            msg = "Videos fetched successfully"
//                        )
//                    )
//
//                } catch (e: Exception) {
//                    mainScope.launch {
//                        getToast(context, e.message.toString())
//                    }
//
//                    e.printStackTrace()
//                    // Error callback
//                    callBack(
//                        OutcomeModel(
//                            isError = true,
//                            msg = e.message.toString()
//                        )
//                    )
//                }
//            }
//        }
//    }

//    fun getVideos(fetchType:String = "local", context: Context,callBack: (OutcomeModel) -> Unit){
//        scope.launch {
//            fetchApiData(
//                apiModel = ApiModel(
//                    base = baseApi,
//                    endPoint = "/vids_v2.json"
//                )
//            ){
//                try {
//                    contentProvider.videos.value = if(fetchType == "REMOTE") {
//                        Videos(videos = Json.decodeFromString<List<Video>>(TCVideoData.getData()))
//                    }else{
//                        Videos(videos = Json.decodeFromString<List<Video>>(it))
//                    }
//                    callBack(
//                        OutcomeModel(
//                            isError = false,
//                            msg = "Videos fetched successfully"
//                        )
//                    )
//                }catch (e:Exception){
//                    mainScope.launch {
//                        getToast(context, e.message.toString(), long = true)
//                    }
//                    e.printStackTrace()
//                    callBack(
//                        OutcomeModel(
//                            isError = true,
//                            msg = e.message.toString()
//                        )
//                    )
    //gson.fromJson(it, Credits::class.java)
//                }
//            }
//        }
//    }

        fun getVideos(fetchType:String = "remote", context: Context,callBack: (OutcomeModel) -> Unit){
        scope.launch {
            fetchApiData(
                apiModel = ApiModel(
                    base = baseApi,
                    endPoint = "/vids_v2.json"
                )
            ){
                try {
                    contentProvider.videos.value = if(fetchType == "local") {
                        //Videos(videos = gson.fromJson(TCVideoData.getData(), Videos::class.java))
                        gson.fromJson(TCVideoData.getData(), Videos::class.java)
                    }else{
                        //Videos(videos = gson.fromJson(it, Videos::class.java))
                        gson.fromJson(it, Videos::class.java)
                    }
                    callBack(
                        OutcomeModel(
                            isError = false,
                            msg = "Videos fetched successfully"
                        )
                    )
                }catch (e:Exception){
                    mainScope.launch {
                        getToast(context, e.message.toString(), long = true)
                    }
                    e.printStackTrace()
                    callBack(
                        OutcomeModel(
                            isError = true,
                            msg = e.message.toString()
                        )
                    )
                }
            }
        }
    }

    fun getDocument(collectionPath: String, documentId: String, onFailure: (Exception) -> Unit = {}, onSuccess: (DocumentSnapshot?) -> Unit = {}) {
        try {
            db.collection(collectionPath).document(documentId).get()
                .addOnSuccessListener {
                    onSuccess(it)
                }
                .addOnFailureListener {
                    onFailure(it)
                    onSuccess(null)
                }
        } catch (e: Exception) {
            onFailure(e)
            onSuccess(null)
        }
    }

    fun getChat(chatId:String, callBack: (ChatModel?) -> Unit = {}){
        getDocument(Databases.Collections.CONVERSATIONS, chatId){ documentSnapshot ->
            if(documentSnapshot != null) {
                val chat = ChatModel().apply {
                    this.id = documentSnapshot.data!!["id"] as String
                    this.owners = documentSnapshot.data!!["owners"] as MutableList<String>
                    this.admins = documentSnapshot.data!!["admins"] as MutableList<String>
                    this.ownershipModel = documentSnapshot.data!!["ownershipModel"] as String
                    this.mediaLinks = documentSnapshot.data!!["mediaLinks"] as MutableList<String>
                    this.currentMediaLink = documentSnapshot.data!!["currentMediaLink"] as String
                    this.content = (documentSnapshot.data!!["content"] as MutableList<Map<String, Any>>)
                        .map { it.toMessageModel() }.toMutableList()
                    this.conversationPhoto = documentSnapshot.data!!["conversationPhoto"] as String
                    this.conversationName = documentSnapshot.data!!["conversationName"] as String
                }
                callBack(chat)
            }else{
                callBack(null)
            }
        }
    }

    fun getAllDocuments(collectionPath: String, onFailure: (Exception) -> Unit = {}, onSuccess: (List<DocumentSnapshot>?) -> Unit = {}) {
        try {
            db.collection(collectionPath)
                .get()
                .addOnSuccessListener { documents ->
                    onSuccess(documents.documents)
                }
                .addOnFailureListener { e ->
                    println("Error getting documents: $e ${getUserUid()}")
                    onFailure(e)
                }
        } catch (e: Exception) {
            println("Error getting documents: $e ${getUserUid()}")
            onFailure(e)
        }
    }

    fun getAllUserConversationDocuments(collectionPath: String, onFailure: (Exception) -> Unit = {}, onSuccess: (List<DocumentSnapshot>?) -> Unit = {}) {
        try {
            val userId = getUserUid() as Any // Assuming this function returns the current user's ID
            db.collection(collectionPath)
                .whereArrayContains("owners", userId)
                .get()
                .addOnSuccessListener { documents ->
                    onSuccess(documents.documents)
                }
                .addOnFailureListener { e ->
                    println("Error getting documents: $e $userId")
                    onFailure(e)
                }
        } catch (e: Exception) {
            println("Error getting documents: $e ${getUserUid()}")
            onFailure(e)
            onSuccess(null)
        }
    }
    
    fun updateDocument(collectionPath: String, documentId: String, data: Map<String, Any>, completion: (success: Boolean, error: Exception?) -> Unit = {_,_->}) {
        db.collection(collectionPath)
            .document(documentId)
            .update(data)
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception)
            }
    }

    fun updateDocumentByField(collectionPath: String, documentId: String, field: String, data: Any?, completion: (success: Boolean, error: Exception?) -> Unit = {_,_->}) {
        db.collection(collectionPath)
            .document(documentId)
            .update(field, data)
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception)
            }
    }

    fun deleteDocument(
        collectionPath: String,
        documentId: String,
        completion: (success: Boolean, error: Exception?) -> Unit = {_,_->}
    ) {
        db.collection(collectionPath)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                completion(true, null)
            }
            .addOnFailureListener { exception ->
                completion(false, exception)
            }
    }

    fun createDocument(collectionPath: String, documentId: String,
                       data: Map<String, Any>,
                       completion: (success: OutcomeModel) -> Unit = {}) {
        db.collection(collectionPath)
            .document(documentId)
            .set(data)
            .addOnSuccessListener {
                // Document created successfully
                completion(
                    OutcomeModel(
                        isError = false,
                        msg = "Document created successfully"
                    )
                )
            }
            .addOnFailureListener { exception ->
                // Handle errors
                completion(
                    OutcomeModel(
                        isError = true,
                        msg = exception.message.toString()
                    )
                )
            }
    }


    fun getUsers(documentId: String = "", onComplete: (List<UserModel>?, OutcomeModel) -> Unit = {
            _, _ ->
    }) {
        val userCollection = db.collection(documentId)
        userCollection.get()
            .addOnSuccessListener { result ->
                val userList = mutableListOf<UserModel>()
                for (document in result.documents) {

                    //val userData = document.toObject(UserModel::class.java)
                    if(document.data != null){
                        try {
                            val userData = UserModel().apply{
                                this.isFirstTimeLogin = document.data?.get("isFirstTimeLogin") as Boolean
                                this.profileImage = document.data!!["photoURL"] as String
                                this.id = document.data!!["id"] as String
                                this.firstName = document.data!!["firstName"] as String
                                this.lastName = document.data!!["lastName"] as String
                                this.email = document.data!!["email"] as String
                                this.isVerified = document.data!!["isVerified"] as Boolean
                            }
                            userList.add(userData)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }
                onComplete(
                    userList,
                    OutcomeModel(
                        isError = false,
                        msg = "Users fetched successfully"
                    )
                )
            }
            .addOnFailureListener { exception ->
                onComplete(
                    null,
                    OutcomeModel(
                        isError = true,
                        msg = exception.message.toString()
                    )
                )
            }
    }

}