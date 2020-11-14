package com.example.util.simpletimetracker.core.adapter.color

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import com.example.util.simpletimetracker.core.viewData.ColorViewData
import kotlinx.android.synthetic.main.item_color_layout.view.*

class ColorAdapterDelegate(
    private val onColorItemClick: ((ColorViewData) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        ColorViewHolder(parent)

    inner class ColorViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_color_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as ColorViewData

            layoutColorItem.setCardBackgroundColor(item.colorInt)
            layoutColorItem.setOnClickWith(item, onColorItemClick)
        }
    }
}