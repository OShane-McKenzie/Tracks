package com.litecodez.tracksc.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat
import com.litecodez.tracksc.MainActivity
import com.litecodez.tracksc.R
import com.litecodez.tracksc.notificationWatcher
import com.litecodez.tracksc.objects.CustomWebView

class YouTubePlayerService : Service() {
    private var webView: CustomWebView? = null
    private val binder = LocalBinder()
    private val FOREGROUND_SERVICE_ID = 1002
    inner class LocalBinder : Binder() {
        fun getService(): YouTubePlayerService = this@YouTubePlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createWebView()
        startForegroundService()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView() {
        webView = CustomWebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            webViewClient = WebViewClient()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createWebView()
        createNotification()
        return START_STICKY
    }


    private fun startForegroundService() {
        val notification = createNotification()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FOREGROUND_SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(FOREGROUND_SERVICE_ID, notification)
        }
    }

    fun loadVideo(videoId: String) {
        val html = """
            <!DOCTYPE html>
            <html>
              <body>
                <div id="player"></div>
                <script src="https://www.youtube.com/iframe_api"></script>
                <script>
                  var player;
                  function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                      height: '100%',
                      width: '100%',
                      videoId: '$videoId',
                      playerVars: {
                        'playsinline': 1,
                        'enablejsapi': 1,
                        'autoplay': 1
                      },
                      events: {
                        'onReady': onPlayerReady
                      }
                    });
                  }
                  function onPlayerReady(event) {
                    event.target.playVideo();
                  }
                </script>
              </body>
            </html>
        """.trimIndent()
        webView?.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "youtube_player_channel"
        val channelName = "YouTube Player"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracks Player Service")
            .setContentText("Alive")
            .setSmallIcon(R.drawable.play)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }

    fun play() {
        webView?.evaluateJavascript("player.playVideo();", null)
    }

    fun pause() {
        webView?.evaluateJavascript("player.pauseVideo();", null)
    }

    fun reload() {
        webView?.reload()
    }

    fun isPlaying(): Boolean {
        var result = false
        webView?.evaluateJavascript("player.getPlayerState();") { state ->
            result = state.toIntOrNull() == 1 // 1 is the code for "playing" state
        }
        return result
    }

    fun getCurrentPosition(): Long {
        var position = 0L
        webView?.evaluateJavascript("player.getCurrentTime();") { time ->
            position = ((time.toFloatOrNull() ?: (0f * 1000))).toLong()
        }
        return position
    }

    fun seekTo(position: Long) {
        webView?.evaluateJavascript("player.seekTo(${position.toFloat() / 1000});", null)
    }
    fun hasVideoEnded(): Boolean {
        var ended = false
        webView?.evaluateJavascript("player.getPlayerState();") { state ->
            ended = state.toIntOrNull() == 0 // 0 is the code for "ended" state
        }
        return ended
    }

    override fun onDestroy() {
        super.onDestroy()
        super.onDestroy()
        // Clean up resources
        webView?.destroy()
        webView = null
        // Stop any ongoing processes or unregister any broadcast receivers
        STOP_FOREGROUND_REMOVE
    }
}
