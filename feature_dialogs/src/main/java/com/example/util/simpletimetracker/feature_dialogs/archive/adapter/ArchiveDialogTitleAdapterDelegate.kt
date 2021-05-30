package com.example.util.simpletimetracker.feature_dialogs.archive.adapter

import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogTitleViewData
import kotlinx.android.synthetic.main.item_archive_dialog_title_layout.view.*

fun createArchiveDialogTitleAdapterDelegate() =
    createRecyclerAdapterDelegate<ArchiveDialogTitleViewData>(
        R.layout.item_archive_dialog_title_layout
    ) { itemView, item, _ ->

        with(itemView) {
            item as ArchiveDialogTitleViewData

            tvArchiveDialogTitleItemText.text = item.text
        }
    }