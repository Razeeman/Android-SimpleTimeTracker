package com.example.util.simpletimetracker.core.adapter.hint

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import kotlinx.android.synthetic.main.item_hint_layout.view.*

class HintAdapterDelegate : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        ChangeCategoryHintViewHolder(parent)

    inner class ChangeCategoryHintViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_hint_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView) {
            item as HintViewData

            tvHintItemText.text = item.text
        }
    }
}