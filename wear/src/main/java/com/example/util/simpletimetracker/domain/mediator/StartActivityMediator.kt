/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.mediator

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.data.WearRPCException
import com.example.util.simpletimetracker.domain.interactor.WearTagSelectionDataInteractor
import com.example.util.simpletimetracker.domain.model.WearRecordRepeatResult
import com.example.util.simpletimetracker.domain.model.WearRecordTag
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartActivityMediator @Inject constructor(
    private val wearDataRepo: WearDataRepo,
    private val wearTagSelectionDataInteractor: WearTagSelectionDataInteractor,
) {

    private val activityActionMutex: Mutex = Mutex()

    suspend fun requestStart(
        activityId: Long,
        onRequestTagSelection: suspend () -> Unit,
        onProgressChanged: (isLoading: Boolean) -> Unit,
    ): Result<Unit> = activityActionMutex.withLock {
        onProgressChanged(true)

        val shouldShowTagSelection = wearDataRepo.loadShouldShowTagSelection(activityId)
            .getOrNull() ?: return@withLock Result.failure(WearRPCException)

        if (shouldShowTagSelection.shouldShow) {
            onProgressChanged(false)
            wearTagSelectionDataInteractor.data[activityId] = shouldShowTagSelection
            onRequestTagSelection()
            Result.success(Unit)
        } else {
            startUnlocked(
                activityId = activityId,
                tags = emptyList(),
                useSelectedTags = false,
            )
        }
    }

    suspend fun start(
        activityId: Long,
        tags: List<WearRecordTag>,
        useSelectedTags: Boolean,
    ): Result<Unit> = activityActionMutex.withLock {
        startUnlocked(activityId, tags, useSelectedTags)
    }

    suspend fun stop(currentId: Long): Result<Unit> = activityActionMutex.withLock {
        wearDataRepo.stopActivity(currentId)
    }

    suspend fun repeat(): Result<WearRecordRepeatResult> = activityActionMutex.withLock {
        wearDataRepo.repeatActivity()
    }

    private suspend fun startUnlocked(
        activityId: Long,
        tags: List<WearRecordTag>,
        useSelectedTags: Boolean,
    ): Result<Unit> {
        return wearDataRepo.startActivity(
            id = activityId,
            tags = tags,
            useSelectedTags = useSelectedTags,
        )
    }
}
