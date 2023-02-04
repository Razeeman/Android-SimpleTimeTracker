package com.example.util.simpletimetracker.feature_notification.recordType.manager

import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class NotificationTypeParams(
    val id: Long,
    val icon: RecordTypeIcon,
    val color: Int,
    val text: String,
    val timeStarted: String,
    val startedTimeStamp: Long,
    val goalTime: String,
    val stopButton: String,
)