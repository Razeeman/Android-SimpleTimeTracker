package com.example.util.simpletimetracker.feature_dialogs.archive.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogInfoViewData
import kotlinx.android.synthetic.main.item_archive_dialog_info_layout.view.tvArchiveDialogInfoName
import kotlinx.android.synthetic.main.item_archive_dialog_info_layout.view.tvArchiveDialogInfoText

fun createArchiveDialogInfoAdapterDelegate() = createRecyclerAdapterDelegate<ArchiveDialogInfoViewData>(
    R.layout.item_archive_dialog_info_layout
) { itemView, item, _ ->

    with(itemView) {
        item as ArchiveDialogInfoViewData

        tvArchiveDialogInfoName.text = item.name
        tvArchiveDialogInfoText.text = item.text
    }
}