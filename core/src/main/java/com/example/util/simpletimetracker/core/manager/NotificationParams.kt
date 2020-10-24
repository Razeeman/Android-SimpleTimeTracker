package com.example.util.simpletimetracker.core.manager

data class NotificationParams(
    val id: Int,
    val icon: Int,
    val color: Int,
    val text: String,
    val description: String,
    val startedTimeStamp: Long
)