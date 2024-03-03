/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearRequests
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class WearRPCClient @Inject constructor(
    private val messenger: Messenger,
) : WearCommunicationAPI {

    private val gson = Gson()

    override suspend fun ping(message: String): String {
        val response: String? = messenger
            .send(WearRequests.PING, mapToBytes(message))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun queryActivities(): List<WearActivity> {
        val response: List<WearActivity>? = messenger
            .send(WearRequests.QUERY_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun queryCurrentActivities(): List<WearCurrentActivity> {
        val response: List<WearCurrentActivity>? = messenger
            .send(WearRequests.QUERY_CURRENT_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun setCurrentActivities(starting: List<WearCurrentActivity>) {
        messenger.send(WearRequests.SET_CURRENT_ACTIVITIES, mapToBytes(starting))
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTag> {
        val response: List<WearTag>? = messenger
            .send(WearRequests.QUERY_TAGS_FOR_ACTIVITY, mapToBytes(activityId))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun querySettings(): WearSettings {
        val response: WearSettings? = messenger
            .send(WearRequests.QUERY_SETTINGS)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
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