/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class WearRPCServer(private val api: SimpleTimeTrackerAPI) {

    suspend fun onRequest(path: String, request: ByteArray): ByteArray? {
        if (path.startsWith("/stt")) {
            when (path) {
                Request.PING -> return onPing(request)
                Request.QUERY_ACTIVITIES -> return onQueryActivities()
                Request.QUERY_CURRENT_ACTIVITIES -> return onQueryCurrentActivities()
                Request.SET_CURRENT_ACTIVITIES -> return onSetCurrentActivities(request)
                Request.QUERY_TAGS_FOR_ACTIVITY -> return onQueryTagsForActivity(request)
                Request.QUERY_SETTINGS -> return onQuerySettings()
                else -> throw WearRPCException("$path is an invalid RPC call")
            }
        } else return null
    }

    private suspend fun onQueryTagsForActivity(request: ByteArray): ByteArray? {
        val activityId = Gson().fromJson(String(request), Long::class.java)
        return Gson().toJson(api.queryTagsForActivity(activityId)).toByteArray()
    }

    private suspend fun onSetCurrentActivities(request: ByteArray): ByteArray? {
        val collectionType = object : TypeToken<Array<CurrentActivity>>() {}.type
        val activities: Array<CurrentActivity> = Gson().fromJson(String(request), collectionType)
        api.setCurrentActivities(activities)
        return ByteArray(0)
    }

    private suspend fun onPing(request: ByteArray): ByteArray? {
        return request
    }

    private suspend fun onQueryActivities(): ByteArray? {
        return Gson().toJson(api.queryActivities()).toByteArray()
    }

    private suspend fun onQueryCurrentActivities(): ByteArray? {
        return Gson().toJson(api.queryCurrentActivities()).toByteArray()
    }

    private suspend fun onQuerySettings(): ByteArray? {
        return Gson().toJson(api.querySettings()).toByteArray()
    }
}