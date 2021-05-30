package com.example.util.simpletimetracker.feature_dialogs.archive.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogButtonsViewData
import kotlinx.android.synthetic.main.item_archive_dialog_buttons.view.*

fun createArchiveDialogButtonsAdapterDelegate(
    onDeleteClick: () -> Unit,
    onRestoreClick: () -> Unit
) = createRecyclerAdapterDelegate<ArchiveDialogButtonsViewData>(
    R.layout.item_archive_dialog_buttons
) { itemView, _, _ ->

    with(itemView) {
        btnArchiveDialogDelete.setOnClick(onDeleteClick)
        btnArchiveDialogRestore.setOnClick(onRestoreClick)
    }
}