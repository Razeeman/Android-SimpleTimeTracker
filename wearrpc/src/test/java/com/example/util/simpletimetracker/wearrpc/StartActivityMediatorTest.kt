/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*


open class StartActivityMediatorTestBase {

    protected val api = MockSimpleTimeTrackerAPI()
    protected val startCallback = MockMediatorCallback()
    protected val requestTagCallback = MockMediatorCallback()
    protected val mediator = StartActivityMediator(
        api = api,
        onRequestStartActivity = startCallback,
        onRequestTagSelection = requestTagCallback,
    )

    protected val sampleActivity = Activity(id = 1, name = "Sleep", icon = "🛏️", color = "#123456")
    protected val sampleGeneralTag = Tag(id = 13, name = "Sleep", isGeneral = true)
    protected val sampleNonGeneralTag = Tag(id = 14, name = "Work", isGeneral = false)
    protected val settings = Settings(
        allowMultitasking = false,
        showRecordTagSelection = false,
        recordTagSelectionCloseAfterOne = false,
        recordTagSelectionEvenForGeneralTags = false,
    )

    @Before
    fun setup() {
        api.mockReset()
        startCallback.reset()
        requestTagCallback.reset()
    }
}

class `Starts Activity When` : StartActivityMediatorTestBase() {
    @Test
    fun `tag selection disabled`() = runTest {
        api.mock_querySettings(settings.copy(showRecordTagSelection = false))
        mediator.requestStart(sampleActivity)
        `assert only start callback invoked`()
    }

    @Test
    fun `tag selection enabled and activity has no tags`() = runTest {
        api.mock_querySettings(settings.copy(showRecordTagSelection = true))
        api.mock_queryTagsForActivity(mapOf())
        mediator.requestStart(sampleActivity)
        `assert only start callback invoked`()
    }

    @Test
    fun `tag selection enabled, but not for generals alone, and activity has only general tags`() = runTest {
        api.mock_querySettings(settings.copy(showRecordTagSelection = true, recordTagSelectionEvenForGeneralTags = false))
        api.mock_queryTagsForActivity(mapOf(sampleActivity.id to arrayOf(sampleGeneralTag)))
        mediator.requestStart(sampleActivity)
        `assert only start callback invoked`()
    }

    private fun `assert only start callback invoked`() {
        startCallback.assertCalledWith(sampleActivity)
        startCallback.assertCallsMade(1)
        requestTagCallback.assertCallsMade(0)
    }
}

class `Requests tags when tag selection enabled and`: StartActivityMediatorTestBase() {
    private val sampleSettings = settings.copy(showRecordTagSelection = true)

    @Test
    fun `activity has non-general tags`() = runTest {
        api.mock_querySettings(sampleSettings)
        api.mock_queryTagsForActivity(mapOf(sampleActivity.id to arrayOf(sampleNonGeneralTag)))
        mediator.requestStart(sampleActivity)
        `assert only tag callback invoked`()
    }

    @Test
    fun `activity has only general tags and tag selection enabled for only generals`() = runTest {
        api.mock_querySettings(sampleSettings.copy(recordTagSelectionEvenForGeneralTags = true))
        api.mock_queryTagsForActivity(mapOf(sampleActivity.id to arrayOf(sampleGeneralTag)))
        mediator.requestStart(sampleActivity)
        `assert only tag callback invoked`()
    }

    private fun `assert only tag callback invoked`() {
        requestTagCallback.assertCalledWith(sampleActivity)
        requestTagCallback.assertCallsMade(1)
        startCallback.assertCallsMade(0)
    }
}

class MockMediatorCallback: (Activity) -> Unit {
    private var calledWith: Activity? = null
    private var callCount: Int = 0
    override fun invoke(activity: Activity) {
        calledWith = activity
        callCount++
    }

    fun assertCalledWith(activity: Activity) {
        assertEquals(activity, calledWith)
    }

    fun assertCallsMade(count: Int) {
        assertEquals(count, callCount)
    }

    fun reset() {
        calledWith = null
        callCount = 0
    }
}