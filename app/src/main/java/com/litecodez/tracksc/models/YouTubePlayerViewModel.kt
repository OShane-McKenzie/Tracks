package com.litecodez.tracksc.models

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.services.YouTubePlayerService

class YouTubePlayerViewModel(application: Application) : AndroidViewModel(application) {
    @SuppressLint("StaticFieldLeak")
    private var playerService: YouTubePlayerService? = null
    var isTcPlayerPlaying by mutableStateOf(false)
    private var currentVideoId by mutableStateOf("")
    private var serviceConnection: ServiceConnection? = null
    private var bound = false

    // Flag to check if service has been connected
    private var serviceConnected = false

    // Function to load video using the player service
    fun loadVideo(videoId: String, context: Context) {
        if (currentVideoId != videoId && !Controller.isPlayListEnabled.value) {
            playerService?.loadVideo(videoId)
            currentVideoId = videoId
            isTcPlayerPlaying = true

            contentProvider.currentSong.value = contentProvider.videos.value.videos.find {
                it.id == videoId
            }
            getToast(context, "Loading media, please wait...", long = true)
        }else if(Controller.isPlayListEnabled.value){
            playerService?.loadVideo(videoId)
            currentVideoId = videoId
            isTcPlayerPlaying = true

            contentProvider.currentSong.value = contentProvider.videos.value.videos.find {
                it.id == videoId
            }
            getToast(context, "Loading media, please wait...", long = true)
        }
    }

    // Toggle play/pause functionality
    fun togglePlayPause() {
        if (isTcPlayerPlaying) {
            playerService?.pause()
        } else {
            playerService?.play()
        }
        isTcPlayerPlaying = !isTcPlayerPlaying
    }

    fun play(){
        playerService?.play()
        isTcPlayerPlaying = true
    }

    fun pause(){
        playerService?.pause()
        isTcPlayerPlaying = false
        contentProvider.playerState.intValue = -1
    }

    // Bind the service to the context and manage lifecycle events
    fun bindService(context: Context, lifecycle: Lifecycle) {
        if (serviceConnection == null) {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as YouTubePlayerService.LocalBinder
                    setPlayerService(binder.getService())
                    bound = true
                    serviceConnected = true
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    setPlayerService(null)
                    bound = false
                    serviceConnected = false
                }
            }
        }

        // Start and bind the foreground service
        val intent = Intent(context, YouTubePlayerService::class.java)
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)

        // Add observer to unbind the service on lifecycle destroy
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    unbindService(context)
                    lifecycle.removeObserver(this)
                }
            }
        })
    }

    // Set the service instance in the ViewModel
    private fun setPlayerService(service: YouTubePlayerService?) {
        playerService = service
    }

    // Unbind the service and clear connections
    private fun unbindService(context: Context) {
        if (bound) {
            context.unbindService(serviceConnection!!)
            bound = false
        }
    }

    // Cleanup when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        playerService = null
        serviceConnection = null
    }

    // Check if the player is currently playing
    fun isPlaying(): Boolean {
        return playerService?.isPlaying() ?: false
    }

    // Seek to a specific position in the video
    fun seekTo(position: Long) {
        playerService?.seekTo(position)
    }

    // Get the current position of the video
    fun getCurrentPosition(): Float {
        return playerService?.getCurrentPosition() ?: 0f
    }

    fun reLoad(){
        playerService?.reload()
    }

    fun hasVideoEnded():Boolean{
        return playerService?.hasVideoEnded()?: false
    }
}