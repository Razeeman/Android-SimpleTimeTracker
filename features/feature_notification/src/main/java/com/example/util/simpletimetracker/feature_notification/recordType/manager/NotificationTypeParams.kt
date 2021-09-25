package com.example.util.simpletimetracker.feature_notification.recordType.manager

import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon

data class NotificationTypeParams(
    val id: Int,
    val icon: RecordTypeIcon,
    val color: Int,
    val text: String,
    val timeStarted: String,
    val startedTimeStamp: Long,
    val goalTime: String
)