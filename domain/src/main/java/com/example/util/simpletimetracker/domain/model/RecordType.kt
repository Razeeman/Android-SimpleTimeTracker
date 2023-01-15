package com.example.util.simpletimetracker.domain.model

data class RecordType(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: AppColor,
    val hidden: Boolean = false,
    val goalTime: Long, // Seconds.
    val dailyGoalTime: Long, // Seconds.
    val weeklyGoalTime: Long, // Seconds.
    val monthlyGoalTime: Long, // Seconds.
)