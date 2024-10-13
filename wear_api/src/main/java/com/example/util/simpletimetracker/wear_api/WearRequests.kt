/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear_api

object WearRequests {
    const val PATH = "/stt"

    // From wear to app.
    // Same strings are set in the android_wear_capabilities manifest
    const val QUERY_ACTIVITIES = "$PATH/QUERY_ACTIVITIES"
    const val QUERY_CURRENT_ACTIVITIES = "$PATH/QUERY_CURRENT_ACTIVITIES"
    const val START_ACTIVITY = "$PATH/START_ACTIVITY"
    const val STOP_ACTIVITY = "$PATH/STOP_ACTIVITY"
    const val REPEAT_ACTIVITY = "$PATH/REPEAT_ACTIVITY"
    const val QUERY_TAGS_FOR_ACTIVITY = "$PATH/QUERY_TAGS_FOR_ACTIVITY"
    const val QUERY_SHOULD_SHOW_TAG_SELECTION = "$PATH/QUERY_SHOULD_SHOW_TAG_SELECTION"
    const val QUERY_SETTINGS = "$PATH/QUERY_SETTINGS"
    const val SET_SETTINGS = "$PATH/SET_SETTINGS"
    const val OPEN_PHONE_APP = "$PATH/OPEN_PHONE_APP"

    // From app to wear.
    const val DATA_UPDATED = "$PATH/DATA_UPDATED"
}