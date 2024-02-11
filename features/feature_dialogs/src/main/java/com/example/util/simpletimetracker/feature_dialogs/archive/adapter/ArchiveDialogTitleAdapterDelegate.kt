package com.example.util.simpletimetracker.feature_dialogs.archive.adapter

import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.viewData.ArchiveDialogTitleViewData as ViewData
import com.example.util.simpletimetracker.feature_dialogs.databinding.ItemArchiveDialogTitleLayoutBinding as Binding

fun createArchiveDialogTitleAdapterDelegate() =
    createRecyclerBindingAdapterDelegate<ViewData, Binding>(
        Binding::inflate,
    ) { itemView, item, _ ->

        with(itemView) {
            item as ViewData

            tvArchiveDialogTitleItemText.text = item.text
        }
    }