package com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemDayOfWeekBinding as Binding

fun createDayOfWeekAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerDayOfWeekItem.setCardBackgroundColor(item.color)
        btnDayOfWeekItem.text = item.text
        btnDayOfWeekItem.setOnClickWith(item, onClick)
    }
}
