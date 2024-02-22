package com.example.util.simpletimetracker.presentation.mediators

import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import com.example.util.simpletimetracker.wearrpc.Settings
import com.example.util.simpletimetracker.wearrpc.Tag
import com.example.util.simpletimetracker.wearrpc.WearRPCClient

class CurrentActivitiesMediator(
    private val rpc: WearRPCClient,
    private val currents: Array<CurrentActivity>,
) {
    suspend fun start(activityId: Long) {
        start(activityId, arrayOf())
    }
    suspend fun start(activityId: Long, tags: Array<Tag>) {
        val newCurrent = CurrentActivity(
            id = activityId,
            startedAt = System.currentTimeMillis(),
            tags = tags
        )
        if (settings().multitasking) {
            this.rpc.setCurrentActivities(currents.plus(newCurrent))
        } else {
            this.rpc.setCurrentActivities(arrayOf(newCurrent))
        }
    }

    suspend fun stop(currentId: Long) {
        val remaining = currents.filter { it.id != currentId }.toTypedArray()
        this.rpc.setCurrentActivities(remaining)
    }
    private suspend fun settings(): Settings {
        return this.rpc.querySettings()
    }

}