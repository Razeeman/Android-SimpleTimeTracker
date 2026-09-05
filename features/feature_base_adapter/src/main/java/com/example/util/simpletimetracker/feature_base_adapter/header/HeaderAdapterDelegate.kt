package com.example.util.simpletimetracker.feature_base_adapter.header

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemHeaderLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.header.HeaderViewData as ViewData

fun createHeaderAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(Binding::inflate) { binding, item, _ ->
        item as ViewData
        binding.tvHeaderItem.text = item.text
    }
