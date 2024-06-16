package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.model.OneShotValue
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesView
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesChartViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailSeriesChartItemBinding as Binding

fun createStatisticsDetailSeriesChartAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        setBarColor(item.color)
        setData(data = item.data, animate = item.animate.getValue().orFalse())
    }
}

data class StatisticsDetailSeriesChartViewData(
    val block: StatisticsDetailBlock,
    @ColorInt val color: Int,
    val data: List<SeriesView.ViewData>,
    val animate: OneShotValue<Boolean>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}