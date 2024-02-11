package com.example.util.simpletimetracker.feature_base_adapter.divider

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemDividerLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData as ViewData

fun createDividerAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { _, _, _ ->

    // Nothing to bind
}
