package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData as ViewData
import com.example.util.simpletimetracker.feature_records_filter.databinding.ItemRecordsFilterRangeBinding as Binding

fun createRecordsFilterRangeAdapterDelegate(
    onTimeStartedClick: () -> Unit,
    onTimeEndedClick: () -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvRecordsFilterRangeTimeStarted.text = item.timeStarted
        tvRecordsFilterRangeTimeEnded.text = item.timeEnded
        fieldRecordsFilterRangeTimeStarted.setOnClick(onTimeStartedClick)
        fieldRecordsFilterRangeTimeEnded.setOnClick(onTimeEndedClick)
    }
}
