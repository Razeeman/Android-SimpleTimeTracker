package com.example.util.simpletimetracker.domain

import com.example.util.simpletimetracker.data.WearRPCClient
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import javax.inject.Inject

class CurrentActivitiesMediator @Inject constructor(
    private val rpc: WearRPCClient,
) {

    suspend fun start(
        activityId: Long,
        tags: List<WearTag> = emptyList(),
    ) {
        val newCurrent = WearCurrentActivity(
            id = activityId,
            startedAt = System.currentTimeMillis(),
            tags = tags,
        )
        if (settings().allowMultitasking) {
            val currents = rpc.queryCurrentActivities()
            this.rpc.setCurrentActivities(currents.plus(newCurrent))
        } else {
            this.rpc.setCurrentActivities(listOf(newCurrent))
        }
    }

    suspend fun stop(currentId: Long) {
        val currents = rpc.queryCurrentActivities()
        val remaining = currents.filter { it.id != currentId }
        this.rpc.setCurrentActivities(remaining)
    }

    private suspend fun settings(): WearSettings {
        return this.rpc.querySettings()
    }
}