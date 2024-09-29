package com.example.util.simpletimetracker.feature_base_adapter.emptySpace

import android.view.ViewGroup
import com.example.util.simpletimetracker.domain.extension.tryCast
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData.ViewDimension
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.google.android.flexbox.FlexboxLayoutManager
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemEmptySpaceLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData as ViewData

fun createEmptySpaceAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        fun ViewDimension.map(): Int {
            return when (this) {
                is ViewDimension.WrapContent -> ViewGroup.LayoutParams.WRAP_CONTENT
                is ViewDimension.MatchParent -> ViewGroup.LayoutParams.MATCH_PARENT
                is ViewDimension.ExactSizeDp -> value.dpToPx()
            }
        }

        layoutParams
            .tryCast<FlexboxLayoutManager.LayoutParams>()
            ?.apply {
                width = item.width.map()
                height = item.height.map()
                isWrapBefore = item.wrapBefore
            }
    }
}
