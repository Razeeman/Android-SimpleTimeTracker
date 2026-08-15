package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import android.graphics.Color
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesCalendarViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData

fun ViewHolderType.mapItem(): StatisticsDetailViewData<*> =
    StatisticsDetailViewData(item = this)

fun List<ViewHolderType>.mapItems(): List<StatisticsDetailViewData<*>> =
    this.map { it.mapItem() }

fun StatisticsDetailBarChartViewData.mapItem(
    forComparison: Boolean,
): StatisticsDetailViewData<*> = this.mapItemInternal(forComparison) { copy(singleColor = it) }

fun StatisticsDetailSeriesChartViewData.mapItem(
    forComparison: Boolean,
): StatisticsDetailViewData<*> = this.mapItemInternal(forComparison) { copy(color = it) }

fun StatisticsDetailSeriesCalendarViewData.mapItem(
    forComparison: Boolean,
): StatisticsDetailViewData<*> = this.mapItemInternal(forComparison) { copy(color = it) }

private fun <T : ViewHolderType> T.mapItemInternal(
    forComparison: Boolean,
    itemProducer: T.(color: Int) -> T,
): StatisticsDetailViewData<*> {
    return StatisticsDetailViewData(
        item = this,
        itemProducer = { preview ->
            val color = if (forComparison) preview?.comparisonPreviewColor else preview?.previewColor
            this.itemProducer(color ?: Color.BLACK)
        },
    )
}
