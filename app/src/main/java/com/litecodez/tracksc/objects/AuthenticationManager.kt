package com.litecodez.tracksc.objects

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

import com.litecodez.tracksc.MainActivity
import com.litecodez.tracksc.appNavigator
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.home
import com.litecodez.tracksc.isValidEmail
import com.litecodez.tracksc.profile
import com.litecodez.tracksc.then
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Manages user authentication using Firebase Authentication and Google Sign-In API.
 * Provides functions for signing in and out, as well as checking the authentication state.
 *
 * @property activity The parent [ComponentActivity] instance used for registering activity result launcher.
 * @property context The [Context] used for resource retrieval and displaying toasts.
 */
class AuthenticationManager(private val activity: ComponentActivity, context: Context) {
    // Private properties for Firebase Authentication and Google Sign-In client

    private val clientId:String = "615695821046-met6hr1qf3qpcdv8j9pgmhqt68opr6b3.apps.googleusercontent.com"
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestServerAuthCode(clientId)
            .requestEmail()
            .requestId()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(activity, gso)
    }
    
    private val thisContext = context
    private val signInLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleSignInResult(result)
        }
    /**
     * Initiates Google Sign-In flow. Launches the Google Sign-In activity.
     *
     * @throws ApiException if the Google Sign-In API encountered an error.
     */
    fun signInWithGoogle(onComplete:()->Unit = {}) {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent).also {
            onComplete()
        }
    }

    /**
     * Handles the result of Google Sign-In activity and authenticates the user with Firebase.
     *
     * @param result The result of the Google Sign-In activity.
     */
    private fun handleSignInResult(result: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { firebaseAuthWithGoogle(it) }
            //dataProvider.googleSignInProcessComplete.value = true
        } catch (e: ApiException) {
            Controller.googleSignInProcessComplete.value = true
            Log.w(ContentValues.TAG, "Google sign in failure", task.exception)
            //getToast(thisContext,"Error: Google sign in failure.", long = true)
            Controller.isGoogleSignInFailure.value = true
        }
    }

    /**
     * Attempts to sign in with the provided email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @param runTask A lambda function to execute after the authentication attempt, indicating success or failure.
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String, runTask:(Boolean)->Unit={}) {
        try{
            if(email.trim().isNotEmpty() && password.isNotEmpty()){
                if(email.trim().isValidEmail()) {
                    FirebaseCenter.getAuth().signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                contentProvider.currentUser.value = FirebaseCenter.getAuth().currentUser
                                runTask(true)
                            } else {
                                runTask(false)
                            }
                        }
                        .addOnFailureListener(activity){
                            getToast(context = thisContext,"Error", long = true)
                        }
                }else{
                    withContext(Dispatchers.Main){
                        getToast(context = thisContext,"Invalid email.")
                        runTask(false)
                    }

                }
            }else{
                withContext(Dispatchers.Main){
                    getToast(context = thisContext,"Required fields are empty.")
                    runTask(false)
                }
            }
        }catch(e: Exception){

            withContext(Dispatchers.Main){
                getToast(context = thisContext,"Error", long = true)
                runTask(false)
            }
        }
    }

    /**
     * Attempts to create a new user account with the provided email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @param runTask A lambda function to execute after the authentication attempt, indicating success or failure.
     */
    suspend fun signUpWithEmailAndPassword(email: String, password: String,runTask:(Boolean)->Unit={}) {
        try{
            if(email.trim().isNotEmpty() && password.isNotEmpty() && password.length >= 8) {
                if(email.trim().isValidEmail()) {
                    FirebaseCenter.getAuth().createUserWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                contentProvider.currentUser.value = FirebaseCenter.getAuth().currentUser
                                runTask(true)
                            } else {
                                runTask(false)
                            }
                        }
                        .addOnFailureListener(activity){
                            getToast(context = thisContext,"Error", long = true)
                        }
                }else{
                    withContext(Dispatchers.Main){
                        getToast(context = thisContext,"Invalid email.")
                        runTask(false)
                    }

                }
            }else{
                withContext(Dispatchers.Main){
                    getToast(context = thisContext,"Required fields are empty.")
                    runTask(false)
                }
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                getToast(context = thisContext,"Error", long = true)
                runTask(false)
            }
        }

    }

    /**
     * Authenticates the user with Firebase using the provided Google Sign-In ID token.
     *
     * @param idToken The ID token received from Google Sign-In.
     * @param context The context used for displaying toasts (optional, defaults to the class context).
     */
    private fun firebaseAuthWithGoogle(idToken: String,context: Context = thisContext) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseCenter.getAuth().signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Controller.googleSignInProcessComplete.value = true
                    contentProvider.currentUser.value = FirebaseCenter.getAuth().currentUser
                    isFirstLogin { firstTimeLogin->
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

                } else {
                    Controller.googleSignInProcessComplete.value = true
                    getToast(context = context, msg = "Unable to sign you in at this time.")
                }
            }
    }

    /**
     * Checks if the user is currently logged in.
     *
     * @return `true` if the user is logged in, `false` otherwise.
     */
    fun isUserLoggedIn(): Boolean {
        contentProvider.currentUser.value = FirebaseCenter.getAuth().currentUser
        return contentProvider.currentUser.value != null
    }

    /**
     * Signs out the user by revoking Google Sign-In access and Firebase authentication.
     */
    fun signOut(onComplete: () -> Unit = {}){
        googleSignInClient.revokeAccess().addOnCompleteListener(activity) {
            //activity.stopService(Intent(thisContext, ListenerService::class.java))
            FirebaseCenter.getAuth().signOut()
            onComplete()
        }
    }

    fun isFirstLogin(callback: (Boolean) -> Unit = {_->}) {
        val db = FirebaseCenter.getDatabase()
        val currentUser = contentProvider.currentUser.value

        if (currentUser != null) {
            val docRef = db.collection(Databases.Collections.USERS).document(currentUser.uid)
            docRef.get()

                .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.data != null) {
                        val isFirstTimeLogin = task.result.data?.get("isFirstTimeLogin") as Boolean? ?: true

                        callback(isFirstTimeLogin)
                    } else {
                        // Handle the case where the document does not exist
                        callback(true)
                    }
                } else {
                    // Handle errors here
                    callback(true)
                }
            }
        } else {
            // Handle the case where the current user is null
            callback(false)
        }
    }
    /**
     * Sends a verification email to the current user's email address.
     *
     * @param onComplete A lambda function to execute after the verification email is sent.
     */
    fun sendEmailVerification(onComplete: (Boolean) -> Unit = {}) {
        val user = FirebaseCenter.getAuth().currentUser

        user?.sendEmailVerification()
            ?.addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    // Handle the case where the verification email could not be sent
                    Log.e(ContentValues.TAG, "Error sending verification email", task.exception)
                    getToast(context = thisContext, msg = "Error sending verification email")
                    onComplete(false)
                }
            }
    }

    /**
     * Checks if the current user's email address has been verified.
     *
     * @return `true` if the email is verified, `false` otherwise.
     */
    fun isEmailVerified(): Boolean {
        val user = FirebaseCenter.getAuth().currentUser
        return user?.isEmailVerified == true
    }
    /**
     * Deletes the current user account.
     *
     * @param onComplete A lambda function to execute after the account deletion is attempted.
     */
    fun deleteUserAccount(onComplete: (Boolean) -> Unit = {}) {
        val user = contentProvider.currentUser.value
        googleSignInClient.revokeAccess().addOnCompleteListener(activity) {
            user?.delete()
                ?.addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        FirebaseCenter.getAuth().signOut().then {
                            onComplete(true)
                        }

                    } else {
                        // Account deletion failed
                        Log.e("Error deleting user account", "Error deleting user account", task.exception)
                        getToast(context = thisContext, msg = "Error deleting user account",long = true)
                        onComplete(false)
                    }
                }
        }
    }

    fun reset(){
        val intent  = Intent(thisContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        thisContext.startActivity(intent)
    }

    fun logout(){
        try {
            FirebaseCenter.getAuth().signOut()
            googleSignInClient.revokeAccess()
        }catch (e:Exception){
            getToast(context = thisContext,"Error", long = true)
        }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean) -> Unit = {}){
        FirebaseCenter.getAuth().sendPasswordResetEmail(email).addOnCompleteListener(activity){
            if(it.isSuccessful){
                onComplete(true)
            }else{
                onComplete(false)
            }
        }
    }
}