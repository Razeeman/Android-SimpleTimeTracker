package com.example.util.simpletimetracker.feature_base_adapter.hintBig

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemHintBigLayoutBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData as ViewData

fun createHintBigAdapterDelegate(
    onCloseClick: () -> Unit = {},
    onActionClick: (tag: ViewData.ButtonType?) -> Unit = {},
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.itemHintBig) {
        item as ViewData
        val button = item.button as? ViewData.Button.Present

        itemText = item.text
        itemInfoIconVisible = item.infoIconVisible
        itemCloseIconVisible = item.closeIconVisible
        itemActionButtonVisible = item.button is ViewData.Button.Present
        itemActionButtonText = button?.text.orEmpty()
        setOnCloseClick(onCloseClick)
        setOnActionClick { onActionClick(button?.type) }
    }
}