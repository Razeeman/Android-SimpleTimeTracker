package com.example.util.simpletimetracker.feature_categories.adapter

import android.view.ViewGroup
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_categories.R
import com.example.util.simpletimetracker.feature_categories.viewData.CategoryAddViewData
import kotlinx.android.synthetic.main.item_category_layout.view.*

class CategoryAddAdapterDelegate(
    private val onItemClick: (() -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        CategoryAddViewHolder(parent)

    inner class CategoryAddViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_category_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewCategoryItem) {
            item as CategoryAddViewData

            itemColor = item.color
            itemName = item.name
            setOnClick(onItemClick)
        }
    }
}