package com.example.util.simpletimetracker.domain.notifications.model

enum class ExternalCommandStatus(val dataValue: String) {
    STARTED("started"),
    STOPPED("stopped"),
    ALREADY_RUNNING("already_running"),
    NOT_RUNNING("not_running"),
    UNKNOWN_ACTIVITY("unknown_activity"),
    INVALID_REQUEST("invalid_request"),
}
