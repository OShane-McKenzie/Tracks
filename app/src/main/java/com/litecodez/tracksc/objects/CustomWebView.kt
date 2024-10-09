package com.litecodez.tracksc.objects

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.webkit.WebBackForwardList
import android.webkit.WebView


class CustomWebView(context: Context) : WebView(context) {
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        // Save the superState but nullify the webViewState
        bundle.putParcelable("superState", superState)
        bundle.putParcelable("android:webViewState", null)
        bundle.clear()
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState = state.getParcelable<Parcelable>("superState")
            super.onRestoreInstanceState(superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun restoreState(inState: Bundle): WebBackForwardList? {
        // Do nothing when restoring state to prevent restoring web history and other states
        return null
    }

}
