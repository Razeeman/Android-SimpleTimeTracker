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
        val response = messenger.sendMessage(Request.GET_ACTIVITIES)
        if (response != null) {
            Log.i(TAG, String(response))
            val collectionType = object : TypeToken<Array<Activity>>() {}.type
            return Gson().fromJson(String(response), collectionType)
        } else throw WearRPCException("No response")
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        TODO("Not yet implemented")
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        TODO("Not yet implemented")
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        TODO("Not yet implemented")
    }

    override suspend fun querySettings(): Settings {
        TODO("Not yet implemented")
    }
}