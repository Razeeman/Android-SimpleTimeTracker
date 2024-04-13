package com.example.util.simpletimetracker.core.delegates.iconSelection.adapter

import androidx.core.view.isVisible
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.core.databinding.ItemIconSelectionCategoryLayoutBinding as Binding
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewData.IconSelectionCategoryViewData as ViewData

fun createIconSelectionCategoryAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData
        val tint = root.context.getThemedAttr(
            if (item.selected) R.attr.appLightTextColor else R.attr.colorPrimary,
        )
        with(ivIconSelectionCategoryItem) {
            setImageResource(item.categoryIcon)
            setColorFilter(tint)
            tag = item.categoryIcon
        }
        viewIconSelectionCategoryItem.isVisible = item.selected
        root.setOnClickWith(item, onItemClick)
    }
}