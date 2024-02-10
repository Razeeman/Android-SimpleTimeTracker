package com.example.util.simpletimetracker.wearrpc

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.CancellationException

class Messenger(private val context: Context) {
    private val TAG: String = Messenger::class.java.name

    suspend fun sendMessage(capability: String): ByteArray? {
        return sendMessage(capability, ByteArray(0))
    }

    suspend fun sendMessage(capability: String, message: ByteArray): ByteArray? {
        val def = CompletableDeferred<ByteArray?>()
        val bestNode = findNearestNode(capability)

        // Send the message
        bestNode?.also { nodeId ->
            Log.d(TAG, "Sending message to ${bestNode?.displayName}")
            Wearable.getMessageClient(context).sendRequest(bestNode.id, capability, message)
                .addOnSuccessListener {
                    Log.d(TAG, "Response received for $capability")
                    def.complete(it)
                }.addOnCanceledListener {
                    val message = "Request $capability to ${bestNode.displayName} was cancelled"
                    Log.d(TAG, message)
                    def.cancel(CancellationException(message))
                }.addOnFailureListener {
                    val message =
                        "No response received from mobile for $capability : ${String(message)}"
                    Log.d(TAG, message)
                    def.cancel(CancellationException(message))
                }
        } ?: run {
            val message = "No nodes found with the capability $capability"
            Log.d(TAG, message)
            def.cancel(CancellationException(message))
        }
        return def.await()
    }

    private suspend fun findNearestNode(capability: String): Node? {
        // Find all nodes which support the time tracking message
        Log.d(TAG, "Searching for nodes with Simple Time Tracker installed")
        val capabilityInfo: CapabilityInfo = Tasks.await(
            Wearable.getCapabilityClient(context)
                .getCapability(capability, CapabilityClient.FILTER_REACHABLE),
        )

        // Choose the best node (the closest one connected to the watch)
        val nodes = capabilityInfo.nodes
        Log.d(TAG, nodes.toString())
        return nodes.firstOrNull { it.isNearby } ?: nodes.firstOrNull()
    }
}