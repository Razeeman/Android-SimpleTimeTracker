package com.example.util.simpletimetracker.feature_records_filter.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_records_filter.adapter.RecordsFilterDurationViewData as ViewData
import com.example.util.simpletimetracker.feature_records_filter.databinding.ItemRecordsFilterDurationBinding as Binding

fun createRecordsFilterDurationAdapterDelegate(
    onClick: (ViewData.FieldType) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvRecordsFilterDurationFrom.text = item.durationFrom
        tvRecordsFilterDurationTo.text = item.durationTo
        fieldRecordsFilterDurationFrom.setOnClick { onClick(ViewData.FieldType.FROM) }
        fieldRecordsFilterDurationTo.setOnClick { onClick(ViewData.FieldType.TO) }
    }
}
