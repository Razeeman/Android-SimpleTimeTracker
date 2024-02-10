/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wearrpc

object Request {
    val PING = "/stt//GET/ping"
    val GET_ACTIVITIES = "/stt//GET/activities"
    val GET_CURRENT_ACTIVITIES = "/stt//GET/activities/current"
    val START_ACTIVITIES = "/stt//POST/activities/current"
    val GET_TAGS_FOR_ACTIVITY = "/stt//GET/activities/:ID/tags"
}