package com.litecodez.tracksc.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.litecodez.tracksc.contentProvider
import com.litecodez.tracksc.contentRepository
import com.litecodez.tracksc.getToast
import com.litecodez.tracksc.getUserUid
import com.litecodez.tracksc.ifNotNull
import com.litecodez.tracksc.objects.Databases
import com.litecodez.tracksc.objects.Initializer
import com.litecodez.tracksc.tcConnectionWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TCConnectionService : LifecycleService() {

    //private val tcConnectionWatcher = Watchers()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startConnectionMonitoring()
        return START_STICKY
    }

    private fun startConnectionMonitoring() {
        lifecycleScope.launch {
            getUserUid()?.let { userRequestOutcomeDoc ->
                monitorConnectionRequests(userRequestOutcomeDoc)
            }
        }
    }

    private suspend fun monitorConnectionRequests(userRequestOutcomeDoc: String) {
        tcConnectionWatcher.watch(
            collection = Databases.Collections.CONNECTION_REQUEST_OUTCOME,
            target = userRequestOutcomeDoc
        ) { data ->
            processConnectionRequestOutcomes(data)
        }
    }

    private fun processConnectionRequestOutcomes(data: Map<String, Any>) {
        val requestOutcomes = data["requests"] as? List<Map<String, Boolean>> ?: return

        val (approvedRequests, refusedRequests) = requestOutcomes.partition { it.values.firstOrNull() == true }

        handleRefusedRequests(refusedRequests)

        if (approvedRequests.isNotEmpty()) {
            Initializer.initConversations()
        }

        updateRequestOutcomes(requestOutcomes)
    }

    private fun handleRefusedRequests(refusedRequests: List<Map<String, Boolean>>) {
        refusedRequests.forEach { request ->
            val targetId = request.keys.firstOrNull() ?: return@forEach
            val targetName = contentProvider.requestedConnections.value.find { it.targetId == targetId }?.targetName
            targetName?.let { name ->
                getToast(applicationContext, "could not connect with $name")
            }
        }
    }

    private fun updateRequestOutcomes(outcomes: List<Map<String, Boolean>>) {
        getUserUid()?.let { userRequestOutcomeDoc ->
            contentRepository.updateDocumentByField(
                collectionPath = Databases.Collections.CONNECTION_REQUEST_OUTCOME,
                field = "requests",
                data = outcomes,
                documentId = userRequestOutcomeDoc
            )
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        tcConnectionWatcher.stopAllWatchers()
    }
}