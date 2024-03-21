/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.mediator

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.data.WearRPCException
import com.example.util.simpletimetracker.wear_api.WearCurrentActivity
import com.example.util.simpletimetracker.wear_api.WearTag
import javax.inject.Inject

class CurrentActivitiesMediator @Inject constructor(
    private val wearDataRepo: WearDataRepo,
) {

    suspend fun start(
        activityId: Long,
        tags: List<WearTag> = emptyList(),
    ): Result<Unit> {
        val newCurrent = WearCurrentActivity(
            id = activityId,
            startedAt = System.currentTimeMillis(),
            tags = tags,
        )
        val settings = wearDataRepo.loadSettings()
            .getOrNull() ?: return Result.failure(WearRPCException)

        return if (settings.allowMultitasking) {
            val currents = wearDataRepo.loadCurrentActivities(forceReload = false)
                .getOrNull() ?: return Result.failure(WearRPCException)
            wearDataRepo.setCurrentActivities(currents.plus(newCurrent))
        } else {
            wearDataRepo.setCurrentActivities(listOf(newCurrent))
        }
    }

    suspend fun stop(currentId: Long): Result<Unit> {
        val currents = wearDataRepo.loadCurrentActivities(forceReload = false)
            .getOrNull() ?: return Result.failure(WearRPCException)
        val remaining = currents.filter { it.id != currentId }
        return wearDataRepo.setCurrentActivities(remaining)
    }
}