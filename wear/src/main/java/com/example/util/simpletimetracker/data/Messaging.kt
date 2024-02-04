/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import com.example.util.simpletimetracker.presentation.LOG_TAG

const val START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME = "start_time_tracking_activity"

class Messaging {
    fun startTimeTracking(
        context: Context,
        activity: String,
        tag: String,
    ) {
        Thread(
            Runnable {
                startTimeTrackingTask(context, activity, tag)
            },
        ).start()
    }

    private fun startTimeTrackingTask(
        context: Context,
        activity: String,
        tag: String,
    ) {
        // Find all nodes which support the time tracking message
        val capabilityInfo: CapabilityInfo =
            Tasks.await(
                Wearable.getCapabilityClient(context)
                    .getCapability(
                        START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME,
                        CapabilityClient.FILTER_REACHABLE,
                    ),
            )

        // Choose the best node (the closest one connected to the watch
        val nodes = capabilityInfo.nodes
        val bestNode = nodes.firstOrNull { it.isNearby }?.id ?: nodes.firstOrNull()?.id

        // Send the message
        val message = "$activity|$tag"
        bestNode?.also { nodeId ->
            Log.i(LOG_TAG, "Sending message to $bestNode")
            val sendTask: Task<*> =
                Wearable.getMessageClient(context).sendMessage(
                    nodeId,
                    "/$START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME",
                    message.toByteArray(),
                ).apply {
                    addOnSuccessListener {
                        Log.i(
                            LOG_TAG,
                            "Sent $START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME message: $message",
                        )
                    }
                    addOnFailureListener {
                        Log.e(
                            LOG_TAG,
                            "Failed to send $START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME message: $message",
                        )
                    }
                }
        } ?: run {
            Log.e(
                LOG_TAG,
                "No nodes found with the capability $START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME",
            )
        }
    }
}
