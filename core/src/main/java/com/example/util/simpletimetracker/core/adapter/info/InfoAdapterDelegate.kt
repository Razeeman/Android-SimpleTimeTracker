package com.example.util.simpletimetracker.core.adapter.info

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import kotlinx.android.synthetic.main.item_info_layout.view.tvInfoItemText

fun createInfoAdapterDelegate() = createRecyclerAdapterDelegate<InfoViewData>(
    R.layout.item_info_layout
) { itemView, item, _ ->

    with(itemView) {
        item as InfoViewData

        tvInfoItemText.text = item.text
    }
}