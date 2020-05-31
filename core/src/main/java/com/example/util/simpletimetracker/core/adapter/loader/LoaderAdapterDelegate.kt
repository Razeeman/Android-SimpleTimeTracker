package com.example.util.simpletimetracker.core.adapter.loader

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

class LoaderAdapterDelegate() : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        LoaderViewHolder(parent)

    inner class LoaderViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_loader_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) {
            // Nothing to bind
        }
    }
}