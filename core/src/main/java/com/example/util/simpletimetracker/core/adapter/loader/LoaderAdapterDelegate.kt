package com.example.util.simpletimetracker.core.adapter.loader

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate

fun createLoaderAdapterDelegate() = createRecyclerAdapterDelegate<LoaderViewData>(
    R.layout.item_loader_layout
) { _, _, _ ->

    // Nothing to bind
}