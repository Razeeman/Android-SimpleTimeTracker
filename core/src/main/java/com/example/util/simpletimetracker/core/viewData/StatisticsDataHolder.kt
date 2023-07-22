package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.domain.model.AppColor

/**
 * Type, category or tag.
 */
data class StatisticsDataHolder(
    val name: String,
    val color: AppColor,
    val icon: String?,
)