package com.litecodez.tracksc.launchers

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.litecodez.tracksc.MainActivity
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.services.ConversationService

class ConversationLauncher : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle any new intents that come in while the activity is running
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val chatId = intent?.getStringExtra("ACTION")
        val notificationId = intent?.getIntExtra("NOTIFICATION_ID", 0)

        if (notificationId != null && notificationId != 0) {
            // Cancel the notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        if (chatId != null) {
            // Process the chatId or perform any action needed
            contentProvider.chatIdFromNotification.value = chatId

            // Bring MainActivity to the front
            val newIntent = Intent(this, MainActivity::class.java)
            startActivity(newIntent)
        }

        // Finish this activity so it doesn't stay in the back stack
        finish()
    }
}