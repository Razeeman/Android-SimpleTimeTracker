package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.domain.model.AppColor

data class StatisticsDataHolder(
    val name: String,
    val color: AppColor,
    val icon: String?,
    val dailyGoalTime: Long,
    val weeklyGoalTime: Long,
    val monthlyGoalTime: Long,
)