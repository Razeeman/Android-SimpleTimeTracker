package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailCardItemBinding as Binding

fun createStatisticsDetailCardAdapterDelegate(
    onClick: (StatisticsDetailCardInternalViewData.ClickableType, Coordinates) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        tag = item.block
        setMargins(top = item.marginTopDp)
        itemsDescription = item.title
        items = item.data
        listener = onClick
    }
}

data class StatisticsDetailCardViewData(
    val block: StatisticsDetailBlock,
    val title: String,
    val marginTopDp: Int,
    val data: List<StatisticsDetailCardInternalViewData>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}