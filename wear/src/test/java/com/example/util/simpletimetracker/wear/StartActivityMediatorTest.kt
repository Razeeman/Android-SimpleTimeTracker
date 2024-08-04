/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear

import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.domain.mediator.StartActivityMediator
import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearTag
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class StartActivityMediatorTest {
    private val wearDataRepo: WearDataRepo = Mockito.mock()
    private val mediator = StartActivityMediator(
        wearDataRepo = wearDataRepo,
    )

    private val sampleActivity = WearActivity(
        id = 1,
        name = "Sleep",
        icon = "🛏️",
        color = 0xFF123456,
    )
    private val sampleTag = WearTag(
        id = 13,
        name = "Sleep",
        color = 0xFF654321,
    )
    private val settings = WearSettings(
        allowMultitasking = false,
        recordTagSelectionCloseAfterOne = false,
    )

    @Before
    fun setup() {
        Mockito.reset(wearDataRepo)
    }

    @Test
    fun `tag selection disabled`() = runTest {
        // Given
        Mockito.`when`(wearDataRepo.loadShouldShowTagSelection(sampleActivity.id))
            .thenReturn(Result.success(false))
        var onRequestTagSelectionCalled = false
        var onProgressChanged = false

        // When
        mediator.requestStart(
            activityId = sampleActivity.id,
            onRequestTagSelection = { onRequestTagSelectionCalled = true },
            onProgressChanged = { onProgressChanged = it },
        )

        // Then
        Mockito.verify(wearDataRepo).startActivity(sampleActivity.id, emptyList())
        assertEquals(false, onRequestTagSelectionCalled)
        assertEquals(true, onProgressChanged)
    }

    @Test
    fun `tag selection enabled`() = runTest {
        // Given
        Mockito.`when`(wearDataRepo.loadShouldShowTagSelection(sampleActivity.id))
            .thenReturn(Result.success(true))
        var onRequestTagSelectionCalled = false
        var onProgressChanged = false

        // When
        mediator.requestStart(
            activityId = sampleActivity.id,
            onRequestTagSelection = { onRequestTagSelectionCalled = true },
            onProgressChanged = { onProgressChanged = it },
        )

        // Then
        Mockito.verify(wearDataRepo, Mockito.never()).startActivity(sampleActivity.id, emptyList())
        assertEquals(true, onRequestTagSelectionCalled)
        assertEquals(false, onProgressChanged)
    }
}