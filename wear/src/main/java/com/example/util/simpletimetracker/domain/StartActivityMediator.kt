/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.data.WearRPCException
import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import javax.inject.Inject

class StartActivityMediator @Inject constructor(
    private val wearDataRepo: WearDataRepo,
    private val currentActivitiesMediator: CurrentActivitiesMediator,
) {

    suspend fun requestStart(
        activity: WearActivity,
        onRequestTagSelection: suspend () -> Unit,
    ): Result<Unit> {
        val settings = wearDataRepo.loadSettings()
            .getOrNull() ?: return Result.failure(WearRPCException)

        return if (settings.showRecordTagSelection) {
            requestTagSelectionIfNeeded(
                activity = activity,
                settings = settings,
                onRequestTagSelection = onRequestTagSelection,
            )
        } else {
            onRequestStartActivity(activity.id)
        }
    }

    private suspend fun requestTagSelectionIfNeeded(
        activity: WearActivity,
        settings: WearSettings,
        onRequestTagSelection: suspend () -> Unit,
    ): Result<Unit> {
        val tags = wearDataRepo.loadTagsForActivity(activity.id)
            .getOrNull() ?: return Result.failure(WearRPCException)

        val generalTags = tags.filter { it.isGeneral }
        val nonGeneralTags = tags.filter { !it.isGeneral }
        val tagSelectionNeeded = nonGeneralTags.isNotEmpty() ||
            generalTags.isNotEmpty() && settings.recordTagSelectionEvenForGeneralTags

        return if (tagSelectionNeeded) {
            onRequestTagSelection()
            Result.success(Unit)
        } else {
            onRequestStartActivity(activity.id)
        }
    }

    private suspend fun onRequestStartActivity(
        activityId: Long,
    ): Result<Unit> {
        return currentActivitiesMediator.start(activityId)
    }
}