/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


open class WearRPCServerTestBase {
    lateinit var api: MockSimpleTimeTrackerAPI
    lateinit var rpc: WearRPCServer
    lateinit var messenger: Messenger
    lateinit var client: WearRPCClient

    @Before
    fun setup() {
        api = MockSimpleTimeTrackerAPI()
        rpc = WearRPCServer(api)
        messenger = MockMessenger(rpc)
        client = WearRPCClient(messenger)
        api.mockReset()
    }
}

class WearRPCServerTest : WearRPCServerTestBase() {
    @Test
    fun returns_null_for_unsupported_request() = runTest {
        val response = rpc.onRequest("/fake/path", "fake data".toByteArray())
        assertNull(response)
    }

    @Test(expected = WearRPCException::class)
    fun raises_for_invalid_SimpleTimeTracker_request() = runTest {
        rpc.onRequest("/stt//GET/fake/path", "fake data".toByteArray())
    }
}

class PingTest : WearRPCServerTestBase() {

    @Test
    fun responds_to_empty_request_with_empty_string() = runTest {
        val response = client.ping("")
        assertEquals("", response)
    }

    @Test
    fun echoes_response() = runTest {
        val response = client.ping("Hello World!")
        assertEquals("Hello World!", response)
    }
}

class GetActivitiesTest : WearRPCServerTestBase() {
    @Test
    fun returns_no_activities_when_none_are_available() = runTest {
        val response = client.queryActivities()
        assertArrayEquals(arrayOf<Activity>(), response)
    }

    @Test
    fun returns_one_activity_when_one_exists() = runTest {
        val activities = arrayOf(Activity(42, "Chores", "🎉", "#00FF00"))
        api.mock_queryActivities(activities)
        val response = client.queryActivities()
        assertArrayEquals(activities, response)
    }

    @Test
    fun returns_all_existing_activities() = runTest {
        val activities = arrayOf(
            Activity(13, "Singing", "🎶", "#123456"),
            Activity(24, "Homework", "📝", "#ABCDEF"),
        )
        api.mock_queryActivities(activities)
        val response = client.queryActivities()
        assertArrayEquals(activities, response)
    }
}

class QueryCurrentActivitiesTest : WearRPCServerTestBase() {
    @Test
    fun returns_no_activities_when_none_are_running() = runTest {
        val activities = arrayOf<CurrentActivity>()
        val response = client.queryCurrentActivities()
        assertArrayEquals(activities, response)
    }
    @Test
    fun returns_one_activity_when_one_exists() = runTest {
        val jan_31_2024_afternoon = 1706704801L
        val activities = arrayOf(
            CurrentActivity(
                42,
                jan_31_2024_afternoon,
                arrayOf(Tag(1, "Friends"), Tag(2, "Family")),
            ),
        )
        api.mock_queryCurrentActivities(activities)
        val response = client.queryCurrentActivities()
        assertArrayEquals(activities, response)
    }

    @Test
    fun returns_all_running_activities() = runTest {
        val jan_31_2024_afternoon = 1706704801L
        val jan_31_2024_evening = 1706751601L
        val activities = arrayOf(
            CurrentActivity(
                42,
                jan_31_2024_afternoon,
                arrayOf(Tag(1, "Friends"), Tag(2, "Family")),
            ),
            CurrentActivity(
                42,
                jan_31_2024_evening,
                arrayOf(Tag(5, "Shopping")),
            ),
        )
        api.mock_queryCurrentActivities(activities)
        val response = client.queryCurrentActivities()
        assertArrayEquals(activities, response)
    }
}

class QueryTagsForActivityTest: WearRPCServerTestBase() {
    @Test
    fun returns_no_tags_if_activity_has_none() = runTest {
        api.mock_queryTagsForActivity(mapOf(13L to arrayOf()))
        val response = client.queryTagsForActivity(13)
        assertArrayEquals(arrayOf(), response)
    }

    @Test
    fun returns_no_tags_if_activity_doesnt_exist() = runTest {
        val response = client.queryTagsForActivity(42)
        assertArrayEquals(arrayOf(), response)
    }

    @Test
    fun returns_one_tag_associated_with_activity() = runTest {
        val tags = arrayOf(Tag(5, "Shopping"))
        api.mock_queryTagsForActivity(mapOf(13L to tags))
        val response = client.queryTagsForActivity(13)
        assertArrayEquals(tags, response)
    }

    @Test
    fun returns_all_tags_associated_with_activity() = runTest {
        val tags = arrayOf(Tag(5, "Shopping"), Tag(14, "Work"))
        api.mock_queryTagsForActivity(mapOf(13L to tags))
        val response = client.queryTagsForActivity(13L)
        assertArrayEquals(tags, response)
    }

    @Test
    fun returns_only_tags_associated_with_requested_activity() = runTest {
        val tags = arrayOf(Tag(5, "Shopping"), Tag(14, "Work"))
        val otherTags = arrayOf(Tag(7, "Chores"), Tag(13, "Sleep"))
        api.mock_queryTagsForActivity(mapOf(10L to tags, 17L to otherTags))
        val response = client.queryTagsForActivity(10L)
        assertArrayEquals(tags, response)
        val responseOther = client.queryTagsForActivity(17L)
        assertArrayEquals(otherTags, responseOther)
    }
}

class QuerySettingsTest: WearRPCServerTestBase() {
    @Test
    fun returns_settings_with_multitasking_enabled() = runTest {
        api.mock_querySettings(Settings(multitasking = true))
        val response = client.querySettings()
        assertTrue(response.multitasking)
    }

    @Test
    fun returns_settings_with_multitasking_disabled() = runTest {
        api.mock_querySettings(Settings(multitasking = false))
        val response = client.querySettings()
        assertFalse(response.multitasking)
    }
}


class MockSimpleTimeTrackerAPI : SimpleTimeTrackerAPI {

    var activities: Array<Activity> = arrayOf()
    var currentActivities: Array<CurrentActivity> = arrayOf()
    var tags: Map<Long, Array<Tag>> = mapOf()
    lateinit var settings: Settings

    override suspend fun queryActivities(): Array<Activity> {
        return activities
    }

    fun mock_queryActivities(activities: Array<Activity>) {
        this.activities = activities
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        return currentActivities
    }

    fun mock_queryCurrentActivities(activities: Array<CurrentActivity>) {
        this.currentActivities = activities
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>): Unit {
        TODO("Not yet implemented")
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        return this.tags.getOrDefault(activityId, arrayOf())
    }
    fun mock_queryTagsForActivity(tags: Map<Long, Array<Tag>>) {
        this.tags = tags
    }

    override suspend fun querySettings(): Settings {
        return settings
    }

    fun mock_querySettings(settings: Settings) {
        this.settings = settings
    }

    fun mockReset() {
        this.activities = arrayOf()
        this.currentActivities = arrayOf()
        this.tags = mapOf()
    }

}

class MockMessenger(private val rpc: WearRPCServer) : Messenger {

    override suspend fun send(capability: String, message: ByteArray): ByteArray? {
        return rpc.onRequest(capability, message)
    }

}