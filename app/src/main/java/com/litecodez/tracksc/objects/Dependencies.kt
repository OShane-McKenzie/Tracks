package com.litecodez.tracksc.objects

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import com.litecodez.tracksc.models.AudioPlayer
import com.litecodez.tracksc.models.AudioRecorder
import com.litecodez.tracksc.models.YouTubePlayerViewModel
import java.io.File


class Dependencies(
    private val context: Context,
    private val application: Application,
    private val activity: ComponentActivity
):ViewModel() {

    private val path: File = context.filesDir
    private val dir = File(path, Databases.Local.AUDIO_DB)
    val tcYouTubePlayerViewModel = YouTubePlayerViewModel(application)
    val authenticationManager = AuthenticationManager(activity = activity, context = context)
    val operator = Operator(context = context, authenticationManager = authenticationManager)
    val audioRecorder = AudioRecorder(context, dir)
    val imageSharer = ImageSharer()

}