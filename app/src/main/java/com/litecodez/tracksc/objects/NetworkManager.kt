package com.litecodez.tracksc.objects

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager {
    /**
     * Checks the internet connection status by attempting to establish a connection to a known server (https://www.google.com).
     *
     * @param context The application context for accessing system services.
     * @return `true` if the device is connected to the internet, `false` otherwise.
     */
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Get network capabilities of the active network
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        // Check if the active network has one of the valid transport types (Wi-Fi, Cellular, Ethernet)
        if (!activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
            !activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        ) {
            return false
        }

        // Attempt to establish a connection to a known server (https://www.google.com)
        return try {
            val url = URL("https://www.github.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Connection", "close")
            connection.connectTimeout = 3000
            connection.connect()

            // Get the response code from the server connection
            val responseCode = connection.responseCode
            connection.disconnect()

            // Return true if the response code indicates a successful connection (HTTP_OK)
            responseCode == HttpURLConnection.HTTP_OK
        } catch (e: IOException) {
            // Handle exceptions and return false if an error occurs during the connection attempt
            false
        }
    }
}