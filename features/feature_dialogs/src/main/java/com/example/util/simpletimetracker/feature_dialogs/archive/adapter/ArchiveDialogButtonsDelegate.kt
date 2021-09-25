package com.example.util.simpletimetracker.feature_dialogs.archive.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogButtonsViewData as ViewData
import com.example.util.simpletimetracker.feature_dialogs.databinding.ItemArchiveDialogButtonsBinding as Binding

fun createArchiveDialogButtonsAdapterDelegate(
    onDeleteClick: () -> Unit,
    onRestoreClick: () -> Unit
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, _, _ ->

    with(binding) {
        btnArchiveDialogDelete.setOnClick(onDeleteClick)
        btnArchiveDialogRestore.setOnClick(onRestoreClick)
    }
}