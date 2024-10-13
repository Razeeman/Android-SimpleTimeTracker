/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.mediator

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.data.WearRPCException
import com.example.util.simpletimetracker.domain.model.WearRecordRepeatResult
import javax.inject.Inject

class StartActivityMediator @Inject constructor(
    private val wearDataRepo: WearDataRepo,
) {

    suspend fun requestStart(
        activityId: Long,
        onRequestTagSelection: suspend () -> Unit,
        onProgressChanged: (isLoading: Boolean) -> Unit,
    ): Result<Unit> {
        onProgressChanged(true)

        val shouldShowTagSelection = wearDataRepo.loadShouldShowTagSelection(activityId)
            .getOrNull() ?: return Result.failure(WearRPCException)

        return if (shouldShowTagSelection) {
            onProgressChanged(false)
            onRequestTagSelection()
            Result.success(Unit)
        } else {
            start(activityId, emptyList())
        }
    }

    suspend fun start(
        activityId: Long,
        tagIds: List<Long>,
    ): Result<Unit> {
        return wearDataRepo.startActivity(activityId, tagIds)
    }

    suspend fun stop(currentId: Long): Result<Unit> {
        return wearDataRepo.stopActivity(currentId)
    }

    suspend fun repeat(): Result<WearRecordRepeatResult> {
        return wearDataRepo.repeatActivity()
    }
}