package com.litecodez.tracksc.objects

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import com.google.firebase.firestore.FieldValue
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getCurrentDate
import com.litecodez.tracksc.getCurrentTime
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.home
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.login
import com.litecodez.tracksc.models.ConversationEditModel
import com.litecodez.tracksc.models.MediaDeleteRequest
import com.litecodez.tracksc.models.MessageModel
import com.litecodez.tracksc.models.OutcomeModel
import com.litecodez.tracksc.models.TagsModel
import com.litecodez.tracksc.models.TrackConnectionRequestModel
import com.litecodez.tracksc.models.UserModel
import com.litecodez.tracksc.profile
import com.litecodez.tracksc.savePreferences
import com.litecodez.tracksc.toByteArray
import com.litecodez.tracksc.toMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Operator(
    private val context: Context,
    private val authenticationManager: AuthenticationManager
) {
    val operationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun splashOperation(callback: (OutcomeModel) -> Unit={}){
//        operationScope.launch {
//            createFile(context, fileLocation = Databases.Local.IMAGES_DB, fileName = Databases.Local.IMAGE_DATA, content = "{\"images\":{}}")
//            readImagesFile(context, fileLocation = Databases.Local.IMAGES_DB, fileName = Databases.Local.IMAGE_DATA){
//                CoroutineScope(Dispatchers.Main).launch{
//                    contentProvider.localImages.value = it
//                }
//
//                contentProvider.localImages.value.images.forEach {
//                    contentProvider.loadedImageBytes[it.key] = it.value.data.toByteArray()
//                }
//            }
//        }
        Controller.firstLaunch.value = true
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
                                        msg = "Email not verified"
                                    )
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
                                    msg = "Email verification sent. Please check your inbox for the verification link."
                                )
                                )
                                authenticationManager.logout()
                            }
                        }
                    }
                }else{
                    callback(
                        OutcomeModel(
                        isError = true,
                        msg = "Could not create account. Check your email and password. Password must be at least 8 characters"
                    )
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
                savePreferences("minorColor","0xFFBBDEFB",context)
                savePreferences("majorColor","0xFF3949AB",context)
                savePreferences("textThemeColor","0xFFFFFFFF",context)
                contentProvider.userProfile.value = userModel
                contentRepository.uploadImage(
                    bucket = Databases.Buckets.USER_PROFILE_IMAGES,
                    imageBytes = image.toByteArray(),
                    imageName = userModel.id
                ){
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

    fun updateConversationOperation(id: String, updateMedia:Boolean = false,callback: (Boolean) -> Unit = {}){
        contentRepository.updateDocument(
            collectionPath = Databases.Collections.CONVERSATIONS,
            documentId = id,
            data = if(updateMedia){
                contentProvider.currentChat.value?.copy(currentMediaLink = contentProvider.nowPlaying.value)!!.toMap()
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
                        getToast(context, "Error: ${error?.message}")
                        callback(false)
                    }

                }
            }
        }
    }
}












