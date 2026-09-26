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
    suspend fun queryActivities(): List<WearActivityDTO>

    /**
     * [WearRequests.QUERY_CURRENT_ACTIVITIES]
     *
     * Retrieves a list of the currently running activity/activities
     */
    suspend fun queryCurrentActivities(): WearCurrentStateDTO

    /**
     * [WearRequests.QUERY_STATISTICS]
     *
     * Retrieves a list of statistics
     */
    suspend fun queryStatistics(request: WearStatisticsRequest): List<WearStatisticsDTO>?

    /**
     * [WearRequests.QUERY_RECORDS]
     *
     * Retrieves the records shown for one shifted day.
     */
    suspend fun queryRecords(request: WearRecordsRequest): List<WearRecordDTO>?

    /**
     * [WearRequests.START_ACTIVITY]
     *
     * Starts new timer.
     */
    suspend fun startActivity(request: WearStartActivityRequest)

    /**
     * [WearRequests.STOP_ACTIVITY]
     *
     * Stops timer.
     */
    suspend fun stopActivity(request: WearStopActivityRequest)

    /**
     * [WearRequests.REPEAT_ACTIVITY]
     *
     * Repeat last timer.
     */
    suspend fun repeatActivity(): WearRecordRepeatResponse

    /**
     * [WearRequests.QUERY_TAGS_FOR_ACTIVITY]
     *
     * Retrieves the tags available for association with the activity with the given ID
     */
    suspend fun queryTagsForActivity(activityId: Long): List<WearTagDTO>

    /**
     * [WearRequests.QUERY_SHOULD_SHOW_TAG_SELECTION]
     *
     * Decides if should show tag selection on activity click.
     */
    suspend fun queryShouldShowTagSelection(
        request: WearShouldShowTagSelectionRequest,
    ): WearShouldShowTagSelectionResponse?

    /**
     * [WearRequests.QUERY_SHOULD_SHOW_TAG_VALUE_SELECTION]
     *
     * Decides if should show tag value selection on tag click.
     */
    suspend fun queryShouldShowTagValueSelection(
        request: WearShouldShowTagValueSelectionRequest,
    ): WearShouldShowTagValueSelectionResponse?

    /**
     * [WearRequests.QUERY_SETTINGS]
     *
     * Retrieves the settings relevant to time tracking behavior
     */
    suspend fun querySettings(): WearSettingsDTO

    /**
     * [WearRequests.SET_SETTINGS]
     *
     * Set app settings from wear.
     */
    suspend fun setSettings(settings: WearSetSettingsRequest)

    /**
     * [WearRequests.OPEN_PHONE_APP]
     *
     * Starts application on the phone.
     */
    suspend fun openPhoneApp()
}
