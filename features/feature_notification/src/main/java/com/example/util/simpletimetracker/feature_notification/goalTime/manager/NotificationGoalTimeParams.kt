package com.example.util.simpletimetracker.feature_notification.goalTime.manager

import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon

data class NotificationGoalTimeParams(
    val typeId: Long,
    val icon: RecordTypeIcon,
    val color: Int,
    val text: String,
    val description: String
)