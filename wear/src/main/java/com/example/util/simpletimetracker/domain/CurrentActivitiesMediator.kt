package com.example.util.simpletimetracker.domain

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearTag
import javax.inject.Inject

class CurrentActivitiesMediator @Inject constructor(
    private val wearDataRepo: WearDataRepo,
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
        val settings = wearDataRepo.loadSettings()

        if (settings.allowMultitasking) {
            val currents = wearDataRepo.loadCurrentActivities()
            this.wearDataRepo.setCurrentActivities(currents.plus(newCurrent))
        } else {
            this.wearDataRepo.setCurrentActivities(listOf(newCurrent))
        }
    }

    suspend fun stop(currentId: Long) {
        val currents = wearDataRepo.loadCurrentActivities()
        val remaining = currents.filter { it.id != currentId }
        this.wearDataRepo.setCurrentActivities(remaining)
    }
}