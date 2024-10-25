package com.litecodez.tracksc.objects

import android.content.Context
import android.util.Log
import java.io.File

class DataManager(private val context: Context) {
    /**
     * Clears different types of app data based on the specified options
     * @param clearCache Whether to clear the app's cache
     * @param clearFiles Whether to clear the app's files
     * @param clearSharedPrefs Whether to clear shared preferences
     * @param clearDatabases Whether to clear databases
     */
    fun clearAppData(
        clearCache: Boolean = true,
        clearFiles: Boolean = true,
        clearSharedPrefs: Boolean = true,
        clearDatabases: Boolean = true
    ) {
        try {
            // Clear cache
            if (clearCache) {
                context.cacheDir?.deleteRecursively()
                context.externalCacheDir?.deleteRecursively()
            }

            // Clear files
            if (clearFiles) {
                context.filesDir.deleteRecursively()
                context.getExternalFilesDir(null)?.deleteRecursively()
            }

            // Clear shared preferences
            if (clearSharedPrefs) {
                val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
                sharedPrefsDir.deleteRecursively()
            }

            // Clear databases
            if (clearDatabases) {
                val databasesDir = File(context.applicationInfo.dataDir, "databases")
                databasesDir.deleteRecursively()
            }

        } catch (e: Exception) {
            Log.e("DataManager", "Error clearing app data", e)
            //throw e
        }
    }

    /**
     * Clears specific shared preferences file
     * @param prefsName Name of the shared preferences file
     */
    fun clearSpecificSharedPreferences(prefsName: String) {
        try {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        } catch (e: Exception) {
            Log.e("DataManager", "Error clearing shared preferences: $prefsName", e)
            throw e
        }
    }
}