/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

object Request {
    val PING = "/stt//GET/ping"
    val QUERY_ACTIVITIES = "/stt//GET/activities"
    val QUERY_CURRENT_ACTIVITIES = "/stt//GET/activities/current"
    val SET_CURRENT_ACTIVITIES = "/stt//PUT/activities/current"
    val QUERY_TAGS_FOR_ACTIVITY = "/stt//GET/activities/:ID/tags"
    val QUERY_SETTINGS = "/stt//GET/settings"
}