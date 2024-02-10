package com.example.util.simpletimetracker.wearrpc

object Request {
    val PING = "/stt//GET/ping"
    val GET_ACTIVITIES = "/stt//GET/activities"
    val GET_CURRENT_ACTIVITIES = "/stt//GET/activities/current"
    val START_ACTIVITIES = "/stt//POST/activities/current"
    val GET_TAGS_FOR_ACTIVITY = "/stt//GET/activities/:ID/tags"
}