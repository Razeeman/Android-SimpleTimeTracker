package com.example.util.simpletimetracker.navigation.params

import com.example.util.simpletimetracker.domain.model.ChartFilterType

data class StatisticsDetailParams(
    val id: Long,
    val filterType: ChartFilterType
)