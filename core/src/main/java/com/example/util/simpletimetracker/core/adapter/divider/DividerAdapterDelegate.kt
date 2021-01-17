package com.example.util.simpletimetracker.core.adapter.divider

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class DividerAdapterDelegate : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        DividerViewHolder(parent)

    inner class DividerViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_divider_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) {
            // Nothing to bind
        }
    }
}