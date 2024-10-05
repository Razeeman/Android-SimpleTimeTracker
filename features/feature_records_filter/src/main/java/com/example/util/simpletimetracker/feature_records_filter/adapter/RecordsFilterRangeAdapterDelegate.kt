package com.example.util.simpletimetracker.feature_records_filter.adapter

import android.view.Gravity
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterRangeViewData as ViewData
import com.example.util.simpletimetracker.feature_records_filter.databinding.ItemRecordsFilterRangeBinding as Binding

fun createRecordsFilterRangeAdapterDelegate(
    onClick: (ViewData.FieldType) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        fun ViewData.Gravity.toViewData(): Int {
            return when (this) {
                ViewData.Gravity.CENTER -> Gravity.CENTER
                ViewData.Gravity.CENTER_VERTICAL -> Gravity.CENTER_VERTICAL
            }
        }

        tvRecordsFilterRangeTimeStarted.text = item.timeStarted
        tvRecordsFilterRangeTimeStarted.gravity = item.gravity.toViewData()
        tvRecordsFilterRangeTimeStarted.setTextColor(item.textColor)
        tvRecordsFilterRangeTimeStartedHint.text = item.timeStartedHint
        fieldRecordsFilterRangeTimeStarted.setOnClick { onClick(ViewData.FieldType.TIME_STARTED) }

        tvRecordsFilterRangeTimeEnded.text = item.timeEnded
        tvRecordsFilterRangeTimeEnded.gravity = item.gravity.toViewData()
        tvRecordsFilterRangeTimeEnded.setTextColor(item.textColor)
        tvRecordsFilterRangeTimeEndedHint.text = item.timeEndedHint
        fieldRecordsFilterRangeTimeEnded.setOnClick { onClick(ViewData.FieldType.TIME_ENDED) }
    }
}
