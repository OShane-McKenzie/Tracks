package com.litecodez.tracksc.services

import android.util.Log

class CustomSecurityManager : SecurityManager() {
    override fun checkExit(status: Int) {
        // Simply ignore the exit call
        // Log the attempt if you want to monitor it
        Log.e("CustomSecurityManager", "Ignored attempt to call System.exit() with status: $status")
    }
}