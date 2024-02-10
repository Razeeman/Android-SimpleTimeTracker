/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import kotlinx.coroutines.test.runTest


open class WearRPCServerTestBase {
    lateinit var api: MockSimpleTimeTrackerAPI
    lateinit var rpc: WearRPCServer


    @Before fun setup() {
        api = MockSimpleTimeTrackerAPI()
        rpc = WearRPCServer(api)
        api.mockReset()
    }

    fun assertResponseEquals(expected: String, response: ByteArray?) {
        if (response != null) assertEquals(expected, String(response))
        else fail("response was unexpectedly null")
    }
}

class WearRPCServerTest: WearRPCServerTestBase() {
    @Test fun returns_null_for_unsupported_request() = runTest {
        val response = rpc.onRequest("/fake/path", "fake data".toByteArray())
        assertNull(response)
    }

    @Test(expected = WearRPCException::class)
    fun raises_for_invalid_SimpleTimeTracker_request() = runTest {
        rpc.onRequest("/stt//GET/fake/path", "fake data".toByteArray())
    }
}

class PingEndpointTest: WearRPCServerTestBase() {

    @Test fun responds_to_empty_request_with_empty_string() = runTest {
        val response = rpc.onRequest(Request.PING, ByteArray(0))
        assertResponseEquals("", response)
    }

    @Test fun echoes_response() = runTest {
        val response = rpc.onRequest(Request.PING, "Hello World!".toByteArray())
        assertResponseEquals("Hello World!", response)
    }
}

class GetActivitiesTest: WearRPCServerTestBase() {
    @Test fun returns_no_activities_when_none_are_available() = runTest {
        val response = rpc.onRequest(Request.GET_ACTIVITIES, ByteArray(0))
        assertResponseEquals("[]", response)
    }

    @Test fun returns_one_activity_when_one_exists() = runTest {
        api.mock_queryActivities(arrayOf(Activity(42, "Chores")))
        val response = rpc.onRequest(Request.GET_ACTIVITIES, ByteArray(0))
        assertResponseEquals("[{\"id\":42,\"name\":\"Chores\"}]", response)
    }

    @Test fun returns_all_existing_activities() = runTest {
        api.mock_queryActivities(arrayOf(Activity(13, "Singing"), Activity(24, "Homework")))
        val response = rpc.onRequest(Request.GET_ACTIVITIES, ByteArray(0))
        assertResponseEquals("[{\"id\":13,\"name\":\"Singing\"},{\"id\":24,\"name\":\"Homework\"}]", response)
    }
}


class MockSimpleTimeTrackerAPI: SimpleTimeTrackerAPI {

    lateinit var activities: Array<Activity>
    override suspend fun queryActivities(): Array<Activity> {
        return activities
    }

    fun mock_queryActivities(activities: Array<Activity>) {
        this.activities = activities
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        TODO("Not yet implemented")
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>): Unit {
        TODO("Not yet implemented")
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        TODO("Not yet implemented")
    }

    override suspend fun querySettings(): Settings {
        TODO("Not yet implemented")
    }

    fun mockReset() {
        this.activities = arrayOf()
    }

}