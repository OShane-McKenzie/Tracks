package com.litecodez.tracksc

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.litecodez.tracksc.components.CustomSnackBar
import com.litecodez.tracksc.components.ImageAnimation
import com.litecodez.tracksc.components.setColorIfDarkTheme
import com.litecodez.tracksc.objects.AppNavigator
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.ContentProvider
import com.litecodez.tracksc.ui.theme.TracksTheme
import com.litecodez.tracksc.objects.ContentRepository
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.services.ConversationService
import android.provider.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.litecodez.tracksc.objects.Watchers

val appNavigator = AppNavigator()
val contentProvider = ContentProvider()
val contentRepository = ContentRepository()
val conversationWatcher = Watchers()
val notificationWatcher = Watchers()
val tagsWatcher = Watchers()
val tcConnectionWatcher = Watchers()
class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authenticationManager = AuthenticationManager(this, context = applicationContext)
        val operator = Operator(context = applicationContext, authenticationManager = authenticationManager)

        // Initialize the launcher for the POST_NOTIFICATIONS permission
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Controller.isPostNotificationPermissionGranted.value = isGranted
        }

        // Check for notification permission
        requestPermission()

        enableEdgeToEdge()
        setContent {

            TracksTheme {
                if (Controller.isPostNotificationPermissionGranted.value) {
                    Root(operator, authenticationManager)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(34.dp),
                    ) {
                        Button(
                            modifier = Modifier.align(Alignment.TopCenter),
                            onClick = { openNotificationSettings() }
                        ) {
                            Text("Open Notification Settings")
                        }
                        ImageAnimation(
                            image = R.drawable.tc_logo_no_bg,
                            colorAnim = false,
                            startDelay = 0.5f,
                            size = 200,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    CustomSnackBar(
                        info = "Please enable notification permission to continue",
                        containerColor = setColorIfDarkTheme(lightColor = Color.White, darkColor = Color.Black),
                        textColor = setColorIfDarkTheme(lightColor = Color.White, darkColor = Color.Black, invert = false),
                        isVisible = true,
                        duration = 100000
                    )
                }
            }
        }
    }

    private fun requestPermission() {
        // Check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Controller.isPostNotificationPermissionGranted.value = true
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }else{
            Controller.isPostNotificationPermissionGranted.value = true
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Check if the notification permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Controller.isPostNotificationPermissionGranted.value = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

