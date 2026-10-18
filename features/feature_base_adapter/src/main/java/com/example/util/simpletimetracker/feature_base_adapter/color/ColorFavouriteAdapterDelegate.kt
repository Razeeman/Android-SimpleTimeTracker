package com.example.util.simpletimetracker.feature_base_adapter.color

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorFavouriteViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemColorFavouriteLayoutBinding as Binding

fun createColorFavouriteAdapterDelegate(
    onColorFavouriteItemClick: (() -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        ivColorFavouriteItem.setImageResource(item.iconResId)
        layoutColorFavouriteItem.setCardBackgroundColor(item.backgroundColor)
        layoutColorFavouriteItem.setOnClick(onColorFavouriteItemClick)
    }
}