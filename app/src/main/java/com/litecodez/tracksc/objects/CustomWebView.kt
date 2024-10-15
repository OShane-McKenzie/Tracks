package com.litecodez.tracksc.objects

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.os.TransactionTooLargeException
import android.util.Log
import android.webkit.WebBackForwardList
import android.webkit.WebView


class CustomWebView(context: Context) : WebView(context) {
    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(): Parcelable {
        // Return an empty Bundle instead of saving any state
        try{
            return Bundle()
        }catch (e: Exception) {
            if (e.cause is TransactionTooLargeException) {
                Log.e("SaveInstanceState", "TransactionTooLargeException caught", e)
                return Bundle()
            } else {
                throw e
            }
        }

    }

    @SuppressLint("MissingSuperCall")
    override fun onRestoreInstanceState(state: Parcelable?) {
        // Do nothing when restoring state
    }

    override fun saveState(outState: Bundle): WebBackForwardList? {
        // Clear any existing data in the outState bundle
        try{ outState.clear() }
        catch (e: Exception) {
            if (e.cause is TransactionTooLargeException) {
                Log.e("SaveInstanceState", "TransactionTooLargeException caught", e)
            } else {
                throw e
            }
        }
        // Return null to indicate no state was saved
        return null
    }

    override fun restoreState(inState: Bundle): WebBackForwardList? {
        // Do nothing when restoring state
        return null
    }

    override fun loadUrl(url: String) {
        // Prevent WebView from saving the URL to browser history
        super.loadUrl(url, mapOf("X-Requested-With" to ""))
    }
}
