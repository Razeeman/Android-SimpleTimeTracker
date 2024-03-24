/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.mediator.CurrentActivitiesMediator
import com.example.util.simpletimetracker.domain.mediator.StartActivityMediator
import com.example.util.simpletimetracker.wear_api.WearActivity
import com.example.util.simpletimetracker.wear_api.WearSettings
import com.example.util.simpletimetracker.wear_api.WearTag
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class StartActivityMediatorTest {
    private val wearDataRepo: WearDataRepo = Mockito.mock()
    private val currentActivitiesMediator: CurrentActivitiesMediator = Mockito.mock()
    private val mediator = StartActivityMediator(
        wearDataRepo = wearDataRepo,
        currentActivitiesMediator = currentActivitiesMediator,
    )

    private val sampleActivity = WearActivity(
        id = 1,
        name = "Sleep",
        icon = "🛏️",
        color = 0xFF123456,
    )
    private val sampleGeneralTag = WearTag(
        id = 13,
        name = "Sleep",
        isGeneral = true,
        color = 0xFF654321,
    )
    private val sampleNonGeneralTag = WearTag(
        id = 14,
        name = "Work",
        isGeneral = false,
        color = 0xFF654321,
    )
    private val settings = WearSettings(
        allowMultitasking = false,
        showRecordTagSelection = false,
        recordTagSelectionCloseAfterOne = false,
        recordTagSelectionExcludedActivities = emptyList(),
    )

    @Before
    fun setup() {
        Mockito.reset(wearDataRepo, currentActivitiesMediator)
    }

    @Test
    fun `tag selection disabled`() = runTest {
        // Given
        Mockito.`when`(wearDataRepo.loadSettings()).thenReturn(
            settings.copy(
                showRecordTagSelection = false,
            ).let(Result.Companion::success),
        )
        var onRequestTagSelectionCalled = false

        // When
        mediator.requestStart(
            activityId = sampleActivity.id,
            onRequestTagSelection = { onRequestTagSelectionCalled = true },
        )

        // Then
        Mockito.verify(currentActivitiesMediator).start(sampleActivity.id)
        assertEquals(false, onRequestTagSelectionCalled)
    }

    @Test
    fun `tag selection enabled and activity has no tags`() = runTest {
        // Given
        Mockito.`when`(wearDataRepo.loadSettings()).thenReturn(
            settings.copy(
                showRecordTagSelection = true,
                recordTagSelectionExcludedActivities = emptyList(),
            ).let(Result.Companion::success),
        )
        Mockito.`when`(wearDataRepo.loadTagsForActivity(Mockito.anyLong())).thenReturn(
            Result.success(emptyList()),
        )
        var onRequestTagSelectionCalled = false

        // When
        mediator.requestStart(
            activityId = sampleActivity.id,
            onRequestTagSelection = { onRequestTagSelectionCalled = true },
        )

        // Then
        Mockito.verify(currentActivitiesMediator).start(sampleActivity.id)
        assertEquals(false, onRequestTagSelectionCalled)
    }

    @Test
    fun `tag selection enabled, but not for this activity`() = runTest {
        // Given
        Mockito.`when`(wearDataRepo.loadSettings()).thenReturn(
            Result.success(
                settings.copy(
                    showRecordTagSelection = true,
                    recordTagSelectionExcludedActivities = listOf(sampleActivity.id),
                ),
            ),
        )
        Mockito.`when`(wearDataRepo.loadTagsForActivity(Mockito.anyLong())).thenReturn(
            Result.success(listOf(sampleGeneralTag)),
        )
        var onRequestTagSelectionCalled = false

        // When
        mediator.requestStart(
            activityId = sampleActivity.id,
            onRequestTagSelection = { onRequestTagSelectionCalled = true },
        )

        // Then
        Mockito.verify(currentActivitiesMediator).start(sampleActivity.id)
        assertEquals(false, onRequestTagSelectionCalled)
    }

    @Test
    fun `tag selection enabled, activity has tags`() = runTest {
        // Given
        Mockito.`when`(wearDataRepo.loadSettings()).thenReturn(
            Result.success(
                settings.copy(
                    showRecordTagSelection = true,
                    recordTagSelectionExcludedActivities = emptyList(),
                ),
            ),
        )
        Mockito.`when`(wearDataRepo.loadTagsForActivity(Mockito.anyLong())).thenReturn(
            Result.success(listOf(sampleNonGeneralTag)),
        )
        var onRequestTagSelectionCalled = false

        // When
        mediator.requestStart(
            activityId = sampleActivity.id,
            onRequestTagSelection = { onRequestTagSelectionCalled = true },
        )

        // Then
        Mockito.verify(currentActivitiesMediator, Mockito.never()).start(sampleActivity.id)
        assertEquals(true, onRequestTagSelectionCalled)
    }
}