package com.litecodez.tracksc.objects

import androidx.compose.runtime.mutableStateOf

object Controller {
    val emailVerificationResendable = mutableStateOf(false)
    val firstLaunch = mutableStateOf(false)
    val autoLockScreenOrientation = mutableStateOf(true)
    val googleSignInProcessComplete = mutableStateOf(false)
    val imageReady = mutableStateOf(false)
    val isPostNotificationPermissionGranted = mutableStateOf(false)
    val isDelayedProfileDocument = mutableStateOf(false)
    val isServiceInitialized = mutableStateOf(false)
    val isService2Initialized = mutableStateOf(false)
    val reloadMessage = mutableStateOf(false)
    val reloadList = mutableStateOf(false)
    val isChatContainerOpen = mutableStateOf(false)
    val showWallpaperSelector = mutableStateOf(false)
}