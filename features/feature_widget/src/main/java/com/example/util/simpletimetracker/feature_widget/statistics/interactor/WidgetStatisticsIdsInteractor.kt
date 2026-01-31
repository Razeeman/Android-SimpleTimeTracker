package com.example.util.simpletimetracker.feature_widget.statistics.interactor

import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.widget.model.StatisticsWidgetData
import com.example.util.simpletimetracker.feature_widget.common.WidgetGetActualFilteredIdsInteractor
import javax.inject.Inject

class WidgetStatisticsIdsInteractor @Inject constructor(
    private val wdGetActualFilteredIdsInteractor: WidgetGetActualFilteredIdsInteractor,
) {

    fun getAllTypeIds(
        typeIds: Set<Long>,
    ): Set<Long> {
        return typeIds + UNTRACKED_ITEM_ID
    }

    fun getAllCategoryIds(
        categoryIds: Set<Long>,
    ): Set<Long> {
        return categoryIds + UNTRACKED_ITEM_ID + UNCATEGORIZED_ITEM_ID
    }

    fun getAllTagIds(
        tagIds: Set<Long>,
    ): Set<Long> {
        return tagIds + UNTRACKED_ITEM_ID + UNCATEGORIZED_ITEM_ID
    }

    suspend fun getActualFilteredIds(
        widgetData: StatisticsWidgetData,
        typeIds: suspend () -> Set<Long>,
        categoryIds: suspend () -> Set<Long>,
        tagIds: suspend () -> Set<Long>,
    ): Set<Long> {
        val filterType = widgetData.chartFilterType

        val widgetItemIds = when (filterType) {
            ChartFilterType.ACTIVITY -> widgetData.typeIds
            ChartFilterType.CATEGORY -> widgetData.categoryIds
            ChartFilterType.RECORD_TAG -> widgetData.tagIds
        }.toSet()

        return wdGetActualFilteredIdsInteractor.execute(
            filterType = widgetData.filteringType,
            widgetItemIds = widgetItemIds,
            allItemIds = when (filterType) {
                ChartFilterType.ACTIVITY -> getAllTypeIds(typeIds.invoke())
                ChartFilterType.CATEGORY -> getAllCategoryIds(categoryIds.invoke())
                ChartFilterType.RECORD_TAG -> getAllTagIds(tagIds.invoke())
            },
        )
    }
}