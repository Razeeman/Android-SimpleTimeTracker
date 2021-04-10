package com.example.util.simpletimetracker.core.adapter.hint

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import kotlinx.android.synthetic.main.item_hint_layout.view.tvHintItemText

fun createHintAdapterDelegate() = createRecyclerAdapterDelegate<HintViewData>(
    R.layout.item_hint_layout
) { itemView, item, _ ->

    with(itemView) {
        item as HintViewData

        tvHintItemText.text = item.text
    }
}