package com.example.util.simpletimetracker.core.adapter.divider

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate

fun createDividerAdapterDelegate() = createRecyclerAdapterDelegate<DividerViewData>(
    R.layout.item_divider_layout
) { _, _, _ ->

    // Nothing to bind
}
