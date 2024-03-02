/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear_api

class MockWearCommunicationAPI : WearCommunicationAPI {

    var activities: List<WearActivity> = emptyList()
    var currentActivities: List<WearCurrentActivity> = emptyList()
    var tags: Map<Long, List<WearTag>> = mapOf()
    lateinit var settings: WearSettings

    override suspend fun queryActivities(): List<WearActivity> {
        return activities
    }

    fun mock_queryActivities(activities: List<WearActivity>) {
        this.activities = activities
    }

    override suspend fun queryCurrentActivities(): List<WearCurrentActivity> {
        return currentActivities
    }

    fun mock_queryCurrentActivities(activities: List<WearCurrentActivity>) {
        this.currentActivities = activities
    }

    override suspend fun setCurrentActivities(starting: List<WearCurrentActivity>) {
        TODO("Not yet implemented")
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTag> {
        return this.tags[activityId].orEmpty()
    }

    fun mock_queryTagsForActivity(tags: Map<Long, List<WearTag>>) {
        this.tags = tags
    }

    override suspend fun querySettings(): WearSettings {
        return settings
    }

    fun mock_querySettings(settings: WearSettings) {
        this.settings = settings
    }

    fun mockReset() {
        this.activities = emptyList()
        this.currentActivities = emptyList()
        this.tags = mapOf()
    }

}