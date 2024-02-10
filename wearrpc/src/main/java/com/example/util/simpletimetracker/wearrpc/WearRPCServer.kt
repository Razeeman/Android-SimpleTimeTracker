/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import com.google.gson.Gson


class WearRPCServer(private val api: SimpleTimeTrackerAPI) {

    suspend fun onRequest(path: String, request: ByteArray): ByteArray? {
        if (path.startsWith("/stt")) {
            when (path) {
                Request.PING -> return onPing(request)
                Request.GET_ACTIVITIES -> return onGetActivities()
                Request.GET_CURRENT_ACTIVITIES -> return onGetCurrentActivities()
                Request.START_ACTIVITIES -> return onStartActivities(request)
                Request.GET_TAGS_FOR_ACTIVITY -> return onGetTagsForActivity(request)
                else -> throw WearRPCException("$path is an invalid RPC call")
            }
        } else return null
    }

    private suspend fun onGetTagsForActivity(request: ByteArray): ByteArray? {
        TODO("Not yet implemented")
    }

    private suspend fun onStartActivities(request: ByteArray): ByteArray? {
        TODO("Not yet implemented")
    }

    private suspend fun onPing(request: ByteArray): ByteArray? {
        return request
    }

    private suspend fun onGetActivities(): ByteArray? {
        return Gson().toJson(api.queryActivities()).toByteArray()
    }

    private suspend fun onGetCurrentActivities(): ByteArray? {
        return Gson().toJson(api.queryCurrentActivities()).toByteArray()
    }
}