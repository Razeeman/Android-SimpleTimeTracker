package com.example.util.simpletimetracker.presentation.mediators

import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import com.example.util.simpletimetracker.presentation.data.WearRPCClient

class CurrentActivitiesMediator(
    private val rpc: WearRPCClient,
    private val currents: List<WearCurrentActivity>,
) {
    suspend fun start(activityId: Long) {
        start(activityId, emptyList())
    }
    suspend fun start(activityId: Long, tags: List<WearTag>) {
        val newCurrent = WearCurrentActivity(
            id = activityId,
            startedAt = System.currentTimeMillis(),
            tags = tags
        )
        if (settings().allowMultitasking) {
            this.rpc.setCurrentActivities(currents.plus(newCurrent))
        } else {
            this.rpc.setCurrentActivities(listOf(newCurrent))
        }
    }

    suspend fun stop(currentId: Long) {
        val remaining = currents.filter { it.id != currentId }
        this.rpc.setCurrentActivities(remaining)
    }
    private suspend fun settings(): WearSettings {
        return this.rpc.querySettings()
    }

}