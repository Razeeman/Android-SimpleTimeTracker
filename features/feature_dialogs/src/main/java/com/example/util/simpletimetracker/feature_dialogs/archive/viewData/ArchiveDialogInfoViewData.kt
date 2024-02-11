package com.example.util.simpletimetracker.feature_dialogs.archive.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class ArchiveDialogInfoViewData(
    val name: String,
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = name.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ArchiveDialogInfoViewData
}