package com.example.util.simpletimetracker.core.adapter.color

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.ColorViewData
import kotlinx.android.synthetic.main.item_color_layout.view.layoutColorItem

fun createColorAdapterDelegate(
    onColorItemClick: ((ColorViewData) -> Unit)
) = createRecyclerAdapterDelegate<ColorViewData>(
    R.layout.item_color_layout
) { itemView, item, _ ->

    with(itemView) {
        item as ColorViewData

        layoutColorItem.setCardBackgroundColor(item.colorInt)
        layoutColorItem.setOnClickWith(item, onColorItemClick)
    }
}