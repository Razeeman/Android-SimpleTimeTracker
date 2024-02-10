/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class WearRPCClient(private val messenger: Messenger): SimpleTimeTrackerAPI {
    private val TAG: String = WearRPCClient::class.java.name

    override suspend fun ping(message: String): String {
        val response = messenger.sendMessage(Request.PING, message.toByteArray())
        if (response != null) return String(response)
        else throw WearRPCException("No response")
    }

    override suspend fun queryActivities(): Array<Activity> {
        val response = messenger.sendMessage(Request.QUERY_ACTIVITIES)
        if (response != null) {
            Log.i(TAG, String(response))
            val collectionType = object : TypeToken<Array<Activity>>() {}.type
            return Gson().fromJson(String(response), collectionType)
        } else throw WearRPCException("No response")
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        TODO("Not yet implemented")
        // 1. Send a Request.QUERY_CURRENT_ACTIVITIES message
        // 2. Parse the resulting JSON bytes and return it
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        TODO("Not yet implemented")
        // 1. Serialize the given activities to JSON
        // 2. Send a Request.SET_CURRENT_ACTIVITIES message
        // 3. Wait for response (successful/error)
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        TODO("Not yet implemented")
        // 1. Serialize the given activityID to bytes
        // 2. Send a Request.QUERY_TAGS_FOR_ACTIVITY message
        // 3. Deserialize the JSON response and return.
    }

    override suspend fun querySettings(): Settings {
        TODO("Not yet implemented")
        // 1. Send a Request.QUERY_SETTINGS message
        // 2. Deserialize the JSON response and return
    }
}