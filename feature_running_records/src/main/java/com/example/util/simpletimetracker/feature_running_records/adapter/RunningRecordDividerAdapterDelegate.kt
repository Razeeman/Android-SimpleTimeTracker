package com.example.util.simpletimetracker.feature_running_records.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordDividerViewData

fun createRunningRecordDividerAdapterDelegate() = createRecyclerAdapterDelegate<RunningRecordDividerViewData>(
    R.layout.item_running_record_divider_layout
) { _, _, _ ->

    // Nothing to bind
}