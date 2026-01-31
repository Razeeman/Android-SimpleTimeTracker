package com.example.util.simpletimetracker.domain.widget.model

// Ids are either filtered or selected,
// depending on filtering type.
data class GridWidgetData(
    val typeIds: Set<Long>,
    val filteringType: WidgetDataFilterType,
)
