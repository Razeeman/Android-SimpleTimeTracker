package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import androidx.annotation.ColorInt
import androidx.core.view.marginTop
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailBarChartItemBinding as Binding

fun createStatisticsDetailBarChartAdapterDelegate(
    onBarClick: (StatisticsDetailBlock, Long?) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        tag = item.block
        setMargins(top = marginTop)
        val viewData = item.data
        showSelectedBarOnStart(viewData.showSelectedBarOnStart)
        setBars(
            data = viewData.data,
            selectedBarPosition = viewData.selectedBarPosition,
            animate = viewData.animate.getValue().orFalse(),
        )
        setYAxisZoomed(viewData.yAxisZoomed)
        setLegendTextSuffix(viewData.legendSuffix)
        shouldAddLegendToSelectedBar(viewData.addLegendToSelectedBar)
        shouldDrawHorizontalLegends(viewData.shouldDrawHorizontalLegends)
        setGoalValues(viewData.goalValues)
        setSingleColor(item.singleColor.takeIf { viewData.useSingleColor })
        setDrawRoundCaps(viewData.drawRoundCaps)
        setOnBarClickListener { onBarClick(item.block, it) }
    }
}

data class StatisticsDetailBarChartViewData(
    val block: StatisticsDetailBlock,
    @ColorInt val singleColor: Int?,
    val marginTopDp: Int,
    val data: StatisticsDetailChartViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}