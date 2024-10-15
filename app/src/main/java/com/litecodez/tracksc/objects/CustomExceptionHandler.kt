package com.litecodez.tracksc.objects

import android.os.TransactionTooLargeException
import android.util.Log

class CustomExceptionHandler(private val defaultHandler: Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        if (throwable is TransactionTooLargeException) {
            // Handle TransactionTooLargeException
            Log.e("CustomExceptionHandler", "TransactionTooLargeException caught", throwable)
        } else {
            // If it's not a TransactionTooLargeException, use the default handler
            defaultHandler.uncaughtException(thread, throwable)
        }
    }
}