/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear_api

interface WearCommunicationAPI {
    /**
     * [WearRequests.QUERY_ACTIVITIES]
     *
     * Retrieves a list of all the time-tracking activities available for selection
     */
    suspend fun queryActivities(): List<WearActivity>

    /**
     * [WearRequests.QUERY_CURRENT_ACTIVITIES]
     *
     * Retrieves a list of the currently running activity/activities
     */
    suspend fun queryCurrentActivities(): List<WearCurrentActivity>

    /**
     * [WearRequests.SET_CURRENT_ACTIVITIES]
     *
     * Replaces the currently running activity/activities with the given activities
     */
    suspend fun setCurrentActivities(starting: List<WearCurrentActivity>)

    /**
     * [WearRequests.QUERY_TAGS_FOR_ACTIVITY]
     *
     * Retrieves the tags available for association with the activity with the given ID
     */
    suspend fun queryTagsForActivity(activityId: Long): List<WearTag>

    /**
     * [WearRequests.QUERY_SETTINGS]
     *
     * Retrieves the settings relevant to time tracking behavior
     */
    suspend fun querySettings(): WearSettings

    /**
     * [WearRequests.SET_SETTINGS]
     *
     * Set app settings from wear.
     */
    suspend fun setSettings(settings: WearSettings)

    /**
     * [WearRequests.OPEN_PHONE_APP]
     *
     * Starts application on the phone.
     */
    suspend fun openPhoneApp()
}
