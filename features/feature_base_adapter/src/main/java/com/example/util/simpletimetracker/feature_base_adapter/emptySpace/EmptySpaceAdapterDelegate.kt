package com.example.util.simpletimetracker.feature_base_adapter.emptySpace

import com.example.util.simpletimetracker.domain.extension.tryCast
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.google.android.flexbox.FlexboxLayoutManager
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemEmptySpaceLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData as ViewData

fun createEmptySpaceAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.root) {
        item as ViewData

        layoutParams
            .tryCast<FlexboxLayoutManager.LayoutParams>()
            ?.apply {
                width = item.widthDp.dpToPx()
                height = item.heightDp.dpToPx()
                isWrapBefore = item.wrapBefore
            }
    }
}
