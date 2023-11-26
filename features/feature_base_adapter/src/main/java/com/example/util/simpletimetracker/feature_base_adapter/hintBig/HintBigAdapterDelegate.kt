package com.example.util.simpletimetracker.feature_base_adapter.hintBig

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemHintBigLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData as ViewData

fun createHintBigAdapterDelegate(
    onCloseClick: () -> Unit = {},
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.itemHintBig) {
        item as ViewData

        itemText = item.text
        itemInfoIconVisible = item.infoIconVisible
        itemCloseIconVisible = item.closeIconVisible
        setOnCloseClick(onCloseClick)
    }
}