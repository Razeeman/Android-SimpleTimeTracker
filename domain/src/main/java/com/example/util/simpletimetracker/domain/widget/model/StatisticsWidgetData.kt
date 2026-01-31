package com.example.util.simpletimetracker.domain.widget.model

import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength

// Ids are either filtered or selected,
// depending on filtering type.
data class StatisticsWidgetData(
    val chartFilterType: ChartFilterType,
    val rangeLength: RangeLength,
    val typeIds: Set<Long>,
    val categoryIds: Set<Long>,
    val tagIds: Set<Long>,
    val filteringType: WidgetDataFilterType,
)