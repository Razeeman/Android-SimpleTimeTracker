/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

interface SimpleTimeTrackerAPI {
    /** /stt//GET/ping */
    suspend fun ping(str: String): String {
        return str
    }

    /** /stt//GET/activities */
    suspend fun queryActivities(): Array<Activity>

    /** /stt//GET/activities/current */
    suspend fun queryCurrentActivities(): Array<CurrentActivity>

    /** /stt//POST/activities/current */
    suspend fun setCurrentActivities(activities: Array<CurrentActivity>): Unit

    /** /stt//GET/activities/:ID/tags */
    suspend fun queryTagsForActivity(activityId: Long): Array<Tag>

    /** /stt//GET/settings */
    suspend fun querySettings(): Settings
}
