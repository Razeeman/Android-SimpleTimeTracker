package com.example.util.simpletimetracker.core.adapter.loader

import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData as ViewData
import com.example.util.simpletimetracker.core.databinding.ItemLoaderLayoutBinding as Binding

fun createLoaderAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { _, _, _ ->

    // Nothing to bind
}