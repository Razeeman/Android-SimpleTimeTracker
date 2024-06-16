package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardDoubleViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailCardDoubleItemBinding as Binding

fun createStatisticsDetailCardDoubleAdapterDelegate(
    onFirstClick: (StatisticsDetailCardInternalViewData.ClickableType, Coordinates) -> Unit,
    onSecondClick: (StatisticsDetailCardInternalViewData.ClickableType, Coordinates) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        cardStatisticsDetailFirst.items = item.first
        cardStatisticsDetailFirst.listener = onFirstClick
        cardStatisticsDetailSecond.items = item.second
        cardStatisticsDetailSecond.listener = onSecondClick
    }
}

data class StatisticsDetailCardDoubleViewData(
    val block: StatisticsDetailBlock,
    val first: List<StatisticsDetailCardInternalViewData>,
    val second: List<StatisticsDetailCardInternalViewData>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}