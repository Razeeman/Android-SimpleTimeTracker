package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import androidx.annotation.ColorInt
import androidx.core.view.marginTop
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailBarChartItemBinding as Binding

fun createStatisticsDetailBarChartAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        setBarColor(item.color)

        setMargins(top = marginTop)
        val viewData = item.data
        showSelectedBarOnStart(viewData.showSelectedBarOnStart)
        setBars(viewData.data)
        setLegendTextSuffix(viewData.legendSuffix)
        shouldAddLegendToSelectedBar(viewData.addLegendToSelectedBar)
        shouldDrawHorizontalLegends(viewData.shouldDrawHorizontalLegends)
        setGoalValue(viewData.goalValue)
    }
}

data class StatisticsDetailBarChartViewData(
    val block: StatisticsDetailBlock,
    @ColorInt val color: Int,
    val marginTopDp: Int,
    val data: StatisticsDetailChartViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}