package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData as ViewData
import com.example.util.simpletimetracker.feature_records_filter.databinding.ItemRecordsFilterRangeBinding as Binding

fun createRecordsFilterRangeAdapterDelegate(
    onClick: (ViewData.FieldType) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvRecordsFilterRangeTimeStarted.text = item.timeStarted
        tvRecordsFilterRangeTimeEnded.text = item.timeEnded
        fieldRecordsFilterRangeTimeStarted.setOnClick { onClick(ViewData.FieldType.TIME_STARTED) }
        fieldRecordsFilterRangeTimeEnded.setOnClick { onClick(ViewData.FieldType.TIME_ENDED) }
    }
}
