package com.litecodez.tracksc.objects

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.google.firebase.firestore.FieldValue
import com.google.gson.Gson
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getCurrentDate
import com.litecodez.tracksc.getCurrentTime
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.help
import com.litecodez.tracksc.home
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.ifNull
import com.litecodez.tracksc.login
import com.litecodez.tracksc.models.ApiModel
import com.litecodez.tracksc.models.ConversationEditModel
import com.litecodez.tracksc.models.MediaDeleteRequest
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.OutcomeModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.models.TrackConnectionRequestModel
import com.litecodez.tracksc.models.UserModel
import com.litecodez.tracksc.models.Values
import com.litecodez.tracksc.privacyPolicy
import com.litecodez.tracksc.profile
import com.litecodez.tracksc.savePreferences
import com.litecodez.tracksc.termsOfService
import com.litecodez.tracksc.toByteArray
import com.litecodez.tracksc.toMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

class Operator(
    private val context: Context,
    private val authenticationManager: AuthenticationManager
) {
    val operationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun splashOperation(callback: (OutcomeModel) -> Unit={}){
        operationScope.launch {
            contentRepository.fetchApiData(
                ApiModel(endPoint = privacyPolicy)
            ){
                contentProvider.privacyPolicy.value = it
            }

            contentRepository.fetchApiData(
                ApiModel(endPoint = termsOfService)
            ){
                contentProvider.termsOfService.value = it
            }

            contentRepository.fetchApiData(
                ApiModel(endPoint = help)
            ){
                contentProvider.help.value = it
            }

            contentRepository.fetchApiData(
                ApiModel(endPoint = "/app_detail.json")
            ){

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val json = Json {
                            ignoreUnknownKeys = true
                            coerceInputValues = true
                        }
                        contentProvider.values.value = json.decodeFromString<Values>(it)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }finally {
                        this.cancel()
                    }
                }
            }
        }

        Controller.firstLaunch.value = true
        Controller.reloadRestrictions.value = false
        val isLoggedIn = authenticationManager.isUserLoggedIn()

        if(isLoggedIn){
            if(authenticationManager.isEmailVerified()){
                authenticationManager.isFirstLogin {firstTimeLogin->
                    Initializer.runInit(context, firstTimeLogin){
                        if(firstTimeLogin) {
                            appNavigator.setViewState(
                                profile,
                                execTask = false, updateHistory = false
                            )
                        }else{
                            appNavigator.setViewState(
                                home,
                                execTask = false, updateHistory = false
                            )
                        }
                    }
                }
            }else{
                appNavigator.setViewState(login, updateHistory = false, execTask = true){
                    Controller.emailVerificationResendable.value = true
                    callback(
                        OutcomeModel(
                        isError = true,
                        msg = "Email not verified"
                        )
                    )
                }
            }
        }else{
            appNavigator.setViewState(login, updateHistory = false)
        }

    }

    fun loginOperation(loginMethod: LoginMethod,
                       email: String?=null,
                       password: String?=null,
                       callback: (OutcomeModel) -> Unit
    ){
        if(loginMethod == LoginMethod.GOOGLE){
            authenticationManager.signInWithGoogle()
        }else if(loginMethod == LoginMethod.EMAIL_PASSWORD){
            operationScope.launch {
                if(email != null && password != null){
                    authenticationManager.signInWithEmailAndPassword(email, password){
                        if(it){
                            if(contentProvider.currentUser.value!=null){
                                if(contentProvider.currentUser.value!!.isEmailVerified){
                                    authenticationManager.isFirstLogin {firstTimeLogin->
                                        Initializer.runInit(context,firstTimeLogin){
                                            if(firstTimeLogin) {
                                                appNavigator.setViewState(
                                                    profile,
                                                    execTask = false, updateHistory = false
                                                )
                                            }else{
                                                appNavigator.setViewState(
                                                    home,
                                                    execTask = false, updateHistory = false
                                                )
                                            }
                                        }
                                    }
                                }else{
                                    Controller.emailVerificationResendable.value = true
                                    callback(
                                        OutcomeModel(
                                        isError = true,
                                        msg = "Email not verified")
                                    )
                                }
                            }else{
                                callback(
                                    OutcomeModel(
                                    isError = true,
                                    msg = "Cannot get user"
                                )
                                )
                            }
                        }else{
                            callback(
                                OutcomeModel(
                                isError = true,
                                msg = "Invalid email or password"
                            )
                            )
                        }
                    }
                }else{
                    withContext(Dispatchers.Main){
                        callback(
                            OutcomeModel(
                            isError = true,
                            msg = "Provide email and password"
                        )
                        )
                    }
                }
            }
        }
    }

    fun createAccountOperation(email: String, password: String, callback: (OutcomeModel) -> Unit){
        operationScope.launch {
            authenticationManager.signUpWithEmailAndPassword(email, password){ it ->
                if(it){
                    if(contentProvider.currentUser.value!=null) {
                        authenticationManager.sendEmailVerification { verificationSent ->
                            if(verificationSent){
                                Controller.emailVerificationResendable.value = true
                                callback(
                                    OutcomeModel(
                                    isError = false,
                                    msg = "Email verification sent. Please check your inbox for the verification link.")
                                )
                                authenticationManager.logout()
                            }
                        }
                    }
                }else{
                    callback(
                        OutcomeModel(
                        isError = true,
                        msg = "Could not create account. Check your email and password. Password must be at least 8 characters")
                    )
                }
            }
        }
    }

    fun sendPasswordResetEmailOperation(email: String, callback: (OutcomeModel) -> Unit){
        authenticationManager.sendPasswordResetEmail(email){
            if(it){
                callback(
                    OutcomeModel(
                    isError = false,
                    msg = "Password reset email sent"
                )
                )
            }else{
                callback(
                    OutcomeModel(
                    isError = true,
                    msg = "Could not send password reset email"
                )
                )
            }
        }
    }

    fun profileUpdateOperation(
        userModel: UserModel,
        image: ImageBitmap?,
        callback: (OutcomeModel) -> Unit = {}){

        contentRepository.updateDocument(
            collectionPath = Databases.Collections.USERS,
            documentId = userModel.id,
            data = userModel.toMap()
        ){ success, error ->
            if(!success){
                callback(OutcomeModel(isError = true, msg = "${error?.message}"))
            }else{
                contentProvider.userProfile.value = userModel
                image.ifNotNull { bitmap ->
                    contentRepository.uploadImage(
                        bucket = Databases.Buckets.USER_PROFILE_IMAGES,
                        imageBytes = bitmap.toByteArray(),
                        imageName = userModel.id
                    ){ outcome ->
                        val userTag = contentProvider.tags.value.find { it.userId == userModel.id }
                        userTag
                            .ifNotNull {
                                contentRepository.updateDocument(
                                    collectionPath = Databases.Collections.TAGS,
                                    documentId = it.id,
                                    data = it.copy(
                                        name = userModel.firstName +" "+userModel.lastName
                                    ).toMap()
                                ){
                                    _, _ ->
                                    Initializer.runInit(context){
                                        callback(outcome)
                                    }
                                }
                            }
                            .ifNull {
                                Initializer.runInit(context){
                                    callback(outcome)
                                }
                            }
                    }
                }
                image.ifNull {
                    callback(
                        OutcomeModel(
                            isError = false,
                            msg = "Profile updated"
                        )
                    )
                }
            }
        }
    }

    fun profileSetupOperation(
        userModel: UserModel,
        image: ImageBitmap,
        callback: (OutcomeModel) -> Unit = {}){

        contentRepository.createDocument(
            collectionPath = Databases.Collections.USERS,
            documentId = userModel.id,
            data = userModel.toMap()
        ){ firstErrorModel ->
            if(firstErrorModel.isError){
                callback(firstErrorModel)
            }else{
                savePreferences("minorColor","0xFFFFE0B2",context)
                savePreferences("majorColor","0xFFFB8C00",context)
                savePreferences("textThemeColor","0xFF000000",context)
                contentProvider.userProfile.value = userModel
                contentRepository.uploadImage(
                    bucket = Databases.Buckets.USER_PROFILE_IMAGES,
                    imageBytes = image.toByteArray(),
                    imageName = userModel.id
                ){
                    contentProvider.userTag.value = TagsModel(
                        id = userModel.tag,
                        userId = userModel.id,
                        name = "${ userModel.firstName } ${ userModel.lastName }",
                        type = TCDataTypes.TagType.PERSON,
                        photoUrl = userModel.profileImage
                    )
                    contentRepository.createDocument(
                        collectionPath = Databases.Collections.TAGS,
                        documentId = userModel.tag,
                        data = TagsModel(
                            id = userModel.tag,
                            userId = userModel.id,
                            name = "${ userModel.firstName } ${ userModel.lastName }",
                            type = TCDataTypes.TagType.PERSON,
                            photoUrl = userModel.profileImage
                        ).toMap()
                    ){
                        if(!it.isError){
                            Initializer.initTags(context){}
                        }
                        callback(it)
                    }
                }
            }
        }
    }

    fun updateConversationOperation(id: String, updateMedia:Boolean = false, callback: (Boolean) -> Unit = {}){
        contentRepository.updateDocument(
            collectionPath = Databases.Collections.CONVERSATIONS,
            documentId = id,
            data = if(updateMedia){
                contentProvider.currentChat.value?.copy(
                    currentMediaLink = contentProvider.nowPlaying.value,
                    mediaLinks = contentProvider.currentPlaylist.value.toMutableList()
                )!!.toMap()
            }else{
                contentProvider.currentChat.value!!.toMap()
            }
        ){
            success, error ->
            callback(success)
        }
    }

    fun sendMessageToStagingOperation(messageModel: MessageModel, id:String,callback: (OutcomeModel) -> Unit = {}){
        contentRepository.createDocument(
            collectionPath = Databases.Collections.STAGING,
            documentId = id,
            data = messageModel.toMap()
        ){
            callback(it)
        }
    }

    fun sendConversationManagementRequest(editModel: ConversationEditModel,callback: (OutcomeModel) -> Unit = {}){
        contentRepository.createDocument(
            collectionPath = Databases.Collections.CHAT_MANAGEMENT,
            documentId = editModel.conversationId,
            data = editModel.toMap()
        ){
            callback(it)
        }
        val conv = contentProvider.conversations.value.find { it.id == editModel.conversationId }
        operationScope.launch {
            delay(250)
            conv.ifNotNull {
                withContext(Dispatchers.Main){
                    contentProvider.conversations.value = contentProvider.conversations.value.minus(it)
                }
            }
        }
    }

    fun sendMediaDeletionRequest(
        mediaDeleteRequest: MediaDeleteRequest,
        callback: (OutcomeModel) -> Unit = {}){
        contentRepository.updateDocumentByField(
            collectionPath = Databases.Collections.MEDIA_MANAGEMENT,
            documentId = Databases.Documents.DELETE_MEDIA,
            field = Databases.Fields.DELETION_LIST,
            data = FieldValue.arrayUnion(mediaDeleteRequest.toMap())
        ){
            success, error ->
            if(success){
                callback(
                    OutcomeModel(
                        isError = false,
                        msg = "Media deletion request sent"
                    )
                )
            }else{
                callback(
                    OutcomeModel(
                        isError = true,
                        msg = "Media deletion request failed: ${error?.message}"
                    )
                )
            }
        }
    }

    fun sendConnectionRequest(request: TrackConnectionRequestModel){
        contentRepository.createDocument(
            collectionPath = Databases.Collections.CONNECTION_REQUESTS,
            documentId = getCurrentDate() + "~" + getCurrentTime(),
            data = request.toMap()
        ){
            if(it.isError){
                getToast(context,"Error: connecting to ${request.targetName}")
            }
        }
    }

    fun restrictUserOperation(id: String, restrictionType: RestrictionType, callback: (Boolean) -> Unit = {}){
        if(id.isNotEmpty()){
            val blockedUsers = contentProvider.restrictedUsers.value.toMutableList()

            if (restrictionType == RestrictionType.BLOCK) {
                blockedUsers.add(id)
            } else {
                blockedUsers.remove(id)
            }

            val blockedUsersMap = mapOf("blockedUsers" to blockedUsers)

            getUserUid().ifNotNull {
                contentRepository.updateDocument(
                    collectionPath = Databases.Collections.RESTRICTIONS,
                    documentId = it,
                    data = blockedUsersMap
                ) { success, error ->
                    if (success) {
                        contentProvider.restrictedUsers.value = blockedUsers.toList()
                        Controller.reloadRestrictions.value = !Controller.reloadRestrictions.value
                        callback(true)
                        getToast(context, "User has been blocked")
                    } else {
                        getToast(context, "Error blocking user")
                        callback(false)
                    }
                }
            }
        }
    }

    fun deleteAccountOperation(){
        val userId = getUserUid()
        userId.ifNotNull { uid ->
            contentRepository.createDocument(
                Databases.Collections.ACCOUNT_DELETION_REQUESTS,
                documentId = getCurrentDate() + "~" + getCurrentTime(),
                data = mapOf("userId" to uid)
            ){
                val userTag = contentProvider.tags.value.find { it.userId == getUserUid() }
                userTag.ifNotNull {
                    contentRepository.deleteDocument(
                        Databases.Collections.TAGS,
                        documentId = it.id
                    ){ success, error ->
                        operationScope.launch {
                            withContext(Dispatchers.Main){
                                contentProvider.deleteAccountMessage.value = "Exiting..."
                            }
                            delay(2000)
                            withContext(Dispatchers.Main){
                                DataManager(context).clearAppData()
                                exitProcess(0)
                            }
                        }
                    }
                }
                    .ifNull {
                        operationScope.launch {
                            withContext(Dispatchers.Main){
                                contentProvider.deleteAccountMessage.value = "Exiting..."
                            }
                            delay(2000)
                            withContext(Dispatchers.Main){
                                DataManager(context).clearAppData()
                                exitProcess(0)
                            }
                        }
                    }
            }
        }
            .ifNull {
                getToast(context, "Error sending account deletion request")
            }
    }

    fun freeSpaceOperation(onComplete:()->Unit={}){
        operationScope.launch {
            val dataManager = DataManager(context = context)
            dataManager.free(Databases.Local.AUDIO_DB)
            dataManager.free(Databases.Local.IMAGES_DB)
            dataManager.clearAppData(clearCache = true, clearFiles = false, clearSharedPrefs = false, clearDatabases = false)
            withContext(Dispatchers.Main){
                onComplete.invoke()
            }
        }
    }
}












