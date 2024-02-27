/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class WearRPCClient(private val messenger: Messenger) : SimpleTimeTrackerAPI {

    override suspend fun ping(message: String): String {
        val response = messenger.send(Request.PING, message.toByteArray())
        if (response != null) return String(response)
        else throw WearRPCException("No response")
    }

    override suspend fun queryActivities(): Array<Activity> {
        val response = messenger.send(Request.QUERY_ACTIVITIES)
        if (response != null) {
            val collectionType = object : TypeToken<Array<Activity>>() {}.type
            return Gson().fromJson(String(response), collectionType)
        } else throw WearRPCException("No response")
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        val response = messenger.send(Request.QUERY_CURRENT_ACTIVITIES)
        if (response != null) {
            val collectionType = object : TypeToken<Array<CurrentActivity>>() {}.type
            return Gson().fromJson(String(response), collectionType)
        } else throw WearRPCException("No response")
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        messenger.send(Request.SET_CURRENT_ACTIVITIES, Gson().toJson(activities).toByteArray())
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        val response = messenger.send(
            Request.QUERY_TAGS_FOR_ACTIVITY,
            Gson().toJson(activityId).toByteArray(),
        )
        if (response != null) {
            val collectionType = object : TypeToken<Array<Tag>>() {}.type
            return Gson().fromJson(String(response), collectionType)
        } else throw WearRPCException("No response")
    }

    override suspend fun querySettings(): Settings {
        val response = messenger.send(Request.QUERY_SETTINGS)
        if (response != null) {
            val jsonType = object : TypeToken<Settings>() {}.type
            return Gson().fromJson(String(response), jsonType)
        } else throw WearRPCException("No response")
    }
}