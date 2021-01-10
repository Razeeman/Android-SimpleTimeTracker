package com.example.util.simpletimetracker.feature_notification.recordType.manager

data class NotificationTypeParams(
    val id: Int,
    val icon: Int,
    val color: Int,
    val text: String,
    val description: String,
    val startedTimeStamp: Long
)