package com.example.util.simpletimetracker.core.adapter.empty

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.visible
import kotlinx.android.synthetic.main.item_empty_layout.view.*

class EmptyAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        EmptyViewHolder(parent)

    inner class EmptyViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_empty_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
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
}