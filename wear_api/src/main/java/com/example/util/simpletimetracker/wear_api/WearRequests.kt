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
    const val PING = "$PATH//GET/ping"
    const val QUERY_ACTIVITIES = "$PATH//GET/activities"
    const val QUERY_CURRENT_ACTIVITIES = "$PATH//GET/activities/current"
    const val SET_CURRENT_ACTIVITIES = "$PATH//PUT/activities/current"
    const val QUERY_TAGS_FOR_ACTIVITY = "$PATH//GET/activities/:ID/tags"
    const val QUERY_SETTINGS = "$PATH//GET/settings"

    // From app to wear.
    const val DATA_UPDATED = "$PATH//data/updated"
}