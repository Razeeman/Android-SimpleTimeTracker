package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.ChartGrouping

data class StatisticsDetailGroupingViewData(
    val chartGrouping: ChartGrouping,
    val name: String,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW

    override fun getUniqueId(): Long? = chartGrouping.ordinal.toLong()
}