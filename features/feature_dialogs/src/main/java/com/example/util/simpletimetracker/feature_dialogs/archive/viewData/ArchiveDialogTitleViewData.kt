package com.example.util.simpletimetracker.feature_dialogs.archive.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ArchiveDialogTitleViewData(
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long =
        text.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ArchiveDialogTitleViewData
}