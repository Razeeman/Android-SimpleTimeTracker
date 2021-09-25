package com.example.util.simpletimetracker.feature_dialogs.archive.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

object ArchiveDialogButtonsViewData : ViewHolderType {

    override fun getUniqueId(): Long = 0L

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ArchiveDialogButtonsViewData
}