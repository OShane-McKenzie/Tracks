package com.litecodez.tracksc.models

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException

class AudioRecorder(
    private val context: Context,
    private val outputDirectory: File,
) : ViewModel() {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun startRecording(fileName: String): Result<Unit> {
        if (_isRecording.value) {
            return Result.failure(IllegalStateException("Already recording"))
        }

        return try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }

            audioFile = File(outputDirectory, "$fileName.aac")
            audioFile?.let {
                if (!it.exists()) {
                    it.createNewFile()
                }
            }

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.let { recorder ->
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS) // AAC format
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)      // AAC encoder
                recorder.setAudioEncodingBitRate(96000)                        // Set bit rate to 96 kbps
                recorder.setAudioSamplingRate(32000)
                recorder.setOutputFile(audioFile?.absolutePath)
                recorder.prepare()
                recorder.start()
                _isRecording.value = true
                Result.success(Unit)
            } ?: Result.failure(IllegalStateException("Failed to initialize MediaRecorder"))

        } catch (e: IOException) {
            _error.value = "Failed to start recording: ${e.message}"
            Result.failure(e)
        } catch (e: IllegalStateException) {
            _error.value = "Recorder in invalid state: ${e.message}"
            Result.failure(e)
        }
    }

    fun stopRecording(): Result<File> {
        if (!_isRecording.value) {
            return Result.failure(IllegalStateException("Not currently recording"))
        }

        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false

            audioFile?.let {
                Result.success(it)
            } ?: Result.failure(IllegalStateException("Audio file not found"))

        } catch (e: IllegalStateException) {
            _error.value = "Failed to stop recording: ${e.message}"
            Result.failure(e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    companion object {
        private const val TAG = "AudioRecorder"
    }
}


