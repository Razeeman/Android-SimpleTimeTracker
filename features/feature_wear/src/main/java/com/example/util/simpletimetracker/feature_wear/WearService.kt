/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Landing point for messages coming to Mobile from Wear.
 *
 * Implemented as a Service so that it can receive messages even when the Mobile app is stopped or
 * in the background.
 */
@AndroidEntryPoint
class WearService : WearableListenerService() {

    @Inject
    lateinit var wearRPCServer: WearRPCServer

    override fun onRequest(
        nodeId: String,
        path: String,
        request: ByteArray,
    ): Task<ByteArray>? {
        // Can block because service is on separate thread.
        return runBlocking {
            Tasks.forResult(wearRPCServer.onRequest(path, request))
        }
    }
}
