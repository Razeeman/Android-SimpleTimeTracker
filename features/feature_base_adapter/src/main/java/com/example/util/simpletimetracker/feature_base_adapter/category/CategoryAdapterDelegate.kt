package com.example.util.simpletimetracker.feature_base_adapter.category

import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClickWith
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemCategoryLayoutBinding as Binding

fun createCategoryAdapterDelegate(
    onClick: ((ViewData) -> Unit)? = null,
    onLongClick: ((ViewData) -> Unit)? = null,
    onClickWithTransition: ((ViewData, Pair<Any, String>) -> Unit)? = null,
    onLongClickWithTransition: ((ViewData, Pair<Any, String>) -> Unit)? = null,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewCategoryItem) {
        item as ViewData

        val transitionName = when (item) {
            is ViewData.Category -> TransitionNames.CATEGORY
            is ViewData.Record -> TransitionNames.RECORD_TAG
        } + item.id
        val sharedElements: Pair<Any, String> = this to transitionName
        ViewCompat.setTransitionName(this, transitionName)

        itemColor = item.color
        itemName = item.name
        itemIconColor = item.iconColor

        if (item is ViewData.Record) {
            itemIconAlpha = item.iconAlpha
            itemIconVisible = item.icon != null
            item.icon?.let(this::itemIcon::set)
        } else {
            itemIconVisible = false
        }

        onClick?.let { setOnClickWith(item, it) }
        onLongClick?.let { setOnLongClickWith(item, it) }
        onClickWithTransition?.let { setOnClick { onClickWithTransition(item, sharedElements) } }
        onLongClickWithTransition?.let { setOnLongClick { onLongClickWithTransition(item, sharedElements) } }
    }
}