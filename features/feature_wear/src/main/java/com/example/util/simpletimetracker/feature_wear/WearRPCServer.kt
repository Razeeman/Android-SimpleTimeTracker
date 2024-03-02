/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearRequests
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import javax.inject.Inject

class WearRPCServer @Inject constructor(
    private val api: WearCommunicationAPI,
) {

    private val gson = Gson()

    suspend fun onRequest(path: String, request: ByteArray): ByteArray? {
        return if (path.startsWith(WearRequests.PATH)) {
            when (path) {
                WearRequests.PING -> onPing(request)
                WearRequests.QUERY_ACTIVITIES -> onQueryActivities()
                WearRequests.QUERY_CURRENT_ACTIVITIES -> onQueryCurrentActivities()
                WearRequests.SET_CURRENT_ACTIVITIES -> onSetCurrentActivities(request)
                WearRequests.QUERY_TAGS_FOR_ACTIVITY -> onQueryTagsForActivity(request)
                WearRequests.QUERY_SETTINGS -> onQuerySettings()
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
        val activityId: Long = mapFromBytes(request) ?: return null
        return mapToBytes(api.queryTagsForActivity(activityId))
    }

    private suspend fun onSetCurrentActivities(request: ByteArray): ByteArray? {
        val activities: List<WearCurrentActivity> = mapFromBytes(request) ?: return null
        api.setCurrentActivities(activities)
        return ByteArray(0)
    }

    private fun onPing(request: ByteArray): ByteArray {
        return request
    }

    private suspend fun onQueryActivities(): ByteArray {
        return mapToBytes(api.queryActivities())
    }

    private suspend fun onQueryCurrentActivities(): ByteArray {
        return mapToBytes(api.queryCurrentActivities())
    }

    private suspend fun onQuerySettings(): ByteArray {
        return mapToBytes(api.querySettings())
    }

    private fun <T> mapToBytes(data: T): ByteArray {
        return gson.toJson(data).toByteArray()
    }

    private inline fun <reified T> mapFromBytes(data: ByteArray): T? {
        return runCatching {
            val collectionType = object : TypeToken<T>() {}.type
            gson.fromJson<T>(String(data), collectionType)
        }.getOrNull()
    }
}