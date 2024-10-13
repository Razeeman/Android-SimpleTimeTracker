/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import android.content.Context
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearRequests
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
import com.example.util.simpletimetracker.wear_api.WearStopActivityRequest
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

// Application side.
class WearRPCServer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: WearCommunicationAPI,
) {

    private val gson = Gson()

    suspend fun onRequest(path: String, request: ByteArray): ByteArray? {
        return if (path.startsWith(WearRequests.PATH)) {
            when (path) {
                WearRequests.QUERY_ACTIVITIES -> onQueryActivities()
                WearRequests.QUERY_CURRENT_ACTIVITIES -> onQueryCurrentActivities()
                WearRequests.START_ACTIVITY -> onStartActivity(request)
                WearRequests.STOP_ACTIVITY -> onStopActivity(request)
                WearRequests.REPEAT_ACTIVITY -> onRepeatActivity()
                WearRequests.QUERY_TAGS_FOR_ACTIVITY -> onQueryTagsForActivity(request)
                WearRequests.QUERY_SHOULD_SHOW_TAG_SELECTION -> onQueryShouldShowTagSelection(request)
                WearRequests.QUERY_SETTINGS -> onQuerySettings()
                WearRequests.SET_SETTINGS -> onSetSettings(request)
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
        return mapToBytes(repo.queryTagsForActivity(activityId))
    }

    private suspend fun onQueryShouldShowTagSelection(request: ByteArray): ByteArray? {
        val data: WearShouldShowTagSelectionRequest = mapFromBytes(request) ?: return null
        return mapToBytes(repo.queryShouldShowTagSelection(data))
    }

    private suspend fun onStartActivity(request: ByteArray): ByteArray? {
        val data: WearStartActivityRequest = mapFromBytes(request) ?: return null
        repo.startActivity(data)
        return ByteArray(0)
    }

    private suspend fun onStopActivity(request: ByteArray): ByteArray? {
        val data: WearStopActivityRequest = mapFromBytes(request) ?: return null
        repo.stopActivity(data)
        return ByteArray(0)
    }

    private suspend fun onRepeatActivity(): ByteArray {
        return mapToBytes(repo.repeatActivity())
    }

    private suspend fun onQueryActivities(): ByteArray {
        return mapToBytes(repo.queryActivities())
    }

    private suspend fun onQueryCurrentActivities(): ByteArray {
        return mapToBytes(repo.queryCurrentActivities())
    }

    private suspend fun onQuerySettings(): ByteArray {
        return mapToBytes(repo.querySettings())
    }

    private suspend fun onSetSettings(request: ByteArray): ByteArray? {
        val settings: WearSettingsDTO = mapFromBytes(request) ?: return null
        repo.setSettings(settings)
        return ByteArray(0)
    }

    private suspend fun onOpenPhoneApp(): ByteArray? {
        repo.openPhoneApp()
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