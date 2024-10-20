package com.litecodez.tracksc.models

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class AudioPlayer : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow<Int?>(null)
    val duration: StateFlow<Int?> = _duration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var positionUpdateJob: Job? = null

    fun playAudio(file: File, onCompletion: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        try {
            // First check if file exists and is readable
            if (!file.exists()) {
                val message = "Audio file does not exist: ${file.absolutePath}"
                _error.value = message
                onError?.invoke(message)
                return
            }

            if (!file.canRead()) {
                val message = "Cannot read audio file: ${file.absolutePath}"
                _error.value = message
                onError?.invoke(message)
                return
            }

            // Clean up any existing MediaPlayer
            cleanupMediaPlayer()
            stopPositionUpdates()

            // Create new MediaPlayer instance
            mediaPlayer = MediaPlayer().apply {
                // Set audio stream type
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                // Error listeners should be set before any operations
                setOnErrorListener { mp, what, extra ->
                    val errorMessage = when (what) {
                        MediaPlayer.MEDIA_ERROR_UNKNOWN -> "Unknown media player error: $extra"
                        MediaPlayer.MEDIA_ERROR_SERVER_DIED -> "Server died: $extra"
                        else -> "Error: what=$what, extra=$extra"
                    }
                    _error.value = errorMessage
                    _isPlaying.value = false
                    onError?.invoke(errorMessage)
                    cleanupMediaPlayer()
                    true
                }

                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                        _isPlaying.value = true
                        _duration.value = mp.duration
                        startPositionUpdates()
                    } catch (e: Exception) {
                        val message = "Error starting playback: ${e.message}"
                        _error.value = message
                        onError?.invoke(message)
                        cleanupMediaPlayer()
                    }
                }

                setOnCompletionListener {
                    onCompletion?.invoke()
                    stopPositionUpdates()
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    cleanupMediaPlayer()
                }

                try {
                    // Try using FileDescriptor instead of direct path
                    val fis = FileInputStream(file)
                    setDataSource(fis.fd)
                    fis.close()

                    prepareAsync()
                } catch (e: Exception) {
                    val message = "Error setting data source: ${e.message}"
                    _error.value = message
                    onError?.invoke(message)
                    cleanupMediaPlayer()
                }
            }
        } catch (e: Exception) {
            val message = "Error initializing player: ${e.message}"
            _error.value = message
            onError?.invoke(message)
            cleanupMediaPlayer()
        }
    }

    private fun cleanupMediaPlayer() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
            mediaPlayer = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _duration.value = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAudio() {
        stopPositionUpdates()
        cleanupMediaPlayer()
    }

    fun pauseAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    _isPlaying.value = false
                    stopPositionUpdates()
                }
            }
        } catch (e: Exception) {
            _error.value = "Error pausing: ${e.message}"
            e.printStackTrace()
        }
    }

    fun resumeAudio() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    _isPlaying.value = true
                    startPositionUpdates()
                }
            }
        } catch (e: Exception) {
            _error.value = "Error resuming: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _currentPosition.value = it.currentPosition
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
