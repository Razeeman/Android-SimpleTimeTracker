/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import com.example.util.simpletimetracker.wearrpc.Request
import com.example.util.simpletimetracker.wearrpc.WearCommunicationAPI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import javax.inject.Inject

class WearRPCServer @Inject constructor(
    private val api: WearCommunicationAPI,
) {

    private val gson = Gson()

    suspend fun onRequest(path: String, request: ByteArray): ByteArray? {
        return if (path.startsWith(Request.PATH)) {
            when (path) {
                Request.PING -> onPing(request)
                Request.QUERY_ACTIVITIES -> onQueryActivities()
                Request.QUERY_CURRENT_ACTIVITIES -> onQueryCurrentActivities()
                Request.SET_CURRENT_ACTIVITIES -> onSetCurrentActivities(request)
                Request.QUERY_TAGS_FOR_ACTIVITY -> onQueryTagsForActivity(request)
                Request.QUERY_SETTINGS -> onQuerySettings()
                else -> {
                    Timber.d("$path is an invalid RPC call")
                    null
                }
            }
        } else {
            null
        }
    }

    private suspend fun onQueryTagsForActivity(request: ByteArray): ByteArray? {
        val activityId: Long = mapRequest(request) ?: return null
        return mapToResponse(api.queryTagsForActivity(activityId))
    }

    private suspend fun onSetCurrentActivities(request: ByteArray): ByteArray? {
        val activities: Array<CurrentActivity> = mapRequest(request) ?: return null
        api.setCurrentActivities(activities)
        return ByteArray(0)
    }

    private fun onPing(request: ByteArray): ByteArray {
        return request
    }

    private suspend fun onQueryActivities(): ByteArray {
        return mapToResponse(api.queryActivities())
    }

    private suspend fun onQueryCurrentActivities(): ByteArray {
        return mapToResponse(api.queryCurrentActivities())
    }

    private suspend fun onQuerySettings(): ByteArray {
        return mapToResponse(api.querySettings())
    }

    private fun <T> mapToResponse(data: T): ByteArray {
        return gson.toJson(data).toByteArray()
    }

    private inline fun <reified T> mapRequest(data: ByteArray): T? {
        return runCatching {
            val collectionType = object : TypeToken<T>() {}.type
            gson.fromJson<T>(String(data), collectionType)
        }.getOrNull()
    }
}