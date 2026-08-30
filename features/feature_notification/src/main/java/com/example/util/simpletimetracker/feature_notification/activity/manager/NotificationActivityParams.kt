package com.example.util.simpletimetracker.feature_notification.activity.manager

import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class NotificationActivityParams(
    val activityId: Long,
    val title: String,
    val subtitle: String,
    val icon: RecordTypeIcon,
    val color: Int,
)