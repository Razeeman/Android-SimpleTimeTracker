package com.example.util.simpletimetracker.core.adapter.info

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import kotlinx.android.synthetic.main.item_info_layout.view.tvInfoItemText

class InfoAdapterDelegate : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        InfoViewHolder(parent)

    inner class InfoViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_info_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as InfoViewData

            tvInfoItemText.text = item.text
        }
    }
}