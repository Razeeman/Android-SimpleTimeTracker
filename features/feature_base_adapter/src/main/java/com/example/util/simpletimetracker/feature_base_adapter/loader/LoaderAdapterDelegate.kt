package com.example.util.simpletimetracker.feature_base_adapter.loader

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemLoaderLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData as ViewData

fun createLoaderAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { _, _, _ ->

    // Nothing to bind
}