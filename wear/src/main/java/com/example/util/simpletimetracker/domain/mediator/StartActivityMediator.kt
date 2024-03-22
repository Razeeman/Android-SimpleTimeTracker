/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.mediator

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.data.WearRPCException
import com.example.util.simpletimetracker.wear_api.WearSettings
import javax.inject.Inject

class StartActivityMediator @Inject constructor(
    private val wearDataRepo: WearDataRepo,
    private val currentActivitiesMediator: CurrentActivitiesMediator,
) {

    suspend fun requestStart(
        activityId: Long,
        onRequestTagSelection: suspend () -> Unit,
    ): Result<Unit> {
        val settings = wearDataRepo.loadSettings()
            .getOrNull() ?: return Result.failure(WearRPCException)
        val shouldShowTagSelection = shouldShowTagSelection(
            typeId = activityId,
            settings = settings,
        ).getOrNull() ?: return Result.failure(WearRPCException)

        return if (shouldShowTagSelection) {
            onRequestTagSelection()
            Result.success(Unit)
        } else {
            onRequestStartActivity(activityId)
        }
    }

    private suspend fun onRequestStartActivity(
        activityId: Long,
    ): Result<Unit> {
        return currentActivitiesMediator.start(activityId)
    }

    private suspend fun shouldShowTagSelection(
        typeId: Long,
        settings: WearSettings,
    ): Result<Boolean> {
        // Check if need to show tag selection
        return if (settings.showRecordTagSelection) {
            val excludedActivities = settings.recordTagSelectionExcludedActivities

            // Check if activity is excluded from tag dialog.
            if (typeId !in excludedActivities) {
                // Check if activity has tags.
                val tags = wearDataRepo.loadTagsForActivity(typeId)
                    .getOrNull() ?: return Result.failure(WearRPCException)
                Result.success(tags.isNotEmpty())
            } else {
                Result.success(false)
            }
        } else {
            Result.success(false)
        }
    }
}