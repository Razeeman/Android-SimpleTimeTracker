package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.customView.SeriesCalendarView
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailSeriesCalendarViewData as ViewData
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailSeriesCalendarItemBinding as Binding

fun createStatisticsDetailSeriesCalendarAdapterDelegate(
    onClick: (SeriesCalendarView.ViewData, coordinates: Coordinates) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        setCellColor(item.color)
        setData(item.data)
        setClickListener(onClick)
    }
}

data class StatisticsDetailSeriesCalendarViewData(
    val block: StatisticsDetailBlock,
    @ColorInt val color: Int,
    val data: List<SeriesCalendarView.ViewData>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}