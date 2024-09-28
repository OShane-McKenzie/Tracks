package com.litecodez.tracksc.objects

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import com.litecodez.tracksc.getToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class Watchers {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val mainScope = CoroutineScope(Dispatchers.Main)
    private val db = FirebaseCenter.getDatabase()
    val activeListeners = mutableMapOf<String, ListenerRegistration>()

    fun watch(collection: String, target: String, callBack: (Map<String, Any>) -> Unit) {
        if (!activeListeners.containsKey(target)) {
            val listenerRegistration = db
                .collection(collection)
                .document(target)
                .addSnapshotListener { value, error ->
                    when {
                        error != null -> Log.d("Watcher", "Error getting document: $error")
                        value != null && value.exists() -> value.data?.let { callBack(it) }
                        else -> Log.d("Watcher", "Watcher for $target does not exist.")
                    }
                }
            activeListeners[target] = listenerRegistration
        } else {
            Log.d("Watcher", "Watcher for $target already exists.")
        }
    }

    fun stopWatcher(target: String) {
        try{
            activeListeners[target]?.remove()
            activeListeners.remove(target)
        }catch (e: Exception){
            Log.d("Watcher", "Error stopping watcher for $target: ${e.message}")
        }
    }

    fun stopAllWatchers() {
        try{
            activeListeners.values.forEach { it.remove() }
            activeListeners.clear()
        }catch (e: Exception){
            Log.d("Watcher", "Error stopping all watchers: ${e.message}")
        }
    }
}