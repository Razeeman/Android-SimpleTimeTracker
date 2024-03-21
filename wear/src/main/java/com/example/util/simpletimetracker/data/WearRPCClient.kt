/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearRequests
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearRPCClient @Inject constructor(
    private val messenger: WearMessenger,
) : WearCommunicationAPI {

    private val gson = Gson()

    override suspend fun queryActivities(): List<WearActivity> {
        val response: List<WearActivity>? = messenger
            .send(WearRequests.QUERY_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun queryCurrentActivities(): List<WearCurrentActivity> {
        val response: List<WearCurrentActivity>? = messenger
            .send(WearRequests.QUERY_CURRENT_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun setCurrentActivities(starting: List<WearCurrentActivity>) {
        messenger.send(WearRequests.SET_CURRENT_ACTIVITIES, mapToBytes(starting))
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTag> {
        val response: List<WearTag>? = messenger
            .send(WearRequests.QUERY_TAGS_FOR_ACTIVITY, mapToBytes(activityId))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun querySettings(): WearSettings {
        val response: WearSettings? = messenger
            .send(WearRequests.QUERY_SETTINGS)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException
    }

    override suspend fun setSettings(settings: WearSettings) {
        messenger.send(WearRequests.SET_SETTINGS, mapToBytes(settings))
    }

    override suspend fun openPhoneApp() {
        messenger.send(WearRequests.OPEN_PHONE_APP)
    }

    fun addListener(
        onDataChanged: suspend () -> Unit,
    ) {
        messenger.addListener(onDataChanged)
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