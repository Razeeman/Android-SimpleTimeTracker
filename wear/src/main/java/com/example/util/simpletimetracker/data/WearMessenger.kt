/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import android.content.Context
import android.util.Log
import com.example.util.simpletimetracker.wear_api.WearRequests
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearMessenger @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val tag: String = WearMessenger::class.java.name
    private var listener: (suspend () -> Unit)? = null

    suspend fun send(
        capability: String,
        message: ByteArray = ByteArray(0),
    ): ByteArray? = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<ByteArray?>()
        val bestNode = findNearestNode(capability)

        // Send the message
        if (bestNode != null) {
            Log.d(tag, "Sending message to ${bestNode.displayName}")
            Log.d(tag, String(message))
            Wearable.getMessageClient(context)
                .sendRequest(bestNode.id, capability, message)
                .addOnSuccessListener {
                    Log.d(tag, "Response received for $capability")
                    Log.d(tag, String(it))
                    deferred.complete(it)
                }.addOnCanceledListener {
                    val logMessage = "Request $capability to ${bestNode.displayName} was cancelled"
                    Log.d(tag, logMessage)
                    deferred.cancel(CancellationException(logMessage))
                }.addOnFailureListener {
                    val logMessage = "No response received from mobile for $capability : ${String(message)}"
                    Log.d(tag, logMessage)
                    deferred.cancel(CancellationException(logMessage))
                }
        } else {
            val logMessage = "No nodes found with the capability $capability"
            Log.d(tag, logMessage)
            deferred.cancel(CancellationException(logMessage))
        }

        deferred.await()
    }

    fun addListener(
        onDataChanged: suspend () -> Unit,
    ) {
        listener = onDataChanged
    }

    suspend fun onRequest(path: String): ByteArray? {
        if (path.startsWith(WearRequests.PATH)) {
            when (path) {
                WearRequests.DATA_UPDATED -> {
                    listener?.invoke()
                }
                else -> {
                    Log.d(tag, "$path is an invalid RPC call")
                }
            }
        }
        return null
    }

    private fun findNearestNode(capability: String): Node? {
        // Find all nodes which support the time tracking message
        Log.d(tag, "Searching for nodes with ${context.packageName} installed")
        val nodeClient = Wearable.getNodeClient(context)
        val connectedNodes = Tasks.await(nodeClient.connectedNodes)
        connectedNodes.forEach {
            Log.d(tag, "Connected to ${it.displayName} (id: ${it.id}) (nearby: ${it.isNearby})")
        }

        val capabilityInfo: CapabilityInfo = Tasks.await(
            Wearable.getCapabilityClient(context)
                .getCapability(capability, CapabilityClient.FILTER_REACHABLE),
        )

        // Choose the best node (the closest one connected to the watch)
        val nodes = capabilityInfo.nodes
        Log.d(tag, nodes.toString())
        return nodes.firstOrNull { it.isNearby } ?: nodes.firstOrNull()
    }
}