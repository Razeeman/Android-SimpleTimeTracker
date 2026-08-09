package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import android.graphics.Color
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData

fun ViewHolderType.mapItem(): StatisticsDetailViewData<*> {
    return StatisticsDetailViewData(
        item = this,
    )
}

fun StatisticsDetailBarChartViewData.mapItem(
    forComparison: Boolean,
): StatisticsDetailViewData<*> {
    return StatisticsDetailViewData(
        item = this,
        itemProducer = { preview ->
            val color = if (forComparison) preview?.comparisonPreviewColor else preview?.previewColor
            this.copy(singleColor = color ?: Color.BLACK)
        },
    )
}