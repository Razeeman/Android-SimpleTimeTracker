package com.example.util.simpletimetracker.feature_dialogs.archive.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogInfoViewData as ViewData
import com.example.util.simpletimetracker.feature_dialogs.databinding.ItemArchiveDialogInfoLayoutBinding as Binding

fun createArchiveDialogInfoAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvArchiveDialogInfoName.text = item.name
        tvArchiveDialogInfoText.text = item.text
    }
}