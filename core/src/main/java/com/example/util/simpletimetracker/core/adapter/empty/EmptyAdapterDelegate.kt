package com.example.util.simpletimetracker.core.adapter.empty

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.visible
import kotlinx.android.synthetic.main.item_empty_layout.view.tvEmptyItem
import kotlinx.android.synthetic.main.item_empty_layout.view.tvEmptyItemHint

fun createEmptyAdapterDelegate() = createRecyclerAdapterDelegate<EmptyViewData>(
    R.layout.item_empty_layout
) { itemView, item, _ ->

    with(itemView) {
        item as EmptyViewData

        tvEmptyItem.text = item.message

        if (item.hint.isNotEmpty()) {
            tvEmptyItemHint.visible = true
            tvEmptyItemHint.text = item.hint
        } else {
            tvEmptyItemHint.visible = false
        }
    }
}