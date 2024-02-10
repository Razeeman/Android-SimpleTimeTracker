/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

interface SimpleTimeTrackerAPI {
    /**
     * /stt//GET/ping
     *
     * Echos the message it receives
     *
     * Primarily used to test request/response functionality
     */
    suspend fun ping(str: String): String {
        return str
    }

    /**
     * /stt//GET/activities
     *
     * Retrieves a list of all the time-tracking activities available for selection
     */
    suspend fun queryActivities(): Array<Activity>

    /**
     * /stt//GET/activities/current
     *
     * Retrieves a list of the currently running activity/activities
     */
    suspend fun queryCurrentActivities(): Array<CurrentActivity>

    /**
     * /stt//POST/activities/current
     *
     * Replaces the currently running activity/activities with the given activities
     */
    suspend fun setCurrentActivities(activities: Array<CurrentActivity>): Unit

    /**
     * /stt//GET/activities/:ID/tags
     *
     * Retrieves the tags available for association with the activity with the given ID
     */
    suspend fun queryTagsForActivity(activityId: Long): Array<Tag>

    /**
     * /stt//GET/settings
     *
     * Retrieves the settings relevant to time tracking behavior
     */
    suspend fun querySettings(): Settings
}
