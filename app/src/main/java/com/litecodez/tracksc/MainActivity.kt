package com.litecodez.tracksc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.litecodez.tracksc.components.CustomSnackBar
import com.litecodez.tracksc.components.ImageAnimation
import com.litecodez.tracksc.components.setColorIfDarkTheme
import com.litecodez.tracksc.models.YouTubePlayerViewModel
import com.litecodez.tracksc.objects.AppNavigator
import com.litecodez.tracksc.objects.AuthenticationManager
import com.litecodez.tracksc.objects.ContentProvider
import com.litecodez.tracksc.objects.ContentRepository
import com.litecodez.tracksc.objects.Controller
import com.litecodez.tracksc.objects.CustomExceptionHandler
import com.litecodez.tracksc.objects.Operator
import com.litecodez.tracksc.objects.Watchers
import com.litecodez.tracksc.services.CustomSecurityManager
import com.litecodez.tracksc.ui.theme.TracksTheme

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
        val tcYouTubePlayerViewModel = YouTubePlayerViewModel(this.application)
        // Initialize authentication manager
        val authenticationManager = AuthenticationManager(this, context = applicationContext)
        val operator = Operator(context = applicationContext, authenticationManager = authenticationManager)

        // Initialize the launcher for the POST_NOTIFICATIONS permission
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Controller.isPostNotificationPermissionGranted.value = isGranted
        }
        Thread.setDefaultUncaughtExceptionHandler(Thread.getDefaultUncaughtExceptionHandler()
            ?.let { CustomExceptionHandler(it) })
        // Check and request notification permission
        requestPermission()

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        // Check and request the user to disable battery optimizations
        //requestDisableBatteryOptimizations()

        try {
            // Load preferences for theme colors and wallpaper
            val minor = loadPreferences(applicationContext, "minorColor", "0xFFAC9D9D")
                .trim()
                .replace("0x", "")
                .toLong(16)
            val major = loadPreferences(applicationContext, "majorColor", "0xFFB17E01")
                .trim()
                .replace("0x", "")
                .toLong(16)
            val textTheme = loadPreferences(applicationContext, "textThemeColor", "0xFF000000")
                .trim()
                .replace("0x", "")
                .toLong(16)
            val wallpaper = loadPreferences(applicationContext, "wallpaper", "six")

            // Set theme colors and wallpaper in content provider
            contentProvider.majorThemeColor.value = Color(major)
            contentProvider.minorThemeColor.value = Color(minor)
            contentProvider.textThemeColor.value = Color(textTheme)
            contentProvider.wallpaper.intValue = contentProvider.wallpaperMap[wallpaper] ?: R.drawable.tracks_bg_6

        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            TracksTheme {
                if (Controller.isPostNotificationPermissionGranted.value) {
                    // Render the main content if notification permission is granted
                    Root(operator, authenticationManager, tcYouTubePlayerViewModel)
                } else {
                    // Render a notification permission prompt if not granted
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
        // Check and request POST_NOTIFICATIONS permission for Android Tiramisu (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Controller.isPostNotificationPermissionGranted.value = true
                }
                else -> {
                    // Request permission if not granted
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // If the OS version is lower than Tiramisu, assume permission is granted
            Controller.isPostNotificationPermissionGranted.value = true
        }
    }

    private fun openNotificationSettings() {
        // Open the notification settings for this app
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    @SuppressLint("BatteryLife")
    private fun requestDisableBatteryOptimizations() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = applicationContext.packageName

        // Check if battery optimization is ignoring your app
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if the notification permission is still granted when resuming the app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Controller.isPostNotificationPermissionGranted.value = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

