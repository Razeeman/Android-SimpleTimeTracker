/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import android.content.Context
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearRequests
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class WearRPCServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: WearCommunicationAPI,
) {

    private val gson = Gson()

    suspend fun onRequest(path: String, request: ByteArray): ByteArray? {
        return if (path.startsWith(WearRequests.PATH)) {
            when (path) {
                WearRequests.QUERY_ACTIVITIES -> onQueryActivities()
                WearRequests.QUERY_CURRENT_ACTIVITIES -> onQueryCurrentActivities()
                WearRequests.SET_CURRENT_ACTIVITIES -> onSetCurrentActivities(request)
                WearRequests.QUERY_TAGS_FOR_ACTIVITY -> onQueryTagsForActivity(request)
                WearRequests.QUERY_SETTINGS -> onQuerySettings()
                WearRequests.OPEN_PHONE_APP -> onOpenPhoneApp()
                else -> {
                    Timber.d("$path is an invalid RPC call")
                    null
                }
            }
        } else {
            null
        }
    }

    suspend fun updateData() = withContext(Dispatchers.IO) {
        runCatching {
            Timber.d("Searching for nodes with ${context.packageName} installed")
            val nodesList = Wearable.getNodeClient(context)
                .connectedNodes
                .let { Tasks.await(it) }

            nodesList.forEach {
                Timber.d("Connected to ${it.displayName} (id: ${it.id}) (nearby: ${it.isNearby})")
            }

            nodesList.forEach { node ->
                Timber.d("Sending message to ${node.displayName}")
                Wearable.getMessageClient(context).sendRequest(
                    node.id,
                    WearRequests.DATA_UPDATED,
                    null,
                )
            }
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

    private suspend fun onQueryActivities(): ByteArray {
        return mapToBytes(api.queryActivities())
    }

    private suspend fun onQueryCurrentActivities(): ByteArray {
        return mapToBytes(api.queryCurrentActivities())
    }

    private suspend fun onQuerySettings(): ByteArray {
        return mapToBytes(api.querySettings())
    }

    private suspend fun onOpenPhoneApp(): ByteArray? {
        api.openPhoneApp()
        return null
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