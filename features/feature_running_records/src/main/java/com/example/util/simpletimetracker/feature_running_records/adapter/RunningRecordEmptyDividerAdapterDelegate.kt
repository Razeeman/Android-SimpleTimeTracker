package com.example.util.simpletimetracker.feature_running_records.adapter

import com.example.util.simpletimetracker.feature_running_records.databinding.ItemRunningRecordEmptyDividerBinding as Binding
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordEmptyDividerViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate

fun createRunningRecordEmptyDividerAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { _, _, _ ->

    // Nothing to bind
}