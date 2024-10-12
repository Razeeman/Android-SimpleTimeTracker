package com.example.util.simpletimetracker.feature_base_adapter.color

import android.content.res.ColorStateList
import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorFavouriteViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemColorFavouriteLayoutBinding as Binding

fun createColorFavouriteAdapterDelegate(
    onColorFavouriteItemClick: (() -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    val alphaShadowProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            outline ?: return
            view?.background?.getOutline(outline)
            outline.alpha = 0.3f
        }
    }

    with(binding) {
        item as ViewData

        ivColorFavouriteItem.imageTintList = ColorStateList.valueOf(item.iconColor)
        layoutColorFavouriteItem.outlineProvider = alphaShadowProvider
        layoutColorFavouriteItem.setOnClick(onColorFavouriteItemClick)
    }
}