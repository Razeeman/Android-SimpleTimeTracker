package com.example.util.simpletimetracker.core.adapter.divider

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.divider.DividerViewData as ViewData
import com.example.util.simpletimetracker.core.databinding.ItemDividerLayoutBinding as Binding

fun createDividerAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { _, _, _ ->

    // Nothing to bind
}
