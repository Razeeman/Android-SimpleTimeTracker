package com.example.util.simpletimetracker.feature_categories.adapter

import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerViewHolder
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.feature_categories.R
import kotlinx.android.synthetic.main.item_category_layout.view.viewCategoryItem

class CategoryAdapterDelegate(
    private val onItemLongClick: ((CategoryViewData, Map<Any, String>) -> Unit)
) : BaseRecyclerAdapterDelegate() {

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder =
        CategoryViewHolder(parent)

    inner class CategoryViewHolder(parent: ViewGroup) :
        BaseRecyclerViewHolder(parent, R.layout.item_category_layout) {

        override fun bind(
            item: ViewHolderType,
            payloads: List<Any>
        ) = with(itemView.viewCategoryItem) {
            item as CategoryViewData
            val transitionName = TransitionNames.CATEGORY + item.id

            itemColor = item.color
            itemName = item.name
            setOnClick { onItemLongClick(item, mapOf(this to transitionName)) }
            ViewCompat.setTransitionName(this, transitionName)
        }
    }
}