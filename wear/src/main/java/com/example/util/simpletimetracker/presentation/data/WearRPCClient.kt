/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.data

import com.example.util.simpletimetracker.wear_api.Activity
import com.example.util.simpletimetracker.wear_api.CurrentActivity
import com.example.util.simpletimetracker.wear_api.Request
import com.example.util.simpletimetracker.wear_api.Settings
import com.example.util.simpletimetracker.wear_api.Tag
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
            .send(Request.PING, mapToBytes(message))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun queryActivities(): List<Activity> {
        val response: List<Activity>? = messenger
            .send(Request.QUERY_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun queryCurrentActivities(): List<CurrentActivity> {
        val response: List<CurrentActivity>? = messenger
            .send(Request.QUERY_CURRENT_ACTIVITIES)
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun setCurrentActivities(starting: List<CurrentActivity>) {
        messenger.send(Request.SET_CURRENT_ACTIVITIES, mapToBytes(starting))
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<Tag> {
        val response: List<Tag>? = messenger
            .send(Request.QUERY_TAGS_FOR_ACTIVITY, mapToBytes(activityId))
            ?.let(::mapFromBytes)

        return response ?: throw WearRPCException("No response")
    }

    override suspend fun querySettings(): Settings {
        val response: Settings? = messenger
            .send(Request.QUERY_SETTINGS)
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