/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.wearrpc.Activity
import com.example.util.simpletimetracker.wearrpc.CurrentActivity
import com.example.util.simpletimetracker.presentation.data.Messenger
import com.example.util.simpletimetracker.wearrpc.MockWearCommunicationAPI
import com.example.util.simpletimetracker.wearrpc.Settings
import com.example.util.simpletimetracker.wearrpc.Tag
import com.example.util.simpletimetracker.presentation.data.WearRPCClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// TODO use mockito
@Suppress("LocalVariableName")
class WearRPCServerTest {
    private lateinit var api: MockWearCommunicationAPI
    private lateinit var rpc: WearRPCServer
    private lateinit var messenger: Messenger
    private lateinit var client: WearRPCClient

    private val sampleSettings = Settings(
        allowMultitasking = true,
        showRecordTagSelection = false,
        recordTagSelectionCloseAfterOne = false,
        recordTagSelectionEvenForGeneralTags = false,
    )

    private val tagFriends = Tag(1, "Friends", isGeneral = false, 0xFFFD3251)
    private val tagFamily = Tag(2, "Family", isGeneral = true, 0xFFFD3251)
    private val tagShopping = Tag(3, "Shopping", isGeneral = false, 0xFFFF0000)
    private val tagWork = Tag(14, "Work", isGeneral = false, 0xFF00FF00)

    @Before
    fun setup() {
        api = MockWearCommunicationAPI()
        rpc = WearRPCServer(api)
        messenger = MockMessenger(rpc)
        client = WearRPCClient(messenger)
        api.mockReset()
    }

    @Test
    fun returns_null_for_unsupported_request() = runTest {
        val response = rpc.onRequest("/fake/path", "fake data".toByteArray())
        assertNull(response)
    }

    @Test
    fun returns_null_for_invalid_request() = runTest {
        val response = rpc.onRequest("/stt//GET/fake/path", "fake data".toByteArray())
        assertNull(response)
    }

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

    @Test
    fun returns_no_activities_when_none_are_available() = runTest {
        val response = client.queryActivities()
        assertArrayEquals(arrayOf<Activity>(), response)
    }

    @Test
    fun returns_one_activity_when_one_exists() = runTest {
        val activities = arrayOf(Activity(42, "Chores", "🎉", 0xFF00FF00))
        api.mock_queryActivities(activities)
        val response = client.queryActivities()
        assertArrayEquals(activities, response)
    }

    @Test
    fun returns_all_existing_activities() = runTest {
        val activities = arrayOf(
            Activity(13, "Singing", "🎶", 0xFF123456),
            Activity(24, "Homework", "📝", 0xFFABCDEF),
        )
        api.mock_queryActivities(activities)
        val response = client.queryActivities()
        assertArrayEquals(activities, response)
    }

    @Test
    fun returns_no_activities_when_none_are_running() = runTest {
        val activities = arrayOf<CurrentActivity>()
        val response = client.queryCurrentActivities()
        assertArrayEquals(activities, response)
    }

    @Test
    fun returns_one_current_activity_when_one_exists() = runTest {
        val jan_31_2024_afternoon = 1706704801L
        val activities = arrayOf(
            CurrentActivity(
                42,
                jan_31_2024_afternoon,
                arrayOf(tagFriends, tagFamily),
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
                arrayOf(tagFriends, tagFamily),
            ),
            CurrentActivity(
                42,
                jan_31_2024_evening,
                arrayOf(tagShopping),
            ),
        )
        api.mock_queryCurrentActivities(activities)
        val response = client.queryCurrentActivities()
        assertArrayEquals(activities, response)
    }

    @Test
    fun returns_no_tags_if_activity_has_none() = runTest {
        api.mock_queryTagsForActivity(mapOf(13L to arrayOf()))
        val response = client.queryTagsForActivity(13)
        assertArrayEquals(arrayOf(), response)
    }

    @Test
    fun returns_no_tags_if_activity_does_not_exist() = runTest {
        val response = client.queryTagsForActivity(42)
        assertArrayEquals(arrayOf(), response)
    }

    @Test
    fun returns_one_tag_associated_with_activity() = runTest {
        val tags = arrayOf(tagShopping)
        api.mock_queryTagsForActivity(mapOf(13L to tags))
        val response = client.queryTagsForActivity(13)
        assertArrayEquals(tags, response)
    }

    @Test
    fun returns_all_tags_associated_with_activity() = runTest {
        val tags = arrayOf(tagShopping, tagWork)
        api.mock_queryTagsForActivity(mapOf(13L to tags))
        val response = client.queryTagsForActivity(13L)
        assertArrayEquals(tags, response)
    }

    @Test
    fun returns_only_tags_associated_with_requested_activity() = runTest {
        val tags = arrayOf(tagShopping, tagWork)
        val otherTags = arrayOf(tagFriends, tagFamily)
        api.mock_queryTagsForActivity(mapOf(10L to tags, 17L to otherTags))
        val response = client.queryTagsForActivity(10L)
        assertArrayEquals(tags, response)
        val responseOther = client.queryTagsForActivity(17L)
        assertArrayEquals(otherTags, responseOther)
    }

    @Test
    fun returns_settings_with_multitasking_enabled() = runTest {
        api.mock_querySettings(sampleSettings)
        val response = client.querySettings()
        assertTrue(response.allowMultitasking)
    }

    @Test
    fun returns_settings_with_multitasking_disabled() = runTest {
        api.mock_querySettings(sampleSettings.copy(allowMultitasking = false))
        val response = client.querySettings()
        assertFalse(response.allowMultitasking)
    }

    class MockMessenger(private val rpc: WearRPCServer) :
        Messenger {

        override suspend fun send(capability: String, message: ByteArray): ByteArray? {
            return rpc.onRequest(capability, message)
        }
    }
}