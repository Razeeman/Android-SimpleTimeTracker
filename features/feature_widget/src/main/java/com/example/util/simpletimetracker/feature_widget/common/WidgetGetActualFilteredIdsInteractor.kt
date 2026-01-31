package com.example.util.simpletimetracker.feature_widget.common

import com.example.util.simpletimetracker.domain.widget.model.WidgetDataFilterType
import javax.inject.Inject

class WidgetGetActualFilteredIdsInteractor @Inject constructor() {

    fun execute(
        filterType: WidgetDataFilterType,
        widgetItemIds: Set<Long>,
        allItemIds: Set<Long>,
    ): Set<Long> {
        return when (filterType) {
            WidgetDataFilterType.FILTER -> {
                widgetItemIds
            }
            WidgetDataFilterType.SELECT -> {
                allItemIds.filter { it !in widgetItemIds }.toSet()
            }
        }
    }
}