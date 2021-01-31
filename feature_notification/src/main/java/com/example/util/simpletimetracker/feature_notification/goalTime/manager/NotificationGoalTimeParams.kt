package com.example.util.simpletimetracker.feature_notification.goalTime.manager

data class NotificationGoalTimeParams(
    val typeId: Long,
    val icon: Int,
    val color: Int,
    val text: String,
    val description: String
)