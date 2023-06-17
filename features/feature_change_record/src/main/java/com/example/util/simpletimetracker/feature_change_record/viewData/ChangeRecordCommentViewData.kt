package com.example.util.simpletimetracker.feature_change_record.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

sealed class ChangeRecordCommentViewData : ViewHolderType {

    abstract val text: String

    override fun getUniqueId(): Long = text.hashCode().toLong()

    data class Last(
        override val text: String,
    ) : ChangeRecordCommentViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Last
    }

    data class Favourite(
        override val text: String,
    ) : ChangeRecordCommentViewData() {

        override fun isValidType(other: ViewHolderType): Boolean = other is Favourite
    }
}