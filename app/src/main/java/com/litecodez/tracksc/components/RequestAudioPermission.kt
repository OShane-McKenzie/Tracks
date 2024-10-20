package com.litecodez.tracksc.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestAudioPermission(onPermissionResult: (Boolean) -> Unit) {
    val context = LocalContext.current

    // Launcher to request the RECORD_AUDIO permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Callback when the permission result is available
        onPermissionResult(isGranted)
    }

    // Check if the permission is already granted
    LaunchedEffect(Unit) {
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted) {
            // Permission is already granted
            onPermissionResult(true)
        } else {
            // Request the RECORD_AUDIO permission
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
